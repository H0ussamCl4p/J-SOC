package jsoc.repository.csv;

import jsoc.model.enums.IncidentStatus;
import jsoc.model.enums.Severity;
import jsoc.model.incident.DDoSIncident;
import jsoc.model.incident.Incident;
import jsoc.model.incident.MalwareIncident;
import jsoc.model.incident.PhishingIncident;
import jsoc.model.user.Analyst;
import jsoc.model.user.Manager;
import jsoc.model.user.User;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for reading and writing CSV files.
 *
 * Incident CSV columns (8 columns):
 *   id, type, title, description, severity, status, assignedTo, createdAt
 *
 * User CSV columns (4 columns):
 *   username, password, email, role
 *
 * NOTE: There is no resolvedAt field because Member 1's Incident class does
 * not have one. Resolution time tracking is handled via HistoryEntry.
 */
public class CsvHelper {

    private static final String SEP = ",";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ===========================
    //        INCIDENTS
    // ===========================

    /**
     * Load incidents from a CSV file and re-link their assignedTo User objects.
     *
     * @param filePath path to incidents.csv
     * @param userMap  map of username -> User, used to restore the assignedTo reference
     */
    public static List<Incident> loadIncidents(String filePath, Map<String, User> userMap) {
        List<Incident> incidents = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return incidents;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                if (line.isBlank()) continue;
                try {
                    incidents.add(parseIncident(line, userMap));
                } catch (Exception e) {
                    System.err.println("[CsvHelper] Skipping malformed incident line: " + line);
                    System.err.println("  Reason: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[CsvHelper] Cannot read " + filePath + ": " + e.getMessage());
        }
        return incidents;
    }

    /**
     * Save all incidents to a CSV file. Called after every write operation.
     */
    public static void saveIncidents(String filePath, List<Incident> incidents) {
        ensureDataDir(filePath);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("id,type,title,description,severity,status,assignedTo,createdAt");
            for (Incident i : incidents) {
                pw.println(incidentToCsv(i));
            }
        } catch (IOException e) {
            System.err.println("[CsvHelper] Cannot write " + filePath + ": " + e.getMessage());
        }
    }

    // ===========================
    //          USERS
    // ===========================

    /**
     * Load users from a CSV file.
     */
    public static List<User> loadUsers(String filePath) {
        List<User> users = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return users;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.isBlank()) continue;
                try {
                    users.add(parseUser(line));
                } catch (Exception e) {
                    System.err.println("[CsvHelper] Skipping malformed user line: " + line);
                    System.err.println("  Reason: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[CsvHelper] Cannot read " + filePath + ": " + e.getMessage());
        }
        return users;
    }

    /**
     * Save all users to a CSV file.
     */
    public static void saveUsers(String filePath, List<User> users) {
        ensureDataDir(filePath);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("username,password,email,role");
            for (User u : users) {
                pw.println(userToCsv(u));
            }
        } catch (IOException e) {
            System.err.println("[CsvHelper] Cannot write " + filePath + ": " + e.getMessage());
        }
    }

    // ===========================
    //       PARSING HELPERS
    // ===========================

    /**
     * Parse one CSV line into an Incident.
     * Uses the full 7-argument constructor of each subclass, which matches
     * exactly what Member 1 defined in PhishingIncident, MalwareIncident, DDoSIncident.
     */
    private static Incident parseIncident(String line, Map<String, User> userMap) {
        String[] parts = splitCsvLine(line);

        String id          = parts[0].trim();
        String type        = parts[1].trim();
        String title       = unescape(parts[2]);
        String description = unescape(parts[3]);
        Severity severity  = Severity.valueOf(parts[4].trim());
        IncidentStatus status = IncidentStatus.valueOf(parts[5].trim());
        String assignedToUsername = parts[6].trim();
        LocalDateTime createdAt = LocalDateTime.parse(parts[7].trim(), FMT);

        // Resolve the User object from the stored username (null if unassigned)
        User assignedTo = assignedToUsername.isEmpty() ? null : userMap.get(assignedToUsername);

        // Use the full constructor — matches Member 1's subclass constructors exactly:
        // (String id, String title, String description, Severity severity,
        //  IncidentStatus status, LocalDateTime createdAt, User assignedTo)
        return switch (type) {
            case "PHISHING" -> new PhishingIncident(id, title, description, severity, status, createdAt, assignedTo);
            case "MALWARE"  -> new MalwareIncident(id, title, description, severity, status, createdAt, assignedTo);
            case "DDOS"     -> new DDoSIncident(id, title, description, severity, status, createdAt, assignedTo);
            default -> throw new IllegalArgumentException("Unknown incident type: " + type);
        };
    }

    /**
     * Serialize an Incident to a CSV line.
     * Uses getAssignee() (not getAssignedTo()) — matches Member 1's Assignable interface.
     */
    private static String incidentToCsv(Incident i) {
        String assignedToUsername = i.isAssigned() ? i.getAssignee().getUsername() : "";
        return String.join(SEP,
                i.getId(),
                i.getType(),
                escape(i.getTitle()),
                escape(i.getDescription()),
                i.getSeverity().name(),
                i.getStatus().name(),
                assignedToUsername,
                i.getCreatedAt().format(FMT)
        );
    }

    /**
     * Parse one CSV line into a User.
     * Uses (String username, String password, String email) constructor — matches Member 1.
     */
    private static User parseUser(String line) {
        String[] parts = splitCsvLine(line);
        String username = parts[0].trim();
        String password = parts[1].trim();
        String email    = parts[2].trim();
        String role     = parts[3].trim();
        return role.equalsIgnoreCase("MANAGER")
                ? new Manager(username, password, email)
                : new Analyst(username, password, email);
    }

    /** Serialize a User to a CSV line. */
    private static String userToCsv(User u) {
        return String.join(SEP,
                u.getUsername(),
                u.getPassword(),
                u.getEmail(),
                u.getRole()
        );
    }

    // ===========================
    //      CSV UTILITY METHODS
    // ===========================

    /**
     * Split a CSV line into fields, respecting quoted fields (fields that
     * contain commas are wrapped in double quotes).
     */
    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"'); // escaped quote ""
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Wrap a field in double quotes if it contains a comma, newline, or quote.
     * Internal double quotes are escaped as "".
     */
    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Remove surrounding quotes added by escape(), and unescape internal "".
     */
    private static String unescape(String value) {
        if (value == null) return "";
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value;
    }

    /** Create the data/ directory if it doesn't exist yet. */
    private static void ensureDataDir(String filePath) {
        File dir = new File(filePath).getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
    }
}

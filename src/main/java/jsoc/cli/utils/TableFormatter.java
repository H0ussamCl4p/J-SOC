package jsoc.cli.utils;

import jsoc.model.incident.Incident;
import java.util.List;

public class TableFormatter {

    public void printIncidentTable(List<Incident> incidents) {
        if (incidents.isEmpty()) {
            System.out.println("  (no incidents to display)");
            return;
        }

        String format = "│ %-10s │ %-25s │ %-12s │ %-10s │ %-15s │%n";
        String separator = "├────────────┼───────────────────────────┼──────────────┼────────────┼─────────────────┤";
        String top       = "┌────────────┬───────────────────────────┬──────────────┬────────────┬─────────────────┐";
        String bottom    = "└────────────┴───────────────────────────┴──────────────┴────────────┴─────────────────┘";

        System.out.println(top);
        System.out.printf(format, "ID", "Title", "Type", "Severity", "Status");
        System.out.println(separator);

        for (Incident i : incidents) {
            System.out.printf(format,
                    i.getId(),
                    truncate(i.getTitle(), 25),
                    i.getClass().getSimpleName().replace("Incident", ""),
                    i.getSeverity(),
                    i.getStatus()
            );
        }

        System.out.println(bottom);
        System.out.println("  Total: " + incidents.size() + " incident(s)");
    }

    public void printKeyValue(String key, String value) {
        System.out.printf("  %-20s : %s%n", key, value);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}
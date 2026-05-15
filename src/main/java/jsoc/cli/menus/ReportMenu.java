package jsoc.cli.menus;

import jsoc.model.incident.Incident;
import jsoc.model.user.User;
import jsoc.service.IncidentService;
import jsoc.service.SLAService;
import jsoc.cli.utils.ConsoleHelper;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ReportMenu {

    private final User currentUser;
    private final ConsoleHelper console;
    private final IncidentService incidentService;
    private final SLAService slaService;

    public ReportMenu(User currentUser, Scanner scanner,
                      IncidentService incidentService,
                      SLAService slaService) {
        this.currentUser = currentUser;
        this.console = new ConsoleHelper(scanner);
        this.incidentService = incidentService;
        this.slaService = slaService;
    }

    public void show() {
        boolean running = true;
        while (running) {
            console.printHeader("Reports");
            System.out.println("  1. SLA report (console)");
            System.out.println("  2. Export TXT report");
            System.out.println("  3. Export CSV report");
            System.out.println("  4. Export Markdown report");
            System.out.println("  0. Back");

            int choice = console.readInt("\nChoice: ", 0, 4);
            switch (choice) {
                case 1 -> showSlaReport();
                case 2 -> exportTXT();
                case 3 -> exportCSV();
                case 4 -> exportMarkdown();
                case 0 -> running = false;
            }
        }
    }

    private void showSlaReport() {
        slaService.printSlaReport();
        console.pause();
    }

    private void exportTXT() {
        String filename = "report_" + timestamp() + ".txt";
        List<Incident> incidents = incidentService.getAllIncidents();
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("J-SOC INCIDENT REPORT\n");
            fw.write("Generated: " + LocalDateTime.now() + "\n");
            fw.write("By: " + currentUser.getUsername() + "\n");
            fw.write("=".repeat(50) + "\n\n");
            for (Incident i : incidents) {
                fw.write("ID       : " + i.getId() + "\n");
                fw.write("Title    : " + i.getTitle() + "\n");
                fw.write("Severity : " + i.getSeverity() + "\n");
                fw.write("Status   : " + i.getStatus() + "\n");
                fw.write("Assigned : " + (i.isAssigned() ? i.getAssignee().getUsername() : "Unassigned") + "\n");
                fw.write("-".repeat(50) + "\n");
            }
            System.out.println("  ✔ TXT report saved: " + filename);
        } catch (IOException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
        console.pause();
    }

    private void exportCSV() {
        String filename = "report_" + timestamp() + ".csv";
        List<Incident> incidents = incidentService.getAllIncidents();
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("ID,Title,Type,Severity,Status,AssignedTo\n");
            for (Incident i : incidents) {
                fw.write(String.join(",",
                        csvEscape(i.getId()),
                        csvEscape(i.getTitle()),
                        csvEscape(i.getType()),
                        csvEscape(i.getSeverity().toString()),
                        csvEscape(i.getStatus().toString()),
                        csvEscape(i.isAssigned() ? i.getAssignee().getUsername() : "Unassigned")
                ) + "\n");
            }
            System.out.println("  ✔ CSV report saved: " + filename);
        } catch (IOException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
        console.pause();
    }

    private void exportMarkdown() {
        String filename = "report_" + timestamp() + ".md";
        List<Incident> incidents = incidentService.getAllIncidents();
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write("# J-SOC Incident Report\n\n");
            fw.write("**Generated:** " + LocalDateTime.now() + "  \n");
            fw.write("**By:** " + currentUser.getUsername() + "\n\n");
            fw.write("| ID | Title | Type | Severity | Status | Assigned To |\n");
            fw.write("|----|-------|------|----------|--------|-------------|\n");
            for (Incident i : incidents) {
                fw.write("| " + String.join(" | ",
                        i.getId(),
                        i.getTitle().replace("|", "\\|"),
                        i.getType(),
                        i.getSeverity().toString(),
                        i.getStatus().toString(),
                        i.isAssigned() ? i.getAssignee().getUsername() : "Unassigned"
                ) + " |\n");
            }
            System.out.println("  ✔ Markdown report saved: " + filename);
        } catch (IOException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
        console.pause();
    }

    private static String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}

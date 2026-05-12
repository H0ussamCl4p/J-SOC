package jsoc.cli.menus;

import jsoc.model.incident.Incident;
import jsoc.model.user.User;
import jsoc.service.IncidentService;
import jsoc.cli.utils.ConsoleHelper;
import jsoc.cli.utils.TableFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ReportMenu {

    private final User currentUser;
    private final ConsoleHelper console;
    private final TableFormatter table;
    private final IncidentService incidentService;

    public ReportMenu(User currentUser, Scanner scanner, IncidentService incidentService) {
        this.currentUser = currentUser;
        this.console = new ConsoleHelper(scanner);
        this.table = new TableFormatter();
        this.incidentService = incidentService;
    }

    public void show() {
        boolean running = true;
        while (running) {
            console.printHeader("Reports");
            System.out.println("  1. Export TXT report");
            System.out.println("  2. Export CSV report");
            System.out.println("  3. Export Markdown report");
            System.out.println("  0. Back");

            int choice = console.readInt("\nChoice: ", 0, 3);
            switch (choice) {
                case 1 -> exportTXT();
                case 2 -> exportCSV();
                case 3 -> exportMarkdown();
                case 0 -> running = false;
            }
        }
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
                        i.getId(),
                        "\"" + i.getTitle() + "\"",
                        i.getClass().getSimpleName(),
                        i.getSeverity().toString(),
                        i.getStatus().toString(),
                        i.isAssigned() ? i.getAssignee().getUsername() : "Unassigned"
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
                        i.getTitle(),
                        i.getClass().getSimpleName(),
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

    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
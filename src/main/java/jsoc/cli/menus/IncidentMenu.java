package jsoc.cli.menus;

import jsoc.model.user.User;
import jsoc.model.user.Manager;
import jsoc.model.incident.Incident;
import jsoc.model.enums.IncidentStatus;
import jsoc.service.IncidentService;
import jsoc.service.SLAService;
import jsoc.cli.utils.ConsoleHelper;
import jsoc.cli.utils.TableFormatter;
import java.util.List;
import java.util.Scanner;

public class IncidentMenu {

    private final User currentUser;
    private final ConsoleHelper console;
    private final TableFormatter table;
    private final IncidentService incidentService;
    private final SLAService slaService;

    public IncidentMenu(User currentUser, Scanner scanner,
                        IncidentService incidentService,
                        SLAService slaService) {
        this.currentUser = currentUser;
        this.console = new ConsoleHelper(scanner);
        this.table = new TableFormatter();
        this.incidentService = incidentService;
        this.slaService = slaService;
    }

    public void show() {
        boolean running = true;
        while (running) {
            console.printHeader("Incidents");
            System.out.println("  1. List all incidents");
            System.out.println("  2. View incident details");
            System.out.println("  3. Update incident status");
            System.out.println("  4. Add comment");
            if (currentUser instanceof Manager) {
                System.out.println("  5. Delete incident");
            }
            System.out.println("  0. Back");

            int choice = console.readInt("\nChoice: ", 0, 5);
            switch (choice) {
                case 1 -> listIncidents();
                case 2 -> viewIncident();
                case 3 -> updateStatus();
                case 4 -> addComment();
                case 5 -> {
                    if (currentUser instanceof Manager) deleteIncident();
                    else System.out.println("  ✘ Access denied.");
                }
                case 0 -> running = false;
            }
        }
    }

    private void listIncidents() {
        console.printHeader("All Incidents");
        List<Incident> incidents = incidentService.getAllIncidents();
        table.printIncidentTable(incidents);
        console.pause();
    }

    private void viewIncident() {
        String id = console.readNonEmpty("Incident ID: ");
        try {
            Incident i = incidentService.getById(id);
            console.printHeader("Incident — " + i.getId());
            console.printKeyValue("Title", i.getTitle());
            console.printKeyValue("Type", i.getType());
            console.printKeyValue("Severity", i.getSeverity().toString());
            console.printKeyValue("Status", i.getStatus().toString());
            console.printKeyValue("Assigned to", i.isAssigned() ? i.getAssignee().getUsername() : "Unassigned");
            console.printKeyValue("Description", i.getDescription());
            console.printKeyValue("Time remaining", slaService.getTimeRemaining(i));
            console.printKeyValue("SLA breached", String.valueOf(slaService.isSlaBreached(i)));
        } catch (Exception e) {
            System.out.println("  ✘ " + e.getMessage());
        }
        console.pause();
    }

    private void updateStatus() {
        String id = console.readNonEmpty("Incident ID: ");
        System.out.println("  Status: 1-IN_PROGRESS  2-RESOLVED  3-ESCALATED");
        int s = console.readInt("New status: ", 1, 3);
        IncidentStatus[] options = {IncidentStatus.IN_PROGRESS, IncidentStatus.RESOLVED, IncidentStatus.ESCALATED};
        try {
            incidentService.changeStatus(id, options[s - 1]);
            System.out.println("  ✔ Status updated.");
        } catch (Exception e) {
            System.out.println("  ✘ " + e.getMessage());
        }
        console.pause();
    }

    private void addComment() {
        String id = console.readNonEmpty("Incident ID: ");
        String text = console.readNonEmpty("Comment: ");
        try {
            incidentService.addComment(id, text);
            System.out.println("  ✔ Comment added.");
        } catch (Exception e) {
            System.out.println("  ✘ " + e.getMessage());
        }
        console.pause();
    }

    private void deleteIncident() {
        String id = console.readNonEmpty("Incident ID to delete: ");
        if (console.confirm("Are you sure?")) {
            try {
                incidentService.deleteIncident(id);
                System.out.println("  ✔ Incident deleted.");
            } catch (Exception e) {
                System.out.println("  ✘ " + e.getMessage());
            }
        }
        console.pause();
    }
}
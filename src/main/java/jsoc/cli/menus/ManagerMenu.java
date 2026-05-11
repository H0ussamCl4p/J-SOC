package jsoc.cli.menus;

import jsoc.model.user.User;
import jsoc.model.enums.IncidentStatus;
import jsoc.service.IncidentService;
import jsoc.service.AssignmentService;
import jsoc.service.StatisticsService;
import jsoc.cli.utils.ConsoleHelper;
import jsoc.cli.utils.TableFormatter;
import java.util.Scanner;

public class ManagerMenu {

    private final User currentUser;
    private final ConsoleHelper console;
    private final TableFormatter table;
    private final IncidentService incidentService;
    private final AssignmentService assignmentService;
    private final StatisticsService statisticsService;

    public ManagerMenu(User currentUser, Scanner scanner,
                       IncidentService incidentService,
                       AssignmentService assignmentService,
                       StatisticsService statisticsService) {
        this.currentUser = currentUser;
        this.console = new ConsoleHelper(scanner);
        this.table = new TableFormatter();
        this.incidentService = incidentService;
        this.assignmentService = assignmentService;
        this.statisticsService = statisticsService;
    }

    public void show() {
        boolean running = true;
        while (running) {
            console.printHeader("Manager Panel");
            System.out.println("  1. Assign incident to analyst");
            System.out.println("  2. Reassign incident");
            System.out.println("  3. Unassign incident");
            System.out.println("  4. View workload summary");
            System.out.println("  5. View statistics dashboard");
            System.out.println("  6. Close incident");
            System.out.println("  0. Back");

            int choice = console.readInt("\nChoice: ", 0, 6);
            switch (choice) {
                case 1 -> assignManually();
                case 2 -> reassign();
                case 3 -> unassign();
                case 4 -> viewWorkload();
                case 5 -> viewStatistics();
                case 6 -> closeIncident();
                case 0 -> running = false;
            }
        }
    }

    private void assignManually() {
        String id = console.readNonEmpty("Incident ID: ");
        String analyst = console.readNonEmpty("Analyst username: ");
        try {
            assignmentService.assign(id, analyst);
            System.out.println("  ✔ Incident assigned to " + analyst + ".");
        } catch (Exception e) {
            System.out.println("  ✘ " + e.getMessage());
        }
        console.pause();
    }

    private void reassign() {
        String id = console.readNonEmpty("Incident ID: ");
        String analyst = console.readNonEmpty("New analyst username: ");
        try {
            assignmentService.reassign(id, analyst);
            System.out.println("  ✔ Incident reassigned to " + analyst + ".");
        } catch (Exception e) {
            System.out.println("  ✘ " + e.getMessage());
        }
        console.pause();
    }

    private void unassign() {
        String id = console.readNonEmpty("Incident ID: ");
        try {
            assignmentService.unassign(id);
            System.out.println("  ✔ Incident unassigned.");
        } catch (Exception e) {
            System.out.println("  ✘ " + e.getMessage());
        }
        console.pause();
    }

    private void viewWorkload() {
        console.printHeader("Analyst Workload");
        assignmentService.getWorkloadSummary().forEach(System.out::println);
        console.pause();
    }

    private void viewStatistics() {
        statisticsService.printDashboard();
        console.pause();
    }

    private void closeIncident() {
        String id = console.readNonEmpty("Incident ID to close: ");
        if (console.confirm("Confirm close?")) {
            try {
                incidentService.changeStatus(id, IncidentStatus.CLOSED);
                System.out.println("  ✔ Incident closed.");
            } catch (Exception e) {
                System.out.println("  ✘ " + e.getMessage());
            }
        }
        console.pause();
    }
}
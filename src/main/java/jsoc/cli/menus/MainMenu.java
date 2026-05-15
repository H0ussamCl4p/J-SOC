package jsoc.cli.menus;

import jsoc.model.user.User;
import jsoc.model.user.Manager;
import jsoc.service.IncidentService;
import jsoc.service.AssignmentService;
import jsoc.service.SLAService;
import jsoc.service.StatisticsService;
import jsoc.cli.utils.ConsoleHelper;
import java.util.Scanner;

public class MainMenu {

    private final User currentUser;
    private final ConsoleHelper console;
    private final IncidentMenu incidentMenu;
    private final ManagerMenu managerMenu;
    private final ReportMenu reportMenu;

    public MainMenu(User currentUser, Scanner scanner,
                    IncidentService incidentService,
                    AssignmentService assignmentService,
                    SLAService slaService,
                    StatisticsService statisticsService) {
        this.currentUser = currentUser;
        this.console = new ConsoleHelper(scanner);
        this.incidentMenu = new IncidentMenu(currentUser, scanner, incidentService, slaService);
        this.managerMenu = new ManagerMenu(currentUser, scanner, incidentService, assignmentService, statisticsService);
        this.reportMenu = new ReportMenu(currentUser, scanner, incidentService, slaService);
    }

    public void show() {
        boolean running = true;
        while (running) {
            console.printHeader("Main Menu — " + currentUser.getUsername());
            System.out.println("  1. Incidents");
            if (currentUser instanceof Manager) {
                System.out.println("  2. Manager Panel");
            }
            System.out.println("  3. Reports");
            System.out.println("  0. Logout");

            int choice = console.readInt("\nChoice: ", 0, 3);
            switch (choice) {
                case 1 -> incidentMenu.show();
                case 2 -> {
                    if (currentUser instanceof Manager) managerMenu.show();
                    else System.out.println("  ✘ Access denied.");
                }
                case 3 -> reportMenu.show();
                case 0 -> {
                    System.out.println("\nGoodbye, " + currentUser.getUsername() + "!");
                    running = false;
                }
            }
        }
    }
}
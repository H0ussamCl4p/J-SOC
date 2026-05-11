package jsoc;

import jsoc.cli.menus.LoginMenu;
import jsoc.cli.menus.MainMenu;
import jsoc.model.user.User;
import jsoc.repository.IncidentRepository;
import jsoc.repository.UserRepository;
import jsoc.service.AssignmentService;
import jsoc.service.AuthService;
import jsoc.service.IncidentService;
import jsoc.service.SLAService;
import jsoc.service.StatisticsService;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Repositories
        UserRepository userRepository = new UserRepository();
        IncidentRepository incidentRepository = new IncidentRepository(userRepository);

        // Services
        AuthService authService = new AuthService(userRepository);
        IncidentService incidentService = new IncidentService(incidentRepository, authService);
        AssignmentService assignmentService = new AssignmentService(incidentRepository, userRepository, authService);
        SLAService slaService = new SLAService(incidentRepository);
        StatisticsService statisticsService = new StatisticsService(incidentRepository, userRepository, slaService);

        // Login
        LoginMenu loginMenu = new LoginMenu(authService, scanner);
        User user = loginMenu.show();

        // Main app
        MainMenu mainMenu = new MainMenu(user, scanner, incidentService, assignmentService, slaService, statisticsService);
        mainMenu.show();

        authService.logout();
        scanner.close();
    }
}

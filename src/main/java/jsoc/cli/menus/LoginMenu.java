package jsoc.cli.menus;
import jsoc.model.user.User;
import jsoc.service.AuthService;
import java.util.Scanner;
public class LoginMenu {
        private final AuthService authService;
        private final Scanner scanner;

        public LoginMenu(AuthService authService, Scanner scanner) {
            this.authService = authService;
            this.scanner = scanner;
        }

        public User show() {
            System.out.println("╔══════════════════════════════╗");
            System.out.println("║       J-SOC Platform         ║");
            System.out.println("║  Security Operations Center  ║");
            System.out.println("╚══════════════════════════════╝");

            User user = null;
            while (user == null) {
                System.out.print("\nUsername: ");
                if (!scanner.hasNextLine()) {
                    System.out.println("\nGoodbye.");
                    System.exit(0);
                }
                String username = scanner.nextLine().trim();
                System.out.print("Password: ");
                if (!scanner.hasNextLine()) {
                    System.out.println("\nGoodbye.");
                    System.exit(0);
                }
                String password = scanner.nextLine().trim();

                try {
                    user = authService.login(username, password);
                    System.out.println("\n✔ Welcome, " + user.getUsername() + "! [" + user.getRole() + "]");
                } catch (Exception e) {
                    System.out.println("\n✘ " + e.getMessage() + " — please try again.");
                }
            }
            return user;
        }
    }

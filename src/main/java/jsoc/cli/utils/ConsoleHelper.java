package jsoc.cli.utils;

import java.util.Scanner;

public class ConsoleHelper {

    private final Scanner scanner;

    public ConsoleHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.println("  ✘ Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Invalid input, enter a number.");
            }
        }
    }

    public String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("  ✘ This field cannot be empty.");
        }
    }

    public boolean confirm(String prompt) {
        System.out.print(prompt + " (y/n): ");
        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }

    public void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printSeparator() {
        System.out.println("──────────────────────────────────────────");
    }

    public void printHeader(String title) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.printf( "║  %-40s║%n", title);
        System.out.println("╚══════════════════════════════════════════╝");
    }
    public void printKeyValue(String key, String value) {
        System.out.printf("  %-20s : %s%n", key, value);
    }
}

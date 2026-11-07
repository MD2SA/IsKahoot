package server.cli.utils;

import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import server.GameManager;

public final class Helpers {

    private Helpers() {
    }

    public static void printSectionHeader(String title) {
        System.out.println("\n=== " + title + " " + "=".repeat(50 - title.length()) + "\n");
    }

    public static int readPositiveInt(Scanner scanner, String prompt) {
        int value = -1;
        while (value <= 0) {
            System.out.print(prompt + " ");
            String input = scanner.nextLine();
            try {
                value = Integer.parseInt(input);
                if (value <= 0) {
                    System.out.println("Please enter a positive number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        return value;
    }

    public static int readIntInRange(Scanner scanner, int min, int max, String prompt) {
        int value = -1;
        while (value < min || value > max) {
            System.out.print(prompt + " ");
            String input = scanner.nextLine();
            try {
                value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.printf("Please enter a number between %d and %d.%n", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        return value;
    }

    public static String generateUniqueRandomCode(int length, GameManager gameManager) {
        String chars = "QWERTYUIOPASDFGHJKLZXCVBNM1234567890";

        String id;
        do {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < length) {
                int randomIdx = ThreadLocalRandom.current().nextInt(chars.length());
                sb.append(chars.charAt(randomIdx));
            }
            id = sb.toString();
        } while (gameManager.hasGame(id));

        return id;
    }

    public static String readGameCode(Scanner scanner, GameManager gameManager, int length) {

        System.out.print("Game code (letters/numbers only) - press enter for random: ");

        for (int attempt = 0; attempt < 2; attempt++) {
            String input = scanner.nextLine().trim();

            if (input.isBlank()) {
                String randomCode = generateUniqueRandomCode(length, gameManager);
                System.out.println("Random code generated: " + randomCode);
                return randomCode;
            }

            if (!input.matches("[A-Za-z0-9]+")) {
                System.out.println("Only letters and numbers are allowed.");
                continue;
            }

            String code = input.toUpperCase(Locale.ROOT);

            if (gameManager.hasGame(code)) {
                System.out.println("This code already exists. Try another one.");
                continue;
            }

            System.out.println("Personalized code accepted: " + code);
            return code;
        }

        String fallbackCode = generateUniqueRandomCode(length, gameManager);
        System.out.println("Too many invalid attempts. A random code has been generated: " + fallbackCode);
        return fallbackCode;
    }

    public static int getArgOrReadPositiveInt(String[] args, int argIndex, Scanner scanner, String prompt) {
        if (args.length > argIndex) {
            int value = 0;
            boolean parsed = true;
            try {
                value = Integer.parseInt(args[argIndex]);
            } catch (NumberFormatException e) {
                parsed = false;
            }
            if (parsed && value > 0) {
                System.out.println(prompt + " " + value);
                return value;
            }
            System.out.printf("Invalid argument '%s'. Falling back to input.%n", args[argIndex]);
        }
        return Helpers.readPositiveInt(scanner, prompt);
    }

    public static int getArgOrReadIntInRange(String[] args, int argIndex, Scanner scanner, int min, int max,
            String prompt) {
        if (args.length > argIndex) {
            int value = 0;
            boolean parsed = true;
            try {
                value = Integer.parseInt(args[argIndex]);
            } catch (NumberFormatException e) {
                parsed = false;
            }
            if (parsed && value >= min && value <= max) {
                System.out.println(prompt + " " + value);
                return value;
            }
            System.out.printf("Invalid argument '%s'. Falling back to input.%n", args[argIndex]);
        }
        return Helpers.readIntInRange(scanner, min, max, prompt);
    }

    public static String getArgOrReadGameCode(String[] args, int argIndex, Scanner scanner, GameManager gameManager,
            int length) {
        if (args.length > argIndex) {
            String code = args[argIndex].trim().toUpperCase(Locale.ROOT);
            if (code.matches("[A-Za-z0-9]+") && !gameManager.hasGame(code)) {
                System.out.println("Game code (letters/numbers only) - press enter for random: " + code);
                return code;
            } else {
                System.out.println("Invalid or duplicate code in arguments. Falling back to input.");
            }
        }
        return Helpers.readGameCode(scanner, gameManager, length);
    }

}

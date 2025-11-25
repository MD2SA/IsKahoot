package server.cli.utils;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import server.GameManager;

public class Helpers {

        public static int readPositiveInt(Scanner scanner, String prompt) {
                int value = -1;
                while (value <= 0) {
                        System.out.print(prompt + " "); // print on the same line
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
                } while (gameManager.getGame(id) != null);

                return id;
        }

        public static boolean askYesNo(Scanner scanner, String prompt) {
                while (true) {
                        System.out.print(prompt + " [y]es/[n]o: ");
                        String input = scanner.nextLine().trim().toLowerCase();
                        if (input.equals("y") || input.equals("yes"))
                                return true;
                        else if (input.equals("n") || input.equals("no"))
                                return false;
                        else
                                System.out.println("Please answer 'y' or 'n'.");
                }
        }

        public static String readGameCode(Scanner scanner, GameManager gameManager, int length) {
                boolean wantsCustom = askYesNo(scanner, "Do you want a personalized code for this game?");

                if (!wantsCustom) {
                        String code = generateUniqueRandomCode(length, gameManager);
                        System.out.println("Random code generated: " + code);
                        return code;
                }

                System.out.println("Enter personalized game code (letters/numbers only).");

                for (int attempt = 0; attempt < 2; attempt++) {
                        String input = scanner.nextLine().trim();

                        if (input.isBlank()) {
                                System.out.println("Code cannot be empty.");
                                continue;
                        }
                        if (!input.matches("[A-Za-z0-9]+")) {
                                System.out.println("Only letters and numbers are allowed.");
                                continue;
                        }

                        String code = input.toUpperCase();

                        if (gameManager.getGame(code) != null) {
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

}

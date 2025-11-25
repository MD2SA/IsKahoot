package server.TUI.utils;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Helpers {

        public static void awaitPrompt(Scanner scanner, String command) {
                while (true) {
                        String line = scanner.nextLine();
                        if (line == null)
                                continue;

                        if (line.equals(command))
                                break;

                        if (line.contains(command)) {
                                if (Helpers.askYesNo(scanner, "Did you meant \"" + command + "\"?"))
                                        break;
                        }
                }
        }

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

        private static int ID;

        public static String generateUniqueRandomCode(int length) {
                String chars = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm";
                StringBuilder sb = new StringBuilder();
                sb.append(ID++);

                while (sb.length() < length) {
                        int randomIdx = ThreadLocalRandom.current().nextInt(chars.length());
                        sb.append(chars.charAt(randomIdx));
                }

                return sb.toString();
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

}

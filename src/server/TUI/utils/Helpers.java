package server.TUI.utils;

import java.util.Scanner;

public class Helpers {

        public static int readPositiveInt(Scanner scanner, String prompt) {
                int value = -1;
                while (value <= 0) {
                        System.out.print(prompt + " ");  // print on the same line
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
}

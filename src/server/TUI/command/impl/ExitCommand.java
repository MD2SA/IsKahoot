package server.TUI.command.impl;

import java.util.Scanner;

import server.TUI.command.Command;
import server.TUI.command.CommandContext;

public class ExitCommand implements Command {

        @Override
        public String getName() {
                return "exit";
        }

        @Override
        public String getDescription() {
                return "Exits the server console";
        }

        @Override
        public void execute(Scanner scanner, CommandContext context) {
                System.out.println("Goodbye!");
                System.exit(0);
        }

}


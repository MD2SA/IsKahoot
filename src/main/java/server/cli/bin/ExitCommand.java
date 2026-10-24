package server.cli.bin;

import java.util.Scanner;

import server.Server;
import server.cli.Command;
import server.cli.CommandContext;
import server.cli.utils.Helpers;

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
        public void execute(Scanner scanner, CommandContext context, String[] args) {
                Helpers.printSectionHeader("Goodbye!");
                System.exit(0);
        }

}


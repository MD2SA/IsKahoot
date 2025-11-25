package server.TUI.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import server.TUI.command.impl.*;

public class CommandDispatcher {

        private final Map<String, Command> commands = new HashMap<>();
        private final CommandContext context;

        public CommandDispatcher(CommandContext context) {
                this.context = context;
                registerDefaultCommands();
        }

        private void registerDefaultCommands() {
                register(new ListGamesCommand());
                register(new CreateGameCommand());
                register(new HelpCommand(this));
                register(new ExitCommand());
        }

        public void register(Command command) {
                commands.put(command.getName().toLowerCase(), command);
        }

        public void processLine(Scanner scanner, String line) {
                if (line == null || line.isBlank())
                        return;
                String[] args = line.trim().split("\\s+", 2);
                String cmdName = args[0].toLowerCase();

                Command cmd = commands.get(cmdName);
                if (cmd == null) {
                        System.out.println("Unkown command: " + cmdName + ". Type 'help' for available commands.");
                        return;
                }
                cmd.execute(scanner, context);
        }

        public void showHelp() {
                System.out.println("\n");
                System.out.println("Available commands:");
                commands.values().stream()
                                .forEach(c -> System.out.printf("        %-10s - %s%n", c.getName(),
                                                c.getDescription()));
                System.out.println("\n");
        }

}

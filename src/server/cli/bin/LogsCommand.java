package server.cli.bin;

import java.util.Scanner;
import java.util.List;

import server.Server;
import server.cli.Command;
import server.cli.CommandContext;
import server.cli.utils.Helpers;
import server.utils.Logger;

public class LogsCommand implements Command {
        private int tipCounter = 0;

        @Override
        public String getName() {
                return "logs";
        }

        @Override
        public String getDescription() {
                return "Shows server logs.";
        }

        @Override
        public void execute(Scanner scanner, CommandContext context, String[] args) {
                Logger logger = Server.getInstance().getLogger();

                if (args.length == 0) {
                        Helpers.printSectionHeader("ALL LOGS");
                        printLogs(logger.getAll());
                        if (tipCounter++ % 3 == 0)
                                System.out.println("TIP: Type 'logs -h' to see all available options.\n");
                        return;
                }

                for (String arg : args) {
                        switch (arg) {
                                case "-a", "--all" -> {
                                        Helpers.printSectionHeader("ALL LOGS");
                                        printLogs(logger.getAll());
                                }
                                case "-d", "--debug" -> {
                                        Helpers.printSectionHeader("DEBUG LOGS");
                                        printLogs(logger.getLogs(log -> log.level == Logger.Level.DEBUG));
                                }
                                case "-i", "--info" -> {
                                        Helpers.printSectionHeader("INFO LOGS");
                                        printLogs(logger.getLogs(log -> log.level == Logger.Level.INFO));
                                }
                                case "-e", "--errors" -> {
                                        Helpers.printSectionHeader("WARNINGS + ERRORS");
                                        printLogs(logger.getErrors());
                                }
                                case "-c", "--clear" -> {
                                        logger.clear();
                                        System.out.println("Logs cleared.");
                                }
                                case "-h", "--help", "?" -> help();
                                default -> System.out.println("Unknown option: " + arg + " (use -h for help)");
                        }
                }
        }

        private void printLogs(List<Logger.Log> logs) {
                if (logs.isEmpty()) {
                        System.out.println("    (no logs)\n");
                        return;
                }

                for (Logger.Log log : logs) {
                        String levelStr = switch (log.level) {
                                case DEBUG -> "DEBUG";
                                case INFO -> "INFO";
                                case WARN -> "WARN";
                                case ERROR -> "ERROR";
                        };

                        // Format: [timestamp] [LEVEL] message
                        System.out.printf("[%s] [%s] %s%n",
                                        log.timestamp,
                                        levelStr,
                                        log.message);
                }
                System.out.println();
        }

        private void help() {
                Helpers.printSectionHeader("USAGE");
                System.out.println("""

                                logs [option]

                                OPTIONS
                                -------
                                -a, --all        Show all logs (default when no option is given)
                                -d, --debug      Show only debug-level logs
                                -i, --info       Show only info-level logs
                                -e, --errors     Show warnings and errors only
                                -c, --clear      Clear all logs
                                -h, --help       Show this help message

                                Example: logs -e    -> shows only warnings and errors
                                """);
        }
}

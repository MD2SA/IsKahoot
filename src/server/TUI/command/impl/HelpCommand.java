package server.TUI.command.impl;

import java.util.Scanner;

import server.TUI.command.*;

public class HelpCommand implements Command {

        private final CommandDispatcher dispatcher;

        public HelpCommand(CommandDispatcher dispatcher) {
                this.dispatcher = dispatcher;
        }

        @Override
        public String getName() {
                return "help";
        }

        @Override
        public String getDescription() {
                return "Provides help";
        }

        @Override
        public void execute(Scanner scanner, CommandContext context) {
                dispatcher.showHelp();
        }

}

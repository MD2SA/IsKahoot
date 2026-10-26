package server.cli.bin;

import java.util.Scanner;

import server.cli.Command;
import server.cli.CommandContext;
import server.cli.CommandDispatcher;

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
    public void execute(Scanner scanner, CommandContext context, String[] args) {
        dispatcher.showHelp();
    }

}

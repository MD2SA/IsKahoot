package server.cli.bin;

import java.util.Scanner;

import server.cli.Command;
import server.cli.CommandContext;

public class ClearCommand implements Command {

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "Clears the console screen. May not work in some terminals like Windows CMD.";
    }

    @Override
    public void execute(Scanner scanner, CommandContext context, String[] args) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}

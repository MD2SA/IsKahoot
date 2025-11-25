package server.TUI.command;

import java.util.Scanner;

public interface Command {
        String getName();
        String getDescription();
        void execute(Scanner scanner, CommandContext context);
}

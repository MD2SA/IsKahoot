package server.cli.bin;

import java.util.List;
import java.util.Scanner;

import server.game.GameSession;

import server.cli.Command;
import server.cli.CommandContext;

import server.cli.utils.*;

public class ListGamesCommand implements Command {

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Lists all waiting and active games";
    }

    @Override
    public void execute(Scanner scanner, CommandContext context, String[] args) {
        List<GameSession> games = context.gameManager().getAllGames();

        System.out.println(Constants.MARGIN);

        Helpers.printSectionHeader("Active games:");
        if (games == null || games.size() == 0) {
            System.out.println("    (no games)");
        }
        for (int i = 0; i < games.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, games.get(i).getId());
        }

        System.out.println(Constants.MARGIN);
    }
}

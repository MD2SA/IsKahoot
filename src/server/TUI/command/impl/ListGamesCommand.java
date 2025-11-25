package server.TUI.command.impl;

import java.util.List;
import java.util.Scanner;

import server.game.GameState;

import server.TUI.command.Command;
import server.TUI.command.CommandContext;

import server.TUI.utils.*;

public class ListGamesCommand implements Command {

        @Override
        public String getName() {
                return "list";
        }

        @Override
        public String getDescription() {
                return "Lists all active games";
        }

        @Override
        public void execute(Scanner scanner, CommandContext context) {
                List<GameState> games = context.gameManager().getGames();

                System.out.println(Constants.MARGIN);

                System.out.println("Active games:");
                for (int i = 0; i<games.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, games.get(i).getCode());
                }

                System.out.println(Constants.MARGIN);
        }
}

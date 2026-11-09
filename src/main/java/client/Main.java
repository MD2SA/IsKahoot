package client;

import javax.swing.SwingUtilities;

import client.gui.GUI;

public final class Main {

    private static final int EXPECTED_ARGUMENT_COUNT = 3;

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length > EXPECTED_ARGUMENT_COUNT) {
            System.err.println("args: [username] [teamId] [gameId] (omit any of them to fill them in the window)");
            System.exit(1);
        }

        String username = argument(args, 0);
        String teamId = argument(args, 1);
        String gameId = argument(args, 2);

        SwingUtilities.invokeLater(() -> launch(username, teamId, gameId));
    }

    private static String argument(String[] args, int index) {
        return index < args.length ? args[index].trim() : "";
    }

    private static void launch(String username, String teamId, String gameId) {
        GUI gui = new GUI(username, teamId, gameId);
        gui.setLoginHandler(credentials -> startClient(gui, credentials));

        if (username.isBlank() || teamId.isBlank() || gameId.isBlank()) {
            gui.show();
            return;
        }

        startClient(gui, new ClientCredentials(username, teamId, gameId));
        gui.show();
    }

    private static void startClient(GUI gui, ClientCredentials credentials) {
        gui.showGame(credentials);

        String username = credentials.username();
        Client client = new Client(username, credentials.teamId(), credentials.gameId(), gui);
        gui.registerObserver(client);
        new Thread(client, "client-" + username).start();
    }

}

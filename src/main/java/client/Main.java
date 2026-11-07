package client;

import java.io.IOException;

public final class Main {

    private static final int EXPECTED_ARGUMENT_COUNT = 3;

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != EXPECTED_ARGUMENT_COUNT) {
            System.err.println("args needed: <username> <teamId> <gameId>");
            System.exit(1);
        }

        String username = args[0];
        String teamId = args[1];
        String gameId = args[2];

        if (username.isBlank() || teamId.isBlank() || gameId.isBlank()) {
            System.err.println("Error: arguments cannot be empty or blank");
            System.exit(1);
        }

        new Thread(new Client(username, teamId, gameId), "client-" + username).start();
    }

}

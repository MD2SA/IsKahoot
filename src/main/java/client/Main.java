package client;

import java.io.IOException;

public class Main {

        public static void main(String[] args) throws IOException {
                if (args.length != 3) {
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

                new Client(username, teamId, gameId).start();
        }

}

package server;

import java.io.IOException;

public class Main {

        public static void main(String[] args) throws IOException {

                Server server = Server.getInstance();
                new Thread(() -> {
                        try {

                        server.startServing();
                        } catch (Exception e) {
                                // TODO: handle exception
                        }
                }).start();
                server.createGame();

        }
}

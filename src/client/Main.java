package client;

import server.Server;

public class Main {

        private final String SERVER_IP = "127.0.0.1";
        private final String SERVER_PORT = "8080";

        public static void main(String[] args) {
                Server server = new Server();
                Client client = new Client(server);
        }
}

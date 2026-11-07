package server;

import java.io.IOException;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        Server server = Server.getInstance();
        server.start();
    }
}

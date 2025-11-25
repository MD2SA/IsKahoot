package configs;

public class Config {

        public static class Server {
                public static final String SERVER_IP = "127.0.0.1";
                public static final int SERVER_PORT = 8080;
                public static final String CREATE_GAME_COMMAND = "new";
        }

        public static class Features {
                public static final int ROUND_SECONDS = 3;
        }
}

package configs;

public class Config {

        public static class Server {
                public static final String SERVER_IP = "https://many-mice-draw.loca.lt";
                public static final int SERVER_PORT = 8080;
                public static final String CREATE_GAME_COMMAND = "new";
        }

        public static class Features {
                public static final int ROUND_SECONDS = 20;
                public static final int BONUS_COUNT = 2;
                public static final int BONUS_FACTOR = 2;
        }
}

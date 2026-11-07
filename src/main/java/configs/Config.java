package configs;

// Config is a namespace for the nested configuration groups.
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class Config {

    private Config() {
    }

    public static final class Server {

        private Server() {
        }

        @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
        public static final String SERVER_IP = "127.0.0.1";
        public static final int SERVER_PORT = 8080;
        public static final String CREATE_GAME_COMMAND = "new";
        public static final Integer MAX_GAMES = 5;
    }

    public static final class Features {

        private Features() {
        }

        public static final int ROUND_SECONDS = 40;
        public static final int BONUS_COUNT = 2;
        public static final int BONUS_FACTOR = 2;
    }
}

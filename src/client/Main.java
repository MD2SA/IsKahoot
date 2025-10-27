package client;
public class Main {

        private final String SERVER_IP = "127.0.0.1";
        private final String SERVER_PORT = "8080";

        public static void main(String[] args) {
                for( int i = 0; i<2; i++) {
                        Client client = new Client();
                }
        }
}

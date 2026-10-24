package server;

import server.utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import configs.Config;

public class Server {

        private static final Server INSTANCE = new Server();

        private final GameManager gameManager;
        private final Logger logger;

        private Server() {
                try {
                        this.logger = new Logger();
                        this.gameManager = new GameManager();
                        gameManager.start();
                } catch (IOException e) {
                        throw new RuntimeException("Failed to initialize server", e);
                }
        }

        public static Server getInstance() {
                return INSTANCE;
        }

        public Logger getLogger() {
                return logger;
        }

        public GameManager getGameManager() {
                return gameManager;
        }

        public void start() throws IOException {
                logger.info("Server starting on port: " + Config.Server.SERVER_PORT);

                try (ServerSocket ss = new ServerSocket(Config.Server.SERVER_PORT)) {
                        while (true) {
                                Socket socket = ss.accept();
                                logger.info("New connection from " + socket.getRemoteSocketAddress());
                                new Thread(new DealWithClient(this, socket)).start();
                        }
                } catch (IOException e) {
                        String message = e.getMessage();
                        if (message == null || !message.contains("socket closed"))
                                logger.error("Server socket error: " + e);
                } finally {
                        shutdown();
                }
        }

        private void shutdown() {
                logger.info("Shutting down server...");
                gameManager.shutdown();
        }

}

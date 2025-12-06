package server;

import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import server.game.GameState;
import server.game.utils.Score;
import utils.messages.Code;
import utils.messages.Message;
import utils.messages.MessageFromClient;
import utils.messages.MessageFromServer;
import utils.quiz.Question;

public class DealWithClient implements Runnable, AutoCloseable {

        private static final int MAX_FAILS = 3;

        private final Server server;
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        private String clientId;
        private String teamId;
        private GameState game;
        private int roundNumber;

        public DealWithClient(Server server, Socket socket) throws IOException {
                this.server = server;
                this.socket = socket;
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
                try {
                        if (!registerClient()) {
                                server.getLogger().info("Registration failed for client " + clientId);
                                sendError("Registration failed");
                                return;
                        }
                        serve();
                } catch (IOException e) {
                        server.getLogger().warn("Client disconnected from excepion: " + clientId);
                } finally {
                        cleanup();
                }
        }

        private boolean registerClient() throws IOException {
                MessageFromClient msg = readMessage();
                if (msg == null || msg.getCode() != Code.REGISTER)
                        return false;

                this.clientId = msg.getClientId();
                this.teamId = msg.getTeamId();

                GameState game = server.getGameManager().getGame(msg.getGameId());
                if (game == null) {
                        server.getLogger().debug("Game does not exist to register" + msg.getGameId());
                        return false;
                }
                this.game = game;

                return game.registerPlayer(clientId, teamId, this);
        }

        private void serve() {
                while (!Thread.currentThread().isInterrupted()) {
                        MessageFromClient msg = readMessage();
                        if (msg == null) {
                                server.getLogger().info("Message from client: " + clientId + " was null.");
                                break;
                        }

                        if (!clientId.equals(msg.getClientId())) {
                                server.getLogger().info("Message from client was invalid");
                                sendError("Invalid client ID");
                                break;
                        }

                        if (msg.getCode() == Code.ANSWER)
                                this.game.answer(clientId, msg.getRoundNumber(), msg.getAnswer());
                }
        }

        public void sendRoundInfo(Question question, List<Score> scoreboard) {
                send(MessageFromServer.updateMessage(roundNumber++, question, scoreboard));
        }

        public void sendEndGame(List<Score> scoreboard) {
                send(MessageFromServer.endMessage(scoreboard));
        }

        private void sendError(String reason) {
                send(new Message(Code.ERROR, reason));
        }

        private MessageFromClient readMessage() {
                try {
                        return (MessageFromClient) in.readObject();
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                        return null;
                }
        }

        private void send(Message msg) {
                for (int i = 0; i < MAX_FAILS; i++) {
                        try {
                                synchronized (out) {
                                        out.writeObject(msg);
                                        out.flush();
                                        out.reset();
                                }
                                return;
                        } catch (Exception e) {
                                server.getLogger().warn(
                                                "Failed to send message (attempt " + (i + 1) + ") to " + clientId);
                        }
                }
                server.getLogger().warn("Failed to send message after " + MAX_FAILS + " attempts");
        }

        private void cleanup() {
                if (game != null)
                        game.unregisterPlayer(clientId);
                closeQuietly(socket, in, out);
        }

        private void closeQuietly(AutoCloseable... closeables) {
                for (AutoCloseable c : closeables) {
                        try {
                                if (c != null)
                                        c.close();
                        } catch (Exception e) { // error ignored
                        }
                }
        }

        @Override
        public void close() {
                cleanup();
        }

        public String getClientId() {
                return clientId;
        }

        public String getTeamId() {
                return teamId;
        }

}

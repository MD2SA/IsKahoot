package server;

import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import server.game.utils.Score;
import utils.Code;

public class DealWithClient extends Thread {

        private static final int MAX_FAILS = 3;

        private final Server server;
        private Socket socket;

        private int clientId = -1;
        private int gameId = -1;
        ObjectOutputStream out;
        ObjectInputStream in;

        public DealWithClient(Server server, Socket socket) {
                this.server = server;
                this.socket = socket;
        }

        @Override
        public void run() {
                try (Socket connection = socket) {
                        doConnections();
                        boolean okRegister = registerClient();
                        if (!okRegister) {
                                sendError();
                                return;
                        }
                        serve();
                } catch (IOException e) {
                } finally {
                        server.close(this);
                }

        }

        private void doConnections() throws IOException {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
        }

        private boolean registerClient() throws IOException {
                MessageFromClient msg;
                int tries = 0;
                do {
                        msg = readMessage(in);
                        if (tries++ > MAX_FAILS)
                                return false;
                } while (msg == null || isInvalid(msg));

                this.clientId = msg.getClientId();
                this.gameId = msg.getGameId();
                return server.registerPlayer(this);
        }

        private void serve() {
                while (true) {
                        MessageFromClient msg = readMessage(in);
                        if (msg == null || isInvalid(msg))
                                continue;
                        Code code = msg.getCode();
                        if (code == Code.POST_ANSWER)
                                server.answer(this, msg.getAnswer());
                }
        }

        void sendScoreboard(List<Score> scoreboard) {
                MessageFromServer msg = new MessageFromServer(scoreboard);
                sendWithRetries(msg, MAX_FAILS);
        }

        private MessageFromClient readMessage(ObjectInputStream in) {
                try {
                        return (MessageFromClient) in.readObject();
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                        return null;
                }
        }

        private void sendError() throws IOException {
                Message msg = new Message(Code.ERROR);
                sendWithRetries(msg, MAX_FAILS);
        }

        private boolean isInvalid(MessageFromClient msg) {
                if (clientId != -1 && clientId != msg.getClientId())
                        return true;
                if (gameId != -1 && gameId != msg.getGameId())
                        return true;
                return false;
        }

        private void sendWithRetries(Message msg, int maxTries) {
                for (int attempts = 0; attempts < maxTries; attempts++) {
                        try {
                                synchronized (out) {
                                        out.writeObject(msg);
                                        out.flush();
                                }
                                return;
                        } catch (Exception e) {
                                System.out.println(attempts + "º attempt to send message");
                        }
                }
                System.out.println("Failed to send message after " + maxTries + " attempts");
        }

        public int getGameId() {
                return gameId;
        }

        public int getClientId() {
                return gameId;
        }

        // @Override
        // public boolean equals(Object o) {
        // if (this == o)
        // return true;
        // if (!(o instanceof DealWithClient))
        // return false;
        // DealWithClient other = (DealWithClient) o;
        // return clientId == other.clientId;
        // }
        //
        // @Override
        // public int hashCode() {
        // return Integer.hashCode(clientId);
        // }

}

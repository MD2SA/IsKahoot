package server;

import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import server.game.utils.Score;
import utils.messages.Code;
import utils.messages.Message;
import utils.messages.MessageFromClient;
import utils.messages.MessageFromServer;
import utils.quiz.Question;

public class DealWithClient extends Thread {

        private static final int MAX_FAILS = 3;

        private final Server server;
        private Socket socket;

        private String clientId;
        private String gameId;
        private String teamId;

        private ObjectOutputStream out;
        private ObjectInputStream in;

        public DealWithClient(Server server, Socket socket) {
                this.server = server;
                this.socket = socket;
        }

        @Override
        public void run() {
                try {

                        doConnections();
                        boolean okRegister = registerClient();
                        if (!okRegister) {
                                server.getLogger().debug("DID NOT REGISTER");
                                sendError();
                                return;
                        }
                        server.getLogger().debug("registered client" + clientId);
                        Thread receiver = new Thread(() -> {
                                serve();
                        });
                        receiver.start();
                        receiver.join();
                } catch (IOException | InterruptedException e) {
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
                this.teamId = msg.getTeamId();
                return server.registerPlayer(this);
        }

        private void serve() {
                while (true) {
                        MessageFromClient msg = readMessage(in);
                        if (msg == null || isInvalid(msg))
                                continue;
                        Code code = msg.getCode();
                        if (code == Code.POST_ANSWER) {
                                server.answer(this, msg.getAnswer());
                        }
                }
        }

        void sendQuestion(Question question) {
                MessageFromServer msg = new MessageFromServer(question);
                server.getLogger().debug("Sending question" + question.getQuestion());
                sendWithRetries(msg, MAX_FAILS);
        }

        void sendScoreboard(List<Score> scoreboard) {
                MessageFromServer msg = new MessageFromServer(scoreboard);
                server.getLogger().debug("Sending scoreboard");
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
                server.getLogger().debug("Sending error message");
                sendWithRetries(msg, MAX_FAILS);
        }

        /**
         * ATENCAO QUE ESTA LOGICA NAOE STA BEM
         */
        private boolean isInvalid(MessageFromClient msg) {
                if (clientId != null && !clientId.equals(msg.getClientId())) {
                        server.getLogger().debug("MESSAGE CONSIDERED INVALID" + msg + "\nCLIENTID WAS:" + clientId);
                        return true;
                }
                return false;
        }

        private void sendWithRetries(Message msg, int maxTries) {
                for (int attempts = 0; attempts < maxTries; attempts++) {
                        try {
                                synchronized (out) {
                                        out.writeObject(msg);
                                        out.flush();
                                        out.reset();
                                }
                                return;
                        } catch (Exception e) {
                                server.getLogger().error(attempts + "º attempt to send message");
                                // e.printStackTrace();
                        }
                }
                server.getLogger().warn("Failed to send message after " + maxTries + " attempts");
        }

        public String getGameId() {
                return gameId;
        }

        public String getClientId() {
                return clientId;
        }

        public String getTeamId() {
                return teamId;
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

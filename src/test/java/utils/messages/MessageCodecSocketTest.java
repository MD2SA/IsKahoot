package utils.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.jupiter.api.Test;

class MessageCodecSocketTest {

    @Test
    void exchangesTypedMessagesAcrossALoopbackSocket() throws Exception {
        InetAddress loopback = InetAddress.getLoopbackAddress();
        try (ServerSocket listener = new ServerSocket(0, 1, loopback);
                Socket client = new Socket(loopback, listener.getLocalPort());
                Socket server = listener.accept();
                DataInputStream clientInput = new DataInputStream(client.getInputStream());
                DataOutputStream clientOutput = new DataOutputStream(client.getOutputStream());
                DataInputStream serverInput = new DataInputStream(server.getInputStream());
                DataOutputStream serverOutput = new DataOutputStream(server.getOutputStream())) {

            MessageCodec.write(serverOutput, new ServerMessage.Error("Registration failed"));
            Object response = MessageCodec.read(clientInput, ProtocolObjectFilters.serverToClient());
            assertInstanceOf(ServerMessage.Error.class, response);
            assertEquals("Registration failed", ((ServerMessage.Error) response).reason());

            MessageCodec.write(clientOutput, new ClientMessage.SubmitAnswer(2, 7));
            Object request = MessageCodec.read(serverInput, ProtocolObjectFilters.clientToServer());
            assertEquals(new ClientMessage.SubmitAnswer(2, 7), request);
        }
    }
}

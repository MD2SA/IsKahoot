package testutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import server.Server;

public final class ServerTestSupport {

    private ServerTestSupport() {
    }

    @SuppressWarnings("PMD.CloseResource") // Streams are global singletons that are restored, not closed.
    public static Server initializeWithoutConsole() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        PrintStream sink = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);

        System.setIn(new ByteArrayInputStream(new byte[0]));
        System.setOut(sink);
        System.setErr(sink);
        try {
            Server server = Server.getInstance();
            awaitGameManagerStop();
            return server;
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    public static void awaitGameManagerStop() {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (gameManagerThreadAlive() && System.nanoTime() < deadline)
            Thread.onSpinWait();
    }

    private static boolean gameManagerThreadAlive() {
        return Thread.getAllStackTraces().keySet().stream()
                .anyMatch(thread -> "game-manager".equals(thread.getName()) && thread.isAlive());
    }
}

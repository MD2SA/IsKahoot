package testutils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public final class OutputCapture {

    private OutputCapture() {
    }

    @SuppressWarnings("PMD.CloseResource") // The original stream is restored, never closed.
    public static <T> Captured<T> capture(Supplier<T> action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            T value = action.get();
            return new Captured<>(value, buffer.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(original);
        }
    }

    public record Captured<T>(T value, String output) {
    }
}

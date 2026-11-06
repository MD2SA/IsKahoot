package utils.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.io.Serializable;

public final class MessageCodec {

    public static final int MAX_FRAME_BYTES = 16_777_216;

    private MessageCodec() {
    }

    public static void write(DataOutputStream output, Serializable message) throws IOException {
        BoundedBuffer buffer = new BoundedBuffer();
        try (ObjectOutputStream objectOutput = new ObjectOutputStream(buffer)) {
            objectOutput.writeObject(message);
        }

        byte[] frame = buffer.toByteArray();
        if (frame.length == 0 || frame.length > MAX_FRAME_BYTES)
            throw new IOException("Serialized message exceeds the maximum frame size");

        output.writeInt(frame.length);
        output.write(frame);
        output.flush();
    }

    public static Object read(DataInputStream input, ObjectInputFilter filter)
            throws IOException, ClassNotFoundException {
        int frameLength = input.readInt();
        if (frameLength <= 0 || frameLength > MAX_FRAME_BYTES)
            throw new StreamCorruptedException("Invalid message frame length: " + frameLength);

        byte[] frame = new byte[frameLength];
        input.readFully(frame);

        try (ObjectInputStream objectInput = new ObjectInputStream(new ByteArrayInputStream(frame))) {
            objectInput.setObjectInputFilter(filter);
            Object message = objectInput.readObject();
            try {
                objectInput.readObject();
                throw new StreamCorruptedException("Message frame contains multiple objects");
            } catch (EOFException expectedEndOfFrame) {
                return message;
            }
        }
    }

    private static final class BoundedBuffer extends OutputStream {

        private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();
        private int size;

        @Override
        public void write(int value) throws IOException {
            ensureCapacity(1);
            delegate.write(value);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            ensureCapacity(length);
            delegate.write(bytes, offset, length);
        }

        private void ensureCapacity(int length) throws IOException {
            if (length < 0 || size + length > MAX_FRAME_BYTES)
                throw new IOException("Serialized message exceeds the maximum frame size");
            size += length;
        }

        private byte[] toByteArray() {
            return delegate.toByteArray();
        }
    }
}

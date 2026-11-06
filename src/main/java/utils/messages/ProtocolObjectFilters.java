package utils.messages;

import java.io.ObjectInputFilter;
import java.util.Set;

import utils.quiz.ReducedQuestion;
import utils.quiz.Score;

public final class ProtocolObjectFilters {

    private static final long MAX_STREAM_BYTES = MessageCodec.MAX_FRAME_BYTES;
    private static final long MAX_REFERENCES = 100_000;
    private static final long MAX_ARRAY_LENGTH = 100_000;

    private static final Set<Class<?>> CLIENT_TO_SERVER = Set.of(ClientMessage.Register.class,
            ClientMessage.SubmitAnswer.class, String.class);

    private static final Set<Class<?>> SERVER_TO_CLIENT = Set.of(ServerMessage.Question.class,
            ServerMessage.Scoreboard.class, ServerMessage.AnswerAccepted.class, ServerMessage.AnswerRejected.class,
            ServerMessage.End.class, ServerMessage.Error.class, ReducedQuestion.class, Score.class, String.class);

    private ProtocolObjectFilters() {
    }

    public static ObjectInputFilter clientToServer() {
        return filter(CLIENT_TO_SERVER);
    }

    public static ObjectInputFilter serverToClient() {
        return filter(SERVER_TO_CLIENT);
    }

    private static ObjectInputFilter filter(Set<Class<?>> allowedClasses) {
        return info -> {
            if (info.depth() > 16 || info.references() > MAX_REFERENCES || info.streamBytes() > MAX_STREAM_BYTES
                    || (info.arrayLength() >= 0 && info.arrayLength() > MAX_ARRAY_LENGTH))
                return ObjectInputFilter.Status.REJECTED;

            Class<?> type = info.serialClass();
            if (type == null)
                return ObjectInputFilter.Status.UNDECIDED;

            if (allowedClasses.contains(type) || type.isPrimitive()
                    || isSupportedCollectionSerialization(type, allowedClasses))
                return ObjectInputFilter.Status.ALLOWED;

            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                boolean protocolArray = componentType == String.class && allowedClasses.contains(String.class)
                        || componentType == Score.class && allowedClasses.contains(Score.class)
                        || componentType == Object.class && allowedClasses.contains(Score.class);
                if (protocolArray)
                    return ObjectInputFilter.Status.ALLOWED;
            }

            return ObjectInputFilter.Status.REJECTED;
        };
    }

    private static boolean isSupportedCollectionSerialization(Class<?> type, Set<Class<?>> allowedClasses) {
        if (!allowedClasses.contains(Score.class) && !allowedClasses.contains(ReducedQuestion.class))
            return false;

        return switch (type.getName()) {
            case "java.util.CollSer", "java.util.ImmutableCollections$List12", "java.util.ImmutableCollections$ListN" ->
                true;
            default -> false;
        };
    }
}

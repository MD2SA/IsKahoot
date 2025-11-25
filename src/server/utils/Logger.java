package server.utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.ArrayList;

public class Logger {

        private static final int MAX_LOGS = 100;

        private final List<Log> logs = new ArrayList<>();

        public Logger() {
        }

        public static enum Level {
                DEBUG, INFO, WARN, ERROR
        }

        public class Log {

                public final Level level;
                public final String message;
                public final LocalDateTime timestamp;

                Log(Level level, String message) {
                        this.level = level;
                        this.message = message;
                        this.timestamp = LocalDateTime.now();
                }

                @Override
                public String toString() {
                        return String.format("[%s] %-5s %s",
                                        timestamp.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                                        level, message);
                }
        }

        public List<Log> getLogs(Predicate<Log> predicate) {
                synchronized (logs) {
                        return logs.stream().filter(predicate).toList();
                }
        }

        public void add(Log log) {
                synchronized (logs) {
                        logs.add(log);
                        if (logs.size() > MAX_LOGS)
                                logs.removeFirst();
                }
        }

        public void debug(String msg) {
                add(new Log(Level.DEBUG, msg));
        }

        public void info(String msg) {
                add(new Log(Level.INFO, msg));
        }

        public void warn(String msg) {
                add(new Log(Level.WARN, msg));
        }

        public void error(String msg) {
                add(new Log(Level.ERROR, msg));
        }

        public List<Log> getAll() {
                return List.copyOf(logs);
        }

        public List<Log> getDebug() {
                return getLogs(e -> e.level == Level.DEBUG);
        }

        public List<Log> getInfo() {
                return getLogs(e -> e.level == Level.INFO);
        }

        public List<Log> getErrors() {
                return getLogs(e -> e.level == Level.WARN || e.level == Level.ERROR);
        }

        public void clear() {
                logs.clear();
        }
}

# Architecture

## Overview

```text
            TCP + Java serialization
 Client  <-------------------------->  Server
 (Swing)                               |
                                       +-- GameManager
                                       |     +-- waiting games
                                       |     +-- active games
                                       |     +-- ThreadPool (MAX_GAMES)
                                       |
                                       +-- DealWithClient (one thread per connection)
                                       |
                                        +-- GameSession
                                              +-- TeamManager
                                              +-- RoundManager
                                                    +-- RoundState (per question)
                                                    +-- RoundTimer
```

## Server

`Server` is a singleton that owns the `Logger`, the `GameManager` and the listening socket. On
startup it launches the game manager thread (which serves the console CLI) and then accepts
connections forever. Every accepted socket is handed to a new `DealWithClient` instance running on
its own thread.

`GameManager` owns:

- the quiz catalogue, loaded from the `quizzes.json` classpath resource at startup;
- a map of waiting games (lobbies that are not full yet);
- a map of active games;
- a `ThreadPool` sized by `Config.Server.MAX_GAMES`.

When a player registers, `GameManager` finds the lobby, asks the session to add the player, and
starts the game in the pool if the lobby is now full. The game is moved from the waiting map to
the active map for the duration of its run.

`DealWithClient` owns the socket streams for one player. It performs registration, then loops
reading answer messages. All writes are serialised on the output stream so that game threads and
the connection thread can both send safely. Failures to send are retried a fixed number of times
before the connection is considered dead.

## Session

`GameSession` is the runnable task submitted to the pool. Once the lobby is full, its loop is:

1. snapshot the current roster and prepare the next question;
2. open the round and start the authoritative server deadline;
3. broadcast the reduced question, including that deadline;
4. wait for all participants to answer, leave, or for the deadline to expire;
5. broadcast the updated scoreboard, then repeat until the quiz ends or no clients remain;
6. send the end message and close the connections.

`TeamManager` keeps the registered usernames and teams, enforcing positive capacities, team limits,
non-blank identities, and unique usernames. Its collection accessors return snapshots rather than
mutable backing collections.

## Round synchronisation

`RoundManager` coordinates each question through a `RoundState` and a `RoundTimer`.

- **Round state** moves once from prepared to open to closed. Answers must match the registered
  username/team and current round; duplicates, departed players, and late answers are rejected.
- **Round end** occurs when every roster member has answered or left, or when the deadline expires.
  A player who leaves without answering counts as missing for scoring; the round need not wait for
  that player's timer to expire.
- **Individual scoring** doubles the first `BONUS_COUNT` accepted answers, whether or not those
  answers are correct. This is the configured speed-bonus rule.
- **Team scoring** awards the best correct answer. The team score is doubled only if every player
  in the round's roster snapshot answered correctly; a missing/departed answer prevents the bonus.
- **Deadline display** uses the server's absolute deadline carried with the reduced question, so the
  client countdown tracks the same round cutoff as the server.
- **Scoreboard initialization** happens from the first round's roster snapshot, after lobby
  registration, so teams with zero points remain visible.

## Client

`Client` is a thread that connects to the server and performs the request/response loop with the
GUI. `GUI` is a Swing frame wrapped in a small observer implementation: when the player selects
an option, the GUI notifies the client, which sends the answer and shows a waiting screen until
the next update arrives.

## Concurrency utilities

`utils.tools` intentionally avoids `java.util.concurrent`:

| Class | Purpose |
| --- | --- |
| `BlockingQueue` | Linked queue with `put`/`take` blocking semantics |
| `ThreadPool` | Fixed workers consuming from a `BlockingQueue` |
| `ConcurrentMap` / `ConcurrentSet` | Synchronized wrappers around `HashMap` / `HashSet` |
| `ModifiedCountdownLatch` | Latch with a timeout and a scoring bonus per arrival |
| `ModifiedCyclicBarrier` | Team rendezvous with an action and forced release |
| `Timer` | Countdown thread with an optional per-second callback |

## Lifecycle and failure handling

- A client that disconnects is removed from its session and from its team; an unanswered active
  round entry is marked missing so the round can resolve without waiting for timeout.
- Broadcasts use a snapshot of the current client set; connection cleanup removes departed clients.
- An empty session ends its loop after the active round resolves and sends the end message to any
  remaining connections.
- `GameManager.shutdown()` drains the task queue and interrupts the workers.

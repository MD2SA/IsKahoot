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
                                                   +-- ModifiedCountdownLatch
                                                   +-- ModifiedCyclicBarrier (per team)
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

`GameSession` is the runnable task submitted to the pool. Its loop is:

1. take the next question from `RoundManager`;
2. broadcast the reduced question plus the current scoreboard;
3. wait for the round to end;
4. repeat until the quiz is exhausted or every client has left;
5. broadcast the final scoreboard and close the connections.

`TeamManager` keeps the registered usernames and the teams, enforcing team capacity and rejecting
duplicate usernames.

## Round synchronisation

`RoundManager` is the concurrency core of the game.

- **Round end** is decided by a `ModifiedCountdownLatch`, a hand-written latch with a built-in
  timer. It is created with the number of active players, the bonus count and factor, and the
  round duration. `await()` returns when every player has answered or the timer expires.
- **Scoring** uses the same latch: `countdown()` returns the bonus multiplier for that answer, so
  the first `BONUS_COUNT` answers earn `BONUS_FACTOR × points`.
- **Team questions** add a `ModifiedCyclicBarrier` per team. Each member's answer thread waits at
  the barrier; when the last member arrives (or the round timer force-releases it), the barrier
  action compares the team's answers. The best correct answer scores, and the score is doubled
  when every member answered correctly.
- **Late answers** are rejected by validating the round number and by a `roundReady` flag that is
  cleared before the barriers are released. `activeAnswerCount` makes the round wait until
  in-flight answer handlers finish before the next broadcast.

The barrier is single-use: a fresh barrier is created for every team question and every round.

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

- A client that disconnects is removed from its session and from its team.
- Broadcasts to dead clients are removed from the session's client set.
- An empty session ends its loop and broadcasts the final scoreboard.
- `GameManager.shutdown()` drains the task queue and interrupts the workers.

# IsKahoot

A Kahoot-style, team-based quiz game written in Java. A console server hosts concurrent quiz
sessions over TCP sockets while each player joins through a Swing client, answers rounds against
a countdown, and watches the scoreboard update in real time.

The project began as a concurrent-programming course assignment, so the interesting part is under
the hood: client handling, round synchronisation, team barriers and scoring are implemented with
hand-written concurrency primitives rather than `java.util.concurrent`.

## Features

- Multiple quiz sessions hosted by a single server, each running in its own pooled task.
- Team-based lobbies: a game starts automatically when every seat is filled.
- Individual and team questions, with a per-round countdown.
- Speed bonus for the first correct answers and a team bonus when every member answers correctly.
- Live scoreboard, end-of-game screen, and a server-side log console with filtering.
- Quizzes are plain JSON and can be edited without touching the code.

## Requirements

- JDK 21 or newer
- No local Maven installation is needed — the Maven wrapper is included

## Quick start

Build the project once:

```bash
./mvnw package
```

Start the server:

```bash
./scripts/run.sh server
```

The server reads commands from standard input. Create a game, then start clients:

```text
> new
```

```bash
./scripts/run.sh client alice 0 ABCD
./scripts/run.sh client bob 0 ABCD
./scripts/run.sh client carol 1 ABCD
./scripts/run.sh client dave 1 ABCD
```

Client arguments are `<username> <team-id> <game-code>`. The game starts as soon as the lobby is
full (in this example, two teams of two).

To launch a set of clients automatically, use the simulation script:

```bash
./scripts/simulate.sh 2 2 ABCD
```

It builds once, starts the requested number of clients, writes their output to
`target/sim-logs/`, and stops them all when you press Enter.

## Server commands

| Command | Description |
| --- | --- |
| `new [teams] [players-per-team] [quiz] [code]` | Creates a game; missing arguments are prompted for |
| `list` | Lists waiting and active games |
| `logs [-a\|-d\|-i\|-e\|-c\|-h]` | Shows or clears server logs |
| `clear` | Clears the terminal |
| `help` | Lists available commands |
| `exit` | Stops the server |

## Project layout

```text
src/main/java/
  client/            Swing client and its observer wiring
  server/            Socket server, game sessions, CLI
  configs/           Port, round duration and scoring constants
  utils/             Quiz model, wire messages, concurrency utilities
src/main/resources/
  quizzes.json       Quiz catalogue loaded at startup
src/test/java/       Unit tests
scripts/             Development helpers
docs/                Architecture and protocol notes
```

## How a round works

1. The server broadcasts the question and the current scoreboard.
2. Each player has `ROUND_SECONDS` seconds to answer.
3. Individual questions award `points` multiplied by a speed bonus; the first
   `BONUS_COUNT` answers receive `BONUS_FACTOR`.
4. Team questions wait for every team member through a barrier. The team scores its best correct
   answer, doubled when all members are correct.
5. When every player has answered — or the timer expires — the next round begins.

See [docs/architecture.md](docs/architecture.md) for the threading model and
[docs/protocol.md](docs/protocol.md) for the wire protocol.

## Quiz format

Quizzes live in `src/main/resources/quizzes.json` and are packaged into the application:

```json
{
  "quizzes": [
    {
      "name": "Example quiz",
      "questions": [
        {
          "question": "Which method starts a Java thread?",
          "points": 3,
          "correct": 1,
          "isIndividual": true,
          "options": ["run()", "start()", "execute()", "begin()"]
        }
      ]
    }
  ]
}
```

`correct` is the zero-based index of the right option.

## Configuration

Defaults are in `src/main/java/configs/Config.java`:

| Setting | Default |
| --- | --- |
| `SERVER_PORT` | `8080` |
| `MAX_GAMES` | `5` concurrent games |
| `ROUND_SECONDS` | `40` |
| `BONUS_COUNT` / `BONUS_FACTOR` | `2` / `2` |

## Tests

```bash
./mvnw verify
```

## Known limitations

- The client connects to the local machine only; the server address is not configurable yet.
- A client re-sends its registration before each update, which doubles as a "ready for the next
  question" signal.
- There is no reconnection: leaving a game closes the client.
- Scores are kept in memory and disappear when the server stops.

## License

[MIT](LICENSE)

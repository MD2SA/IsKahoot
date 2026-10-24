# Wire protocol

The client and server exchange Java objects over a TCP socket using
`java.io.ObjectOutputStream` / `ObjectInputStream`. Every message extends `Message` and carries a
`Code`:

| Code | Direction | Purpose |
| --- | --- | --- |
| `REGISTER` | client → server | Join a game with a username, team and game code |
| `UPDATE` | server → client | New question and current scoreboard |
| `ANSWER` | client → server | Submit an option for the current round |
| `END` | server → client | Final scoreboard; the game is over |
| `ERROR` | server → client | Registration or validation failure |

## Message types

```text
Message
  code : Code
  info : String

MessageFromClient extends Message
  clientId : String
  gameId   : String
  teamId   : String
  answer   : Answer

MessageFromServer extends Message
  question   : ReducedQuestion
  scoreboard : List<Score>
```

`Answer` contains `teamId`, `username`, `option` and `roundNumber`.

`ReducedQuestion` is the client-safe view of a question. It deliberately omits `correct`, so the
right answer never reaches the client:

```text
ReducedQuestion
  question     : String
  points       : int
  isIndividual : boolean
  options      : List<String>
  roundNumber  : int
```

`Score` is an interface implemented by `Team`, exposing `name` and `score`. The scoreboard sent to
clients is therefore a serialized list of teams.

## Flow

```text
client                                server
  |  REGISTER(username, team, game)     |
  |------------------------------------>|  DealWithClient.registerClient()
  |                                     |  GameManager.registerToGame()
  |                                     |  GameSession.join()
  |                                     |
  |  UPDATE(question, scoreboard)       |  (when the lobby is full)
  |<------------------------------------|
  |  ANSWER(option, round)              |
  |------------------------------------>|  RoundManager.answer()
  |                                     |
  |  UPDATE(next question, scoreboard)  |  (when the round ends)
  |<------------------------------------|
  |                 ...                 |
  |  END(scoreboard)                    |
  |<------------------------------------|
```

## Notes

- Rounds are zero-based on the server. The first question is round `0`, which is why an unanswered
  registration uses `-1`.
- The client re-sends its registration before each update. The server treats a message as an
  answer only when its code is `ANSWER`, so the extra registration is effectively a keep-alive.
- Registration failures are reported with an `ERROR` message and the connection is closed by the
  server.
- `ObjectOutputStream.reset()` is called after every write so that changed objects are not sent as
  back-references.

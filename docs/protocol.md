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
  deadlineEpochMillis : long
```

The deadline is the server's absolute round deadline; clients use it for the visible countdown.
`Score` is a serializable value containing a team ID and points. Scoreboard messages carry a
serialized `Scoreboard`, exposed to the client as a sorted list of `Score` values.

## Flow

```text
client                                server
  |  REGISTER(username, team, game)     |
  |------------------------------------>|  DealWithClient.registerClient()
  |                                     |  GameManager.registerToGame()
  |                                     |  GameSession.join()
  |                                     |
  |  QUESTION(question, deadline)        |  (when the lobby is full)
  |<------------------------------------|
  |  ANSWER(option, round)              |
  |------------------------------------>|  RoundManager.answer()
  |                                     |
  |  SCOREBOARD(scoreboard)              |  (when the round ends)
  |  QUESTION(next question, deadline)   |  (for the next round)
  |<------------------------------------|
  |                 ...                 |
  |  SCOREBOARD(final scoreboard)        |
  |  END                                 |
  |<------------------------------------|
```

## Notes

- Rounds are zero-based on the server. The first question is round `0`, which is why an unanswered
  registration uses `-1`.
- The username and team carried in an answer must match the connection's registered identity.
- An answer received before a round opens is ignored; invalid or late answers do not terminate the
  connection.
- Registration failures are reported with an `ERROR` message and the connection is closed by the
  server.
- `ObjectOutputStream.reset()` is called after every write so that changed objects are not sent as
  back-references.

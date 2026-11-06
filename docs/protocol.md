# Wire protocol

The client and server exchange typed Java records over TCP. Each object is serialized into an
independent frame with a four-byte length prefix, then deserialized with a fresh
`ObjectInputStream`. Frames are limited to 16 MiB. The wire types are directional sealed
interfaces: `ClientMessage` for requests and `ServerMessage` for responses. Each frame gets a
direction-specific `ObjectInputFilter`, so byte/reference limits apply per message rather than
accumulating over the whole connection.

## Message types

`ClientMessage` variants:

- `Register(clientId, teamId, gameId, protocolVersion)` joins a game.
- `SubmitAnswer(option, roundNumber)` submits an answer for the registered connection. Identity is
  taken from registration rather than repeated in every answer.

`ServerMessage` variants:

- `Question(ReducedQuestion)` starts a round.
- `Scoreboard(List<Score>)` publishes the standings.
- `AnswerAccepted(roundNumber)` acknowledges a valid answer; for the last answer, it may race with
  the scoreboard response, and clients ignore acknowledgements for rounds that have already ended.
- `AnswerRejected(roundNumber, reason)` explains a rejected answer.
- `End` ends the game; `Error(reason)` reports a terminal connection/protocol error.

The internal server-side `Answer` includes the registered username and team, which the server adds
after receiving a `SubmitAnswer`.

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
`Score` is a serializable value containing a team ID and points. Scoreboard messages carry an
immutable sorted list of `Score` values.

## Flow

```text
client                                server
  |  Register(username, team, game, v)   |
  |------------------------------------>|  DealWithClient.registerClient()
  |                                     |  GameManager.registerToGame()
  |                                     |  GameSession.join()
  |                                     |
  |  Question(question, deadline)        |  (when the lobby is full)
  |<------------------------------------|
  |  SubmitAnswer(option, round)          |
  |------------------------------------>|  RoundManager.answer()
  |                                     |
  |  AnswerAccepted(round)               |  (after the server accepts the answer)
  |<------------------------------------|
  |  Scoreboard(scores)                  |  (when the round ends)
  |<------------------------------------|
  |  Question(next question, deadline)   |  (for the next round)
  |<------------------------------------|
  |                 ...                 |
  |  Scoreboard(final scores)            |
  |  End                                 |
  |<------------------------------------|
```

## Notes

- Rounds are zero-based on the server. The first question is round `0`.
- `protocolVersion` is checked during registration.
- An answer received before a round opens, after it closes, or for another round is rejected without
  terminating the connection.
- Registration failures are reported with an `Error` message and the connection is closed by the
  server.
- The server uses one bounded outbound queue/writer per connection. Queue overflow disconnects a
  slow client; terminal `Error` and `End` messages are written after earlier queued messages.
- Registration has a read timeout; after registration, the connection remains open for the game.

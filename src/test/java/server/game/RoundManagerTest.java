package server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import utils.quiz.Answer;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;

class RoundManagerTest {

    private static final String TEAM = "A";

    @Test
    void grantsTheSpeedBonusToTheFirstAnswers() throws Exception {
        TeamManager teams = teamManager(3, "alice", "bob", "carol");
        RoundManager rounds = new RoundManager(loadQuiz(), teams);
        rounds.nextQuestion();

        rounds.answer(new Answer(TEAM, "alice", 1, 0));
        rounds.answer(new Answer(TEAM, "bob", 1, 0));
        rounds.answer(new Answer(TEAM, "carol", 1, 0));

        assertEquals(25, scoreOf(teams));
    }

    @Test
    @Timeout(2)
    void aWrongAnswerScoresNothingButStillEndsTheRound() throws Exception {
        TeamManager teams = teamManager(1, "alice");
        RoundManager rounds = new RoundManager(loadQuiz(), teams);
        rounds.nextQuestion();

        rounds.answer(new Answer(TEAM, "alice", 0, 0));
        rounds.waitForRoundEnd();

        assertEquals(0, scoreOf(teams));
    }

    @Test
    void ignoresDuplicateAnswersFromTheSamePlayer() throws Exception {
        TeamManager teams = teamManager(2, "alice", "bob");
        RoundManager rounds = new RoundManager(loadQuiz(), teams);
        rounds.nextQuestion();

        rounds.answer(new Answer(TEAM, "alice", 1, 0));
        int scoreAfterFirstAnswer = scoreOf(teams);

        rounds.answer(new Answer(TEAM, "alice", 1, 0));

        assertEquals(scoreAfterFirstAnswer, scoreOf(teams));

        rounds.answer(new Answer(TEAM, "bob", 1, 0));

        assertEquals(20, scoreOf(teams));
    }

    @Test
    void ignoresAnswersFromAnotherRound() throws Exception {
        TeamManager teams = teamManager(1, "alice");
        RoundManager rounds = new RoundManager(loadQuiz(), teams);
        rounds.nextQuestion();

        rounds.answer(new Answer(TEAM, "alice", 1, -1));
        assertEquals(0, scoreOf(teams));

        rounds.answer(new Answer(TEAM, "alice", 1, 0));
        assertEquals(10, scoreOf(teams));
    }

    @Test
    void ignoresAnswersAfterTheRoundIsClosed() throws Exception {
        TeamManager teams = teamManager(2, "alice", "bob");
        RoundManager rounds = new RoundManager(loadQuiz(), teams);
        rounds.nextQuestion();

        rounds.answer(new Answer(TEAM, "alice", 1, 0));
        rounds.answer(new Answer(TEAM, "bob", 1, 0));
        int scoreAtRoundEnd = scoreOf(teams);

        rounds.waitForRoundEnd();
        rounds.answer(new Answer(TEAM, "alice", 1, 0));

        assertEquals(scoreAtRoundEnd, scoreOf(teams));
    }

    @Test
    @Timeout(5)
    void doublesTheTeamScoreWhenEveryMemberIsCorrect() throws Exception {
        TeamManager teams = teamManager(2, "alice", "bob");
        RoundManager rounds = new RoundManager(loadQuiz(), teams);
        rounds.nextQuestion();
        rounds.nextQuestion();

        answerTogether(rounds, new Answer(TEAM, "alice", 2, 1), new Answer(TEAM, "bob", 2, 1));
        rounds.waitForRoundEnd();

        assertEquals(20, scoreOf(teams));
    }

    @Test
    @Timeout(5)
    void scoresTheBestCorrectAnswerOfTheTeam() throws Exception {
        TeamManager teams = teamManager(2, "alice", "bob");
        RoundManager rounds = new RoundManager(loadQuiz(), teams);
        rounds.nextQuestion();
        rounds.nextQuestion();

        answerTogether(rounds, new Answer(TEAM, "alice", 2, 1), new Answer(TEAM, "bob", 0, 1));
        rounds.waitForRoundEnd();

        assertEquals(10, scoreOf(teams));
    }

    @Test
    @Timeout(5)
    void scoresNothingWhenEveryTeamMemberIsWrong() throws Exception {
        TeamManager teams = teamManager(2, "alice", "bob");
        RoundManager rounds = new RoundManager(loadQuiz(), teams);
        rounds.nextQuestion();
        rounds.nextQuestion();

        answerTogether(rounds, new Answer(TEAM, "alice", 0, 1), new Answer(TEAM, "bob", 1, 1));
        rounds.waitForRoundEnd();

        assertEquals(0, scoreOf(teams));
    }

    private static TeamManager teamManager(int capacity, String... usernames) {
        TeamManager manager = new TeamManager(1, capacity);
        for (String username : usernames)
            assertTrue(manager.registerPlayer(username, TEAM));
        return manager;
    }

    private static int scoreOf(TeamManager manager) {
        return manager.getTeams().get(TEAM).getScore();
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = RoundManagerTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            assertNotNull(input, "quiz-fast.json is missing");
            return QuizCreator.getFrom(input).get(0);
        }
    }

    private static void answerTogether(RoundManager rounds, Answer... answers) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(answers.length);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();

            for (Answer answer : answers) {
                futures.add(pool.submit(() -> {
                    start.await();
                    rounds.answer(answer);
                    return null;
                }));
            }

            start.countDown();

            for (Future<?> future : futures)
                future.get(2, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }
}

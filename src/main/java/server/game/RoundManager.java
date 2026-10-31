package server.game;

import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.Quiz;

public class RoundManager {

    private final Quiz quiz;
    private int curRound = -1;
    private final TeamManager teamManager;
    private RoundState roundState;

    public RoundManager(Quiz quiz, TeamManager teamManager) {
        this.quiz = quiz;
        this.teamManager = teamManager;
    }

    public RoundState openRound() {
        roundState = new RoundState(++curRound, getCurrentQuestion(), teamManager.getUsernames());
        return roundState;
    }

    public void answer(Answer answer) throws InterruptedException {
        roundState.submitAnswer(Thread.currentThread().getName(), answer.getRoundNumber(), answer);
    }

    public Question getCurrentQuestion() {
        return quiz.getQuestion(curRound);
    }

    public boolean hasNextQuestion() {
        return quiz.getNumberOfQuestions() > curRound;
    }

    public int getRoundNumber() {
        return curRound;
    }

}

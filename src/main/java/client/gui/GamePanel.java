package client.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import utils.quiz.ReducedQuestion;
import utils.quiz.Score;

public final class GamePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final HeaderPanel headerPanel;
    private final ScoreboardPanel scoreboardPanel;
    private final QuestionPanel questionPanel;

    public GamePanel(Runnable onOptionSelected) {
        super(new BorderLayout(10, 10));

        headerPanel = new HeaderPanel();
        scoreboardPanel = new ScoreboardPanel();
        questionPanel = new QuestionPanel(onOptionSelected);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scoreboardPanel, questionPanel);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        add(headerPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    public void setPlayer(String username, String teamId) {
        headerPanel.setPlayer(username, teamId);
    }

    public void displayQuestion(ReducedQuestion question) {
        questionPanel.showQuestion(question);
    }

    public void displayScoreboard(List<Score> scoreboard) {
        scoreboardPanel.show(scoreboard);
    }

    public void displayWaiting() {
        questionPanel.showWaiting();
    }

    public void displaySubmitting() {
        questionPanel.showSubmitting();
    }

    public void displayError(String errorMsg) {
        questionPanel.showError(errorMsg);
    }

    public void displayEndGame() {
        questionPanel.showEndGame();
    }

    public void startTimer(long deadlineEpochMillis) {
        questionPanel.startTimer(deadlineEpochMillis);
    }

    public void stopTimer() {
        questionPanel.stopTimer();
    }

    public boolean hasSelectedOption() {
        return questionPanel.hasSelectedOption();
    }

    public int getSelectedOption() {
        return questionPanel.getSelectedOption();
    }
}

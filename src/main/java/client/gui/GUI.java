package client.gui;

import java.awt.CardLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JPanel;

import client.ClientCredentials;
import client.gui.Observer.Observed;
import utils.quiz.ReducedQuestion;
import utils.quiz.Score;

public class GUI extends Observed {

    private static final String LOGIN_CARD = "login";
    private static final String GAME_CARD = "game";

    private final JFrame frame;
    private final CardLayout cards;
    private final JPanel cardsPanel;
    private final LoginPanel loginPanel;
    private final GamePanel gamePanel;
    private Consumer<ClientCredentials> loginHandler = credentials -> {
    };

    public GUI(String username, String teamId, String gameId) {
        frame = new JFrame("IsKahoot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(760, 460);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        loginPanel = new LoginPanel(username, teamId, gameId, this::submitLogin);
        gamePanel = new GamePanel(this::notifyObservers);

        cards = new CardLayout();
        cardsPanel = new JPanel(cards);
        cardsPanel.add(loginPanel, LOGIN_CARD);
        cardsPanel.add(gamePanel, GAME_CARD);

        frame.add(cardsPanel);
        loginPanel.installDefaultButton(frame.getRootPane());
    }

    public void setLoginHandler(Consumer<ClientCredentials> handler) {
        loginHandler = handler;
    }

    public void show() {
        frame.setVisible(true);
    }

    public void showGame(ClientCredentials credentials) {
        gamePanel.setPlayer(credentials.username(), credentials.teamId());
        frame.getRootPane().setDefaultButton(null);
        cards.show(cardsPanel, GAME_CARD);
    }

    public void displayQuestion(ReducedQuestion question) {
        gamePanel.displayQuestion(question);
    }

    public void startTimer(long deadlineEpochMillis) {
        gamePanel.startTimer(deadlineEpochMillis);
    }

    public void stopTimer() {
        gamePanel.stopTimer();
    }

    public void displayScoreboard(List<Score> scoreboard) {
        gamePanel.displayScoreboard(scoreboard);
    }

    public void displayWaiting() {
        gamePanel.displayWaiting();
    }

    public void displaySubmitting() {
        gamePanel.displaySubmitting();
    }

    public void displayError(String errorMsg) {
        gamePanel.displayError(errorMsg);
    }

    public void displayEndGame() {
        gamePanel.displayEndGame();
    }

    public boolean hasSelectedOption() {
        return gamePanel.hasSelectedOption();
    }

    public int getSelectedOption() {
        return gamePanel.getSelectedOption();
    }

    private void submitLogin(ClientCredentials credentials) {
        loginHandler.accept(credentials);
    }
}

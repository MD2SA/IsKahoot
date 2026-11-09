package client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import utils.quiz.ReducedQuestion;

public final class QuestionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final TimerLabel timerLabel = new TimerLabel();
    private final transient Runnable onOptionSelected;

    private int selectedOption = -1;

    public QuestionPanel(Runnable onOptionSelected) {
        this.onOptionSelected = onOptionSelected;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);
    }

    public void showQuestion(ReducedQuestion question) {
        selectedOption = -1;
        removeAll();

        String modeText = question.isIndividual() ? "Pergunta individual" : "Pergunta de equipa";
        JLabel modeLabel = new JLabel(modeText);
        modeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        modeLabel.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel pointsLabel = new JLabel("Vale " + question.getPoints() + " pontos");
        pointsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pointsLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel questionLabel = new JLabel(question.getQuestion());
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionLabel.setBorder(new EmptyBorder(10, 0, 20, 0));

        JPanel timerPanel = new JPanel();
        timerPanel.setBackground(Color.WHITE);
        timerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        timerPanel.add(timerLabel);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(2, 2, 10, 10));
        optionsPanel.setBackground(Color.WHITE);

        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            JButton button = new JButton(options.get(i));
            final int index = i;
            button.setFont(new Font("SansSerif", Font.BOLD, 14));
            button.setBackground(new Color(220, 220, 255));
            button.setFocusPainted(false);
            button.addActionListener(event -> selectOption(index));
            button.setBorder(new LineBorder(new Color(150, 150, 220), 2));
            optionsPanel.add(button);
        }

        add(modeLabel);
        add(pointsLabel);
        add(questionLabel);
        add(timerPanel);
        add(optionsPanel);

        refresh();
    }

    public void showConnecting() {
        showInfo("A ligar ao servidor...");
    }

    public void showLobby() {
        showInfo("À espera de outros jogadores se juntarem...");
    }

    public void showWaitingForOthers() {
        showInfo("Resposta enviada. À espera dos outros jogadores...");
    }

    public void showSubmitting() {
        showInfo("A enviar resposta...");
    }

    public void showError(String errorMsg) {
        stopTimer();
        removeAll();
        add(message(errorMsg, Font.BOLD, 16, Color.RED));
        refresh();
    }

    public void showEndGame() {
        stopTimer();
        removeAll();
        add(message("Jogo terminado.", Font.BOLD, 18, Color.BLACK));
        refresh();
    }

    public void startTimer(long deadlineEpochMillis) {
        timerLabel.start(deadlineEpochMillis);
    }

    public void stopTimer() {
        timerLabel.stop();
    }

    public boolean hasSelectedOption() {
        return selectedOption != -1;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    private void selectOption(int index) {
        selectedOption = index;
        onOptionSelected.run();
    }

    private void showInfo(String text) {
        removeAll();
        add(message(text, Font.BOLD, 16, Color.BLACK));
        refresh();
    }

    private JLabel message(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void refresh() {
        revalidate();
        repaint();
    }
}

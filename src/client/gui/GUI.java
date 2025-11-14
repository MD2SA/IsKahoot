package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.*;
import java.util.stream.IntStream;

import javax.swing.*;

import client.gui.Observer.Observed;
import configs.Config;
import server.game.utils.Score;
import utils.quiz.Question;

public class GUI extends Observed {

        private final JPanel mainPanel;
        private final JPanel questionPanel;
        private final JPanel scorePanel;

        private int selectedOption;

        public GUI() {
                JFrame frame = new JFrame("IsKahoot");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800,400);
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);

                mainPanel = new JPanel(new GridLayout(2,1));
                questionPanel = new JPanel(new BorderLayout(10,10));
                scorePanel = new JPanel(new BorderLayout());

                mainPanel.add(scorePanel);
                mainPanel.add(questionPanel);

                frame.setContentPane(mainPanel);

                frame.setVisible(true);
        }

        public void update(Question question, List<Score> scoreboard) {
                if(question != null) displayQuestion(question);
                else {
                        mainPanel.remove(questionPanel);
                        mainPanel.revalidate();
                        mainPanel.repaint();
                }
                displayScoreboard(scoreboard);
        }

        private void displayQuestion(Question question) {
                selectedOption = -1;

                questionPanel.removeAll();

                JLabel label = new JLabel(question.getQuestion());
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setVerticalAlignment(SwingConstants.CENTER);
                questionPanel.add(label, BorderLayout.NORTH);

                JPanel optionsPanel = new JPanel(new GridLayout(2,2));

                List<String> options = question.getOptions();

                IntStream.range(0, options.size())
                        .forEach(i->{
                                JButton button = new JButton(options.get(i));
                                styleButton(button);
                                button.addActionListener(l->{
                                        displayWaiting();
                                        this.selectedOption = i;
                                        notifyObservers();
                                });
                                optionsPanel.add(button);
                        });

                questionPanel.add(optionsPanel, BorderLayout.CENTER);

                questionPanel.revalidate();
                questionPanel.repaint();
        }

        private void displayScoreboard(List<Score> scoreboard) {
                scorePanel.removeAll();
                String[] colunas = {"Nome", "Pontuação"};
                Object[][] dados;

                if(Config.Features.HIDE_SCOREBOARD) {
                        dados = getDummyScoreboard();
                } else {
                        dados = scoreboard.stream()
                                .map(s-> new Object[]{s.getName(), s.getScore()})
                                .toArray(Object[][]::new);
                }

                JTable tabela = new JTable(dados, colunas);
                JScrollPane scrollPane = new JScrollPane(tabela);
                scorePanel.add(scrollPane, BorderLayout.CENTER);

                scorePanel.revalidate();
                scorePanel.repaint();
        }

        public void displayWaiting() {
                questionPanel.removeAll();

                JPanel panel = new JPanel();

                JLabel label = new JLabel("Waiting...");
                panel.add(label);

                questionPanel.add(panel);

                questionPanel.revalidate();
                questionPanel.repaint();
        }

        public boolean hasSelectedOption() {
                return selectedOption != -1;
        }

        public int getSelectedOption() {
                return selectedOption;
        }

        private Object[][] getDummyScoreboard() {
                return new Object[][]{
                        {"Red Dragons", 50},
                        {"Blue Sharks", 75},
                        {"Green Tigers", 60},
                        {"Yellow Lions", 40}
                };
        }

        private void styleButton(JButton button) {
                button.setFocusPainted(false);
                button.setBackground(new Color(230,230,250));
                button.setFont(new Font("SansSerif", Font.BOLD, 10));
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

}

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
import server.game.utils.Team;
import utils.Question;

public class GUI extends Observed {

        private final JPanel mainPanel;

        private int selectedOption;

        public GUI() {
                JFrame frame = new JFrame("IsKahoot");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(500,400);
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);

                mainPanel = new JPanel(new BorderLayout(10,10));
                frame.setContentPane(mainPanel);

                frame.setVisible(true);
        }

        public void displayQuestion(Question question) {
                selectedOption = -1;

                mainPanel.removeAll();

                JLabel label = new JLabel(question.getQuestion());
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setVerticalAlignment(SwingConstants.CENTER);
                mainPanel.add(label, BorderLayout.NORTH);

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

                mainPanel.add(optionsPanel, BorderLayout.CENTER);

                mainPanel.revalidate();
                mainPanel.repaint();
        }

        public void displayScoreboard(Collection<Team> teams) {
                mainPanel.removeAll();
                String[] colunas = {"Nome", "Pontuação"};
                Object[][] dados;

                if(Config.Features.HIDE_SCOREBOARD) {
                        dados = getDummyScoreboard();
                } else {
                        dados = teams.stream()
                                .map(team-> new Object[]{
                                        team.getName(),
                                        team.getTeamScore()
                                }).toArray(Object[][]::new);
                }

                JTable tabela = new JTable(dados, colunas);
                JScrollPane scrollPane = new JScrollPane(tabela);
                mainPanel.add(scrollPane, BorderLayout.CENTER);

                mainPanel.revalidate();
                mainPanel.repaint();
        }

        public void displayWaiting() {
                mainPanel.removeAll();

                JPanel panel = new JPanel();

                JLabel label = new JLabel("Waiting...");
                panel.add(label);

                mainPanel.add(panel);

                mainPanel.revalidate();
                mainPanel.repaint();
        }

        public void addExitButton() {
                System.out.println("BUTTON");
                JButton button = new JButton("Sair");
                button.addActionListener(l->System.exit(0));
                mainPanel.add(button, BorderLayout.SOUTH);
                mainPanel.revalidate();
                mainPanel.repaint();
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
                button.setFont(new Font("SansSerif", Font.BOLD, 14));
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

}
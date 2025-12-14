package client.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import client.gui.Observer.Observed;
import utils.quiz.Score;
import utils.quiz.Question;
import java.awt.*;
import java.util.List;

import utils.Timer;

public class GUI extends Observed {

        private final JPanel questionPanel;
        private final JPanel scorePanel;
        private JLabel timerLabel;
        private Timer timer;

        private int selectedOption;

        public GUI(String username, String teamId) {

                JFrame frame = new JFrame("IsKahoot");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(760, 460);
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);
                frame.setLayout(new BorderLayout(10, 10));

                JPanel header = createHeader(username, teamId);

                scorePanel = new JPanel(new BorderLayout());
                scorePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                scorePanel.setBackground(new Color(245, 245, 250));

                questionPanel = new JPanel();
                questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
                questionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
                questionPanel.setBackground(Color.WHITE);

                // Inicialização do timerLabel
                timerLabel = new JLabel("", SwingConstants.CENTER);

                JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scorePanel, questionPanel);
                splitPane.setResizeWeight(0.35);
                splitPane.setDividerSize(5);
                splitPane.setBorder(null);

                frame.add(header, BorderLayout.NORTH);
                frame.add(splitPane, BorderLayout.CENTER);

                frame.setVisible(true);
        }

        public void displayQuestion(Question question) {
                selectedOption = -1;
                questionPanel.removeAll();

                String modeText = question.isIndividual() ? "Individual question" : "Team question";
                JLabel modeLabel = new JLabel(modeText);
                modeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                modeLabel.setBorder(new EmptyBorder(5, 0, 5, 0));

                JLabel pointsLabel = new JLabel("For " + question.getPoints() + " points");
                pointsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                pointsLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

                JLabel q = new JLabel(question.getQuestion());
                q.setFont(new Font("SansSerif", Font.BOLD, 18));
                q.setAlignmentX(Component.CENTER_ALIGNMENT);
                q.setBorder(new EmptyBorder(10, 0, 20, 0));

                // Painel do timer
                JPanel timerPanel = new JPanel();
                timerPanel.setBackground(Color.WHITE);
                timerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                timerLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
                timerLabel.setForeground(Color.BLACK);
                timerPanel.add(timerLabel);

                JPanel opts = new JPanel();
                opts.setLayout(new GridLayout(2, 2, 10, 10));
                opts.setBackground(Color.WHITE);

                List<String> options = question.getOptions();
                for (int i = 0; i < options.size(); i++) {
                        JButton b = new JButton(options.get(i));
                        final int index = i;
                        b.setFont(new Font("SansSerif", Font.BOLD, 14));
                        b.setBackground(new Color(220, 220, 255));
                        b.setFocusPainted(false);
                        b.addActionListener(e -> {
                                displayWaiting();
                                selectedOption = index;
                                notifyObservers();
                        });
                        b.setBorder(new LineBorder(new Color(150, 150, 220), 2));
                        opts.add(b);
                }

                questionPanel.add(modeLabel);
                questionPanel.add(pointsLabel);
                questionPanel.add(q);
                questionPanel.add(timerPanel);
                questionPanel.add(opts);

                refresh(questionPanel);
        }

        public void startTimer(int seconds) {
                if (timer != null) {
                        timer.interrupt();
                }
                timerLabel.setText(String.valueOf(seconds));

                timer = new Timer(seconds, () -> {
                        SwingUtilities.invokeLater(() -> {
                                int current = timer.getTimeLeft();
                                timerLabel.setText(String.valueOf(current));
                                if (current <= 5) {
                                        timerLabel.setForeground(Color.RED);
                                } else {
                                        timerLabel.setForeground(Color.BLACK);
                                }
                        });
                }, true);

                timer.start();
        }

        public void displayScoreboard(List<Score> scoreboard) {
                scorePanel.removeAll();

                JLabel header = new JLabel("Scoreboard");
                header.setFont(new Font("SansSerif", Font.BOLD, 18));
                header.setHorizontalAlignment(SwingConstants.CENTER);
                header.setBorder(new EmptyBorder(0, 0, 10, 0));

                String[] cols = { "Nome", "Pontuação" };
                Object[][] data = new Object[scoreboard.size()][];
                for (int i = 0; i < scoreboard.size(); i++) {
                        Score s = scoreboard.get(i);
                        data[i] = new Object[] { s.getName(), s.getScore() };
                }

                JTable table = new JTable(data, cols);
                table.setRowHeight(25);
                table.setEnabled(false);
                table.getTableHeader().setReorderingAllowed(false);
                table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
                for (int i = 0; i < cols.length; i++)
                        table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

                JScrollPane scroll = new JScrollPane(table);
                scroll.setBorder(new LineBorder(new Color(180, 180, 220), 1));

                scorePanel.add(header, BorderLayout.NORTH);
                scorePanel.add(scroll, BorderLayout.CENTER);

                refresh(scorePanel);
        }

        public void displayWaiting() {
                questionPanel.removeAll();
                JLabel l = new JLabel("A aguardar...");
                l.setFont(new Font("SansSerif", Font.BOLD, 16));
                l.setAlignmentX(Component.CENTER_ALIGNMENT);
                questionPanel.add(l);
                refresh(questionPanel);
        }

        public void displayError(String errorMsg) {
                questionPanel.removeAll();
                JLabel l = new JLabel(errorMsg);
                l.setFont(new Font("SansSerif", Font.BOLD, 16));
                l.setForeground(Color.RED);
                l.setAlignmentX(Component.CENTER_ALIGNMENT);
                questionPanel.add(l);
                refresh(questionPanel);
        }

        public void displayEndGame() {
                questionPanel.removeAll();
                JLabel label = new JLabel("Jogo terminado.");
                label.setFont(new Font("SansSerif", Font.BOLD, 18));
                label.setAlignmentX(Component.CENTER_ALIGNMENT);
                questionPanel.add(label);
                refresh(questionPanel);
        }

        private JPanel createHeader(String username, String teamId) {
                JPanel p = new JPanel();
                p.setLayout(new BorderLayout());
                p.setBackground(new Color(50, 50, 80));
                p.setBorder(new EmptyBorder(10, 15, 10, 15));

                JLabel title = new JLabel("IsKahoot");
                title.setFont(new Font("SansSerif", Font.BOLD, 22));
                title.setForeground(Color.WHITE);

                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                info.setBackground(new Color(50, 50, 80));
                JLabel u = new JLabel("User: " + username);
                JLabel t = new JLabel("Team: " + teamId);
                u.setForeground(Color.WHITE);
                t.setForeground(Color.WHITE);
                info.add(u);
                info.add(t);

                p.add(title, BorderLayout.WEST);
                p.add(info, BorderLayout.EAST);

                return p;
        }

        private void refresh(JPanel p) {
                p.revalidate();
                p.repaint();
        }

        public boolean hasSelectedOption() {
                return selectedOption != -1;
        }

        public int getSelectedOption() {
                return selectedOption;
        }

}

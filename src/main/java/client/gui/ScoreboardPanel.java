package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

import utils.quiz.Score;

public final class ScoreboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public ScoreboardPanel() {
        super(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 245, 250));
    }

    public void show(List<Score> scoreboard) {
        removeAll();

        JLabel header = new JLabel("Classificação");
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));

        String[] cols = {"Nome", "Pontuação"};
        Object[][] data = new Object[scoreboard.size()][];

        for (int i = 0; i < scoreboard.size(); i++) {
            Score score = scoreboard.get(i);
            data[i] = new Object[]{score.teamId(), score.points()};
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

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}

package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public final class HeaderPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel userLabel = new JLabel("Utilizador: —");
    private final JLabel teamLabel = new JLabel("Equipa: —");

    public HeaderPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(50, 50, 80));
        setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("IsKahoot");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(new Color(50, 50, 80));
        userLabel.setForeground(Color.WHITE);
        teamLabel.setForeground(Color.WHITE);
        info.add(userLabel);
        info.add(teamLabel);

        add(title, BorderLayout.WEST);
        add(info, BorderLayout.EAST);
    }

    public void setPlayer(String username, String teamId) {
        userLabel.setText("Utilizador: " + username);
        teamLabel.setText("Equipa: " + teamId);
    }
}

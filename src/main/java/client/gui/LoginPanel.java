package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import client.ClientCredentials;

public final class LoginPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JTextField usernameField;
    private final JTextField teamField;
    private final JTextField gameField;
    private final JLabel errorLabel;
    private final JButton loginButton;
    private final transient Consumer<ClientCredentials> onLogin;

    public LoginPanel(String username, String teamId, String gameId, Consumer<ClientCredentials> onLogin) {
        this.onLogin = onLogin;
        usernameField = new JTextField(username, 18);
        teamField = new JTextField(teamId, 18);
        gameField = new JTextField(gameId, 18);
        errorLabel = new JLabel(" ");
        loginButton = new JButton("Entrar");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.addActionListener(event -> submit());

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    public void installDefaultButton(JRootPane rootPane) {
        rootPane.setDefaultButton(loginButton);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(50, 50, 80));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("IsKahoot");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Entrar num jogo");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(200, 200, 220));

        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Color.WHITE);
        content.setPreferredSize(new Dimension(460, 160));
        content.add(createForm(), BorderLayout.CENTER);
        content.add(createFooter(), BorderLayout.SOUTH);

        body.add(content);
        return body;
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBackground(Color.WHITE);
        form.add(createLabel("Utilizador:", usernameField));
        form.add(usernameField);
        form.add(createLabel("Equipa (ID):", teamField));
        form.add(teamField);
        form.add(createLabel("Código do jogo:", gameField));
        form.add(gameField);
        return form;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);

        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(loginButton);

        footer.add(errorLabel, BorderLayout.CENTER);
        footer.add(buttons, BorderLayout.EAST);
        return footer;
    }

    private JLabel createLabel(String text, Component field) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setLabelFor(field);
        return label;
    }

    private void submit() {
        String username = usernameField.getText().trim();
        String teamId = teamField.getText().trim();
        String gameId = gameField.getText().trim();

        if (username.isEmpty() || teamId.isEmpty() || gameId.isEmpty()) {
            errorLabel.setText("Preenche todos os campos.");
            return;
        }

        errorLabel.setText(" ");
        onLogin.accept(new ClientCredentials(username, teamId, gameId));
    }
}

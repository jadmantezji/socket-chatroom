package pl.sepulkarz.socketchatroom.client.gui;

import pl.sepulkarz.socketchatroom.client.net.Communicator;
import pl.sepulkarz.socketchatroom.client.net.ILoginListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * GUI's entry point - window asking user for his user name that will be used to display and identify him.
 * Confirmation can result in an error when the entered name is taken by some other client. User can also cancel and
 * quite the application.
 */
public class WelcomeFrame extends JFrame implements ILoginListener {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("strings");
    private static final String WINDOW_TITLE = RESOURCE_BUNDLE.getString("welcome");
    private static final String PROMPT = RESOURCE_BUNDLE.getString("welcome.prompt");

    private Communicator communicator;
    private JTextField userNameField;
    private JButton okButton;
    private JButton cancelButton;

    public WelcomeFrame(Communicator communicator) {
        this.communicator = communicator;
        communicator.addLoginListener(WelcomeFrame.this);
        constructWindow();
        assignListeners();
    }

    private void constructWindow() {
        setLayout(new BorderLayout());
        setTitle(WINDOW_TITLE);
        JLabel label = new JLabel(String.format(PROMPT, communicator.getClient().getConnection().getRemoteAddress()));
        add(label, BorderLayout.PAGE_START);
        userNameField = new JTextField();
        add(userNameField, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        okButton = new JButton(RESOURCE_BUNDLE.getString("button.ok"));
        buttonPanel.add(okButton);
        cancelButton = new JButton(RESOURCE_BUNDLE.getString("button.cancel"));
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.PAGE_END);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // centers window
        setLocationRelativeTo(null);
        setVisible(true);
        userNameField.requestFocusInWindow();
    }

    private void assignListeners() {
        ActionListener userEnteredNameListener = new UserEnteredNameListener();
        userNameField.addActionListener(userEnteredNameListener);
        okButton.addActionListener(userEnteredNameListener);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                communicator.leaveChatRoom();
                System.exit(0);
            }
        });
    }

    @Override
    public void loginSuccessful() {
        dispose();
        communicator.removeLoginListener(this);
        new ChatRoomFrame(communicator);
    }

    @Override
    public void rejectedUserName() {
        JOptionPane.showMessageDialog(this, RESOURCE_BUNDLE.getString("server.rejected.user.name"), RESOURCE_BUNDLE
                        .getString("login.error"),
                JOptionPane.ERROR_MESSAGE);
        userNameField.setText("");
    }

    @Override
    public void connectionError() {
        dispose();
        JOptionPane pane = new JOptionPane(RESOURCE_BUNDLE.getString("connection.to.server.error"), JOptionPane
                .ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(RESOURCE_BUNDLE.getString("error"));
        dialog.setVisible(true);
        dialog.dispose();
        System.exit(1);
    }

    private class UserEnteredNameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = userNameField.getText();
            // Don't allow empty name.
            if (name != null && !name.isEmpty()) {
                communicator.getClient().setName(name);
                communicator.sendHello();
            }
        }
    }

}

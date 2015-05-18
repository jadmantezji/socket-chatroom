package pl.sepulkarz.socketchatroom.client.gui;

import pl.sepulkarz.socketchatroom.client.net.Communicator;
import pl.sepulkarz.socketchatroom.client.net.IMessageListener;
import pl.sepulkarz.socketchatroom.net.data.Message;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Private chat window - similar to the main chat room window, except there is no guest list and only two clients
 * participate in the conversation. The title shows the interlocutor's name.
 */
public class PrivateChatFrame extends JFrame implements IMessageListener {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("strings");

    private Communicator communicator;
    private JTextArea textArea;
    private JTextField messageField;
    private JButton sendButton;
    private String interlocutor;

    public PrivateChatFrame(Communicator communicator, String interlocutor) {
        this.communicator = communicator;
        communicator.addMessageListener(this);
        this.interlocutor = interlocutor;
        constructWindow();
        assignListeners();
    }

    private void constructWindow() {
        setLayout(new BorderLayout());
        setTitle(String.format(RESOURCE_BUNDLE.getString("format.window.title.private"), interlocutor));
        setPreferredSize(new Dimension(600, 400));
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBorder(new LineBorder(Color.BLACK));
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(textArea);
        add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        messageField = new JTextField();
        bottomPanel.add(messageField);
        sendButton = new JButton(RESOURCE_BUNDLE.getString("button.send"));
        bottomPanel.add(sendButton, BorderLayout.LINE_END);
        add(bottomPanel, BorderLayout.PAGE_END);
        pack();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                ChatRoomFrame.privateChats.remove(interlocutor);
                communicator.removeMessageListener(PrivateChatFrame.this);
            }
        });
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // centers window
        setLocationRelativeTo(null);
        setVisible(true);
        messageField.requestFocusInWindow();
    }

    private void assignListeners() {
        ActionListener al = new UserEnteredMessageListener();
        messageField.addActionListener(al);
        sendButton.addActionListener(al);
    }

    @Override
    public void messageArrived(final Message message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!message.isBroadcast()) {
                    textArea.append(String.format(RESOURCE_BUNDLE.getString("format.message"), message.getDate(),
                            message.getFrom(), message.getText()));
                }
            }
        });
    }

    @Override
    public void joined(final Date when, final String who) {
        // Do nothing, the main window takes care of that
    }

    @Override
    public void left(final Date when, final String who) {
        if (!interlocutor.equals(who)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    textArea.append(String.format(RESOURCE_BUNDLE.getString("format.left"), when, who));
                    messageField.setEnabled(false);
                    sendButton.setEnabled(false);
                }
            });
        }
    }

    /**
     * Handles user's intention to send a private message.
     */
    private class UserEnteredMessageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String text = messageField.getText();
            if (!text.isEmpty()) {
                communicator.sendPrivateMessage(interlocutor, text);
                // Display own message as it is not going to be sent back
                messageArrived(new Message.Builder().from(communicator.getClient().getName()).text(text).build());
                messageField.setText(null);
            }
        }
    }

}

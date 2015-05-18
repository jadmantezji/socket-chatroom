package pl.sepulkarz.socketchatroom.client.gui;

import pl.sepulkarz.socketchatroom.client.net.Communicator;
import pl.sepulkarz.socketchatroom.client.net.IMessageListener;
import pl.sepulkarz.socketchatroom.net.data.Message;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Main window of the chat room. It has the events frame where messages and information about joining and leaving of
 * others is displayed, guest list frame and edit box + button for entering messages.
 */
public class ChatRoomFrame extends JFrame implements IMessageListener {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("strings");

    static Set<String> privateChats = new HashSet<String>();
    private Communicator communicator;
    private JTextArea textArea;
    private JList<String> guestList;
    private SortedListModel<String> guestListModel;
    private JTextField messageField;
    private JButton sendButton;

    public ChatRoomFrame(Communicator communicator) {
        this.communicator = communicator;
        communicator.addMessageListener(this);
        constructWindow();
        addMyselfToGuestList();
        assignListeners();
    }

    private void constructWindow() {
        setLayout(new BorderLayout());
        setTitle(String.format(RESOURCE_BUNDLE.getString("format.window.title.chatroom"), communicator.getClient()
                .getConnection()
                .getRemoteAddress()));
        setPreferredSize(new Dimension(800, 600));
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
        guestListModel = new SortedListModel<String>();
        guestList = new JList<String>(guestListModel);
        guestList.setCellRenderer(new GuestListCellRenderer());
        guestList.setBorder(new LineBorder(Color.BLACK));
        add(guestList, BorderLayout.LINE_END);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        messageField = new JTextField();
        bottomPanel.add(messageField);
        sendButton = new JButton(RESOURCE_BUNDLE.getString("button.send"));
        bottomPanel.add(sendButton, BorderLayout.LINE_END);
        add(bottomPanel, BorderLayout.PAGE_END);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                communicator.leaveChatRoom();
                communicator.removeMessageListener(ChatRoomFrame.this);
                System.exit(0);
            }
        });
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        messageField.requestFocusInWindow();
        // centers window
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addMyselfToGuestList() {
        joined(new Date(), communicator.getClient().getName());
    }

    private void assignListeners() {
        ActionListener userEnteredMessageListener = new UserEnteredMessageListener();
        messageField.addActionListener(userEnteredMessageListener);
        sendButton.addActionListener(userEnteredMessageListener);
        UserStartedPrivateChatListener pcal = new UserStartedPrivateChatListener();
        guestList.addMouseListener(pcal);
    }

    @Override
    public void messageArrived(final Message message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (message.isBroadcast()) {
                    textArea.append(String.format(RESOURCE_BUNDLE.getString("format.message"), message.getDate(),
                            message.getFrom(), message.getText()));
                } else {
                    if (!privateChats.contains(message.getFrom())) {
                        privateChats.add(message.getFrom());
                        new PrivateChatFrame(communicator, message.getFrom()).messageArrived(message);
                    }
                }
            }
        });
    }

    @Override
    public void joined(final Date when, final String who) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.append(String.format(RESOURCE_BUNDLE.getString("format.joined"), when, who));
                guestListModel.addElement(who);
            }
        });
    }

    @Override
    public void left(final Date when, final String who) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.append(String.format(RESOURCE_BUNDLE.getString("format.left"), when, who));
                guestListModel.removeElement(who);
            }
        });
    }

    /**
     * Handles user's intention to send a message to general public.
     */
    private class UserEnteredMessageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!messageField.getText().isEmpty()) {
                communicator.sendMessageToAll(messageField.getText());
                messageField.setText(null);
            }
        }
    }

    /**
     * Handles the intention of starting a private chat.
     */
    private class UserStartedPrivateChatListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                String selectedGuest = guestList.getSelectedValue();
                if (!privateChats.contains(selectedGuest) && !communicator.getClient().getName().equals
                        (selectedGuest)) {
                    privateChats.add(selectedGuest);
                    new PrivateChatFrame(communicator, selectedGuest);
                }
            }
        }
    }

    /**
     * Colors me (guest entry representing this client) differently (in orange) than others.
     */
    private class GuestListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String guest = (String) value;
            if (communicator.getClient().getName().equals(guest)) {
                setBackground(Color.ORANGE);
            }
            return c;
        }

    }

}

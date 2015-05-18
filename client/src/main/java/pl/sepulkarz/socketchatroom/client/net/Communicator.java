package pl.sepulkarz.socketchatroom.client.net;

import pl.sepulkarz.socketchatroom.net.data.Message;
import pl.sepulkarz.socketchatroom.net.transport.ClientData;
import pl.sepulkarz.socketchatroom.net.transport.Connection;

import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles communication with a server. Contains listeners, so callbacks can be called when new messages arrive.
 */
public class Communicator {

    private final static Logger LOGGER = Logger.getLogger(Communicator.class.getName());

    private ClientData client;
    private SenderThread senderThread;
    private Set<IMessageListener> messageListeners = new HashSet<IMessageListener>();
    private Set<ILoginListener> loginListeners = new HashSet<ILoginListener>();

    public Communicator(String serverAddress, int serverPort) throws IOException {
        client = new ClientData(new Connection(new Socket(serverAddress, serverPort)));
        new Thread(new ServerListenerThread()).start();
        senderThread = new SenderThread();
        new Thread(senderThread).start();
    }

    public ClientData getClient() {
        return client;
    }

    public void addMessageListener(IMessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    public void removeMessageListener(IMessageListener messageListener) {
        messageListeners.remove(messageListener);
    }

    public void addLoginListener(ILoginListener loginListener) {
        loginListeners.add(loginListener);
    }

    public void removeLoginListener(ILoginListener loginListener) {
        loginListeners.remove(loginListener);
    }

    public void sendHello() {
        senderThread.enqueueMessage(new Message.Builder().from(client.getName()).type(Message.Type.HELLO).build());
    }

    public void sendMessageToAll(String text) {
        senderThread.enqueueMessage(new Message.Builder().from(client.getName()).to(Message.BROADCAST).text(text)
                .build());
    }

    public void sendPrivateMessage(String to, String text) {
        senderThread.enqueueMessage(new Message.Builder().from(client.getName()).to(to).text(text).build());
    }

    public void leaveChatRoom() {
        LOGGER.info("Leaving. Goodbye!");
        try {
            client.getConnection().shutdownStreams();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Problem with shutting down streams socket", e);
        }
    }

    private class ServerListenerThread implements Runnable {

        @Override
        public void run() {
            try {
                Message message;
                // Work until socket is closed.
                while ((message = client.getConnection().receive()) != null) {
                    Date date = message.getDate();
                    String from = message.getFrom();
                    switch (message.getType()) {
                        case JOINED:
                            for (IMessageListener messageListener : messageListeners) {
                                messageListener.joined(date, from);
                            }
                            break;
                        case NORMAL:
                            for (IMessageListener messageListener : messageListeners) {
                                messageListener.messageArrived(message);
                            }
                            break;
                        case LEFT:
                            for (IMessageListener messageListener : messageListeners) {
                                messageListener.left(date, from);
                            }
                            break;
                        case LOGIN_SUCCESSFUL:
                            for (ILoginListener loginListener : loginListeners) {
                                loginListener.loginSuccessful();
                            }
                            break;
                        case REJECTED_USER_NAME:
                            for (ILoginListener loginListener : loginListeners) {
                                loginListener.rejectedUserName();
                            }
                            break;
                        default:
                            LOGGER.log(Level.WARNING, "Unrecognized message " + message);
                            break;
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Socket reading failure", e);
            } catch (ParseException e) {
                LOGGER.log(Level.SEVERE, "Incorrect message format", e);
            } finally {
                try {
                    // Wait for the writer thread if it's still writing to socket.
                    synchronized (Communicator.this) {
                        client.getConnection().close();
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Socket closing failure", e);
                }
            }
        }
    }

    /**
     * Consumer thread sending messages to a server.
     */
    private class SenderThread implements Runnable {

        private BlockingQueue<Message> messagesToSend = new LinkedBlockingDeque<Message>();

        @Override
        public void run() {
            while (client.getConnection().isPossibleToWrite()) {
                try {
                    // Listener thread could close the connection in the meantime, so we synchronize and check the
                    // possibility of writing again.
                    synchronized (Communicator.this) {
                        if (client.getConnection().isPossibleToWrite()) {
                            client.getConnection().send(messagesToSend.take());
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Problem with connection", e);
                    for (ILoginListener loginListener : loginListeners) {
                        loginListener.connectionError();
                    }
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Thread interrupted", e);
                }
            }
        }

        /**
         * Enqueue message to send. The implementation is thread-safe as it delegates to {@code BlockingQueue}.
         *
         * @param message The message.
         */
        public void enqueueMessage(Message message) {
            messagesToSend.add(message);
        }

    }

}

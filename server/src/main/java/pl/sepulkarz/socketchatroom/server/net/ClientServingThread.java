package pl.sepulkarz.socketchatroom.server.net;

import pl.sepulkarz.socketchatroom.net.data.Message;
import pl.sepulkarz.socketchatroom.net.transport.ClientData;
import pl.sepulkarz.socketchatroom.net.transport.Connection;

import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This thread handles communication with a single client.
 */
public class ClientServingThread implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(ClientServingThread.class.getName());

    /**
     * This mapping is needed to make sure there are no two clients with the same name and to send broadcast messages.
     */
    private final static Map<String, ClientData> clients = new ConcurrentHashMap<String, ClientData>();

    /**
     * Keeps connection-related information for this thread's client.
     */
    private ClientData myClient;

    public ClientServingThread(Socket socket) throws IOException {
        myClient = new ClientData(new Connection(socket));
    }

    @Override
    public void run() {
        LOGGER.info(String.format("%s: Serving client %s", Thread.currentThread().getName(), myClient));
        try {
            Message message;
            // Work until client closes the socket.
            while ((message = myClient.getConnection().receive()) != null) {
                switch (message.getType()) {
                    case HELLO:
                        handleHello(message);
                        break;
                    case NORMAL:
                        if (message.isBroadcast()) {
                            broadcast(message);
                        } else {
                            send(message, clients.get(message.getTo()));
                        }
                        break;
                    default:
                        LOGGER.log(Level.WARNING, "Not supported message type: " + message);
                        break;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Network communication error", e);
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Incorrect message format", e);
        } finally {
            LOGGER.log(Level.INFO, "Stopping serving " + myClient);
            try {
                clients.remove(myClient.getName());
                // Inform all the other clients of the fact that this one has just left.
                broadcast(new Message.Builder().type(Message.Type.LEFT).from(myClient.getName()).build());
                myClient.getConnection().close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Connection closing failure", e);
            }
        }
    }

    /**
     * Handles {@code Message.Type.HELLO} message. If a client joins the chat room (by sending a hello message), a
     * few things need to happen:
     * <li>Check if there is another client with the name provided and if so, reject by sending {@code Message
     * .Type.REJECTED_USER_NAME} message.</li>
     * <li>Send acknowledgment in form of {@code Message.Type.LOGIN_SUCCESSFUL} message.</li>
     * <li>Inform other clients of the presence of the new client.</li>
     * <li>Inform the new client of the presence of others</li>
     * TODO: Introduce ChatRoomState type containing a list of present clients (and additional fields, e.g. chat room
     * topic, description, etc.). Send its representation to all newly joined clients.
     *
     * @param message The hello message.
     * @throws IOException
     */
    private void handleHello(Message message) throws IOException {
        myClient.setName(message.getFrom());
        myClient.setJoinedDate(message.getDate());
        if (clients.containsKey(myClient.getName())) {
            LOGGER.info("Rejecting " + myClient);
            // There is already another client in the chat room with this name, so server has to deny.
            send(new Message.Builder().type(Message.Type.REJECTED_USER_NAME).build());
        } else {
            LOGGER.info("Accepting " + myClient);
            clients.put(myClient.getName(), myClient);
            send(new Message.Builder().type(Message.Type.LOGIN_SUCCESSFUL).build());
            // All the other clients need to be informed of a fact that someone new joined the chat room.
            informOthersOfMyPresence();
            // Inform my client of clients present in the chat room and when they joined.
            informClientOfOthersPresence();
        }
    }

    /**
     * Broadcasts {@code Message.Type.JOINED} message to all clients except mine, so that they are aware of the new
     * client in the chat room.
     *
     * @throws IOException When broadcasting fails.
     */
    private void informOthersOfMyPresence() throws IOException {
        LOGGER.info(String.format("Sending information of my (%s) presence to others ", myClient));
        for (ClientData otherClient : clients.values()) {
            if (otherClient != myClient) {
                otherClient.getConnection().send(new Message.Builder().from(myClient.getName()).type(Message.Type
                        .JOINED).build());
            }
        }
    }

    /**
     * Sends {@code Message.Type.JOINED} messages with other clients' names, so that my client know who else is
     * present in the chat room. Messages order is defined by joined date order.
     *
     * @throws IOException When sending fails.
     */
    private void informClientOfOthersPresence() throws IOException {
        // Sort clients with joinedDate key.
        TreeSet<ClientData> sortedClients = new TreeSet<ClientData>(new Comparator<ClientData>() {
            @Override
            public int compare(ClientData o1, ClientData o2) {
                return o1.getJoinedDate().compareTo(o2.getJoinedDate());
            }
        });
        sortedClients.addAll(clients.values());
        LOGGER.info("Sending information of others presence to " + myClient);
        for (ClientData otherClient : sortedClients) {
            if (otherClient != myClient) {
                send(new Message.Builder().date(otherClient.getJoinedDate()).type(Message.Type.JOINED).from
                        (otherClient.getName()).build());
            }
        }
    }

    private void send(Message message) throws IOException {
        send(message, null);
    }

    private void send(Message message, ClientData toWhom) throws IOException {
        if (toWhom != null) {
            toWhom.getConnection().send(message);
        } else {
            myClient.getConnection().send(message);
        }
    }

    private void broadcast(Message message) throws IOException {
        LOGGER.info("Broadcasting message to others: " + message);
        for (ClientData otherClient : clients.values()) {
            otherClient.getConnection().send(message);
        }
    }

}

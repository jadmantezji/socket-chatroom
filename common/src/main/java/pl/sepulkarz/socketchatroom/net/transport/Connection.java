package pl.sepulkarz.socketchatroom.net.transport;

import pl.sepulkarz.socketchatroom.net.data.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * The entry point to all client-server communication. This is essentially an implementation of text-based
 * communication protocol.
 */
public class Connection {

    /**
     * In future versions this should be moved to configuration of client and server separately.
     */
    public static final int DEFAULT_PORT = 22222;

    private final static Logger LOGGER = Logger.getLogger(Connection.class.getName());

    private final static DateFormat DATE_FORMATTER = DateFormat.getDateTimeInstance(
            DateFormat.LONG,
            DateFormat.LONG,
            Locale.ROOT);

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream());
    }

    /**
     * Reads from socket and reconstructs a message.
     *
     * @return The reconstructed message.
     * @throws java.net.SocketException
     * @throws IOException              When readng fails. {@code java.net.SocketException} (more specialized
     *                                  exception) is thrown
     *                                  when the socked has been closed - and this can be expected by some client
     *                                  thread.
     * @throws ParseException
     */
    public Message receive() throws IOException, ParseException {
        LOGGER.fine("Waiting for a message from " + socket);
        String dateString = reader.readLine();
        String type = reader.readLine();
        String from = reader.readLine();
        String to = reader.readLine();
        String text = reader.readLine();
        if (dateString != null && type != null) {
            Message message = new Message.Builder().date(DATE_FORMATTER.parse(dateString)).type(Message.Type.valueOf
                    (type)).from(from).to(to).text(text).build();
            LOGGER.info(String.format("Message from %s received: %s", socket, message));
            return message;
        } else {
            return null;
        }
    }

    /**
     * Writes a message to socket.
     *
     * @param message The message.
     * @throws IOException When writing fails.
     */
    public void send(Message message) throws IOException {
        LOGGER.fine(String.format("Sending to %s unicast message: %s", socket, message));
        writer.println(DATE_FORMATTER.format(message.getDate()));
        writer.println(message.getType() != null ? message.getType() : "INVALID");
        writer.println(message.getFrom() != null ? message.getFrom() : "");
        writer.println(message.getTo() != null ? message.getTo() : "");
        writer.println(message.getText() != null ? message.getText() : "");
        writer.flush();
    }

    public void shutdownStreams() throws IOException {
        socket.shutdownInput();
        socket.shutdownOutput();
    }

    public void close() throws IOException {
        writer.close();
        reader.close();
        socket.close();
    }

    public boolean isPossibleToWrite() {
        return !socket.isOutputShutdown();
    }

    public SocketAddress getRemoteAddress() {
        return socket.getRemoteSocketAddress();
    }

    @Override
    public String toString() {
        return String.format("[Connection|remoteAddress:%s]", socket.getRemoteSocketAddress());
    }

}

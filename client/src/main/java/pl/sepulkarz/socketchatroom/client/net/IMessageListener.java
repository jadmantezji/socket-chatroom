package pl.sepulkarz.socketchatroom.client.net;

import pl.sepulkarz.socketchatroom.net.data.Message;

import java.util.Date;

/**
 * Listener interface for messages in general and private chat rooms.
 */
public interface IMessageListener {

    /**
     * {@code Message.Type.NORMAL} message arrived.
     *
     * @param message {@code Message.Type.NORMAL} message.
     */
    void messageArrived(Message message);

    /**
     * Client joined.
     *
     * @param when Date of the event.
     * @param who  The name of the client.
     */
    void joined(Date when, String who);

    /**
     * Client left.
     *
     * @param when Date of the event.
     * @param who  The name of the client.
     */
    void left(Date when, String who);

}

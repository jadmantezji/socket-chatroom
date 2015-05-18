package pl.sepulkarz.socketchatroom.client.net;

/**
 * Listener interface for login-to-server related messages.
 */
public interface ILoginListener {

    /**
     * Client has been accepted by server.
     */
    void loginSuccessful();

    /**
     * Server has rejected chosen user name.
     */
    void rejectedUserName();

    /**
     * Unexpected connection error has happened.
     */
    void connectionError();

}

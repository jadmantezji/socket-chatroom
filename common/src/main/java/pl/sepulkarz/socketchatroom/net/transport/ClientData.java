package pl.sepulkarz.socketchatroom.net.transport;

import java.util.Date;

/**
 * A representation of client with connection state and controller included.
 */
public class ClientData {

    private Connection connection;
    private Date joinedDate;
    private String name;

    /**
     * Only connection is obligatory, other fields can be filled later.
     *
     * @param connection Controller part of the protocol.
     */
    public ClientData(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public Date getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(Date joinedDate) {
        this.joinedDate = joinedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("[ClientData|connection:%s|name:%s|joinedDate:%s]", connection, name, joinedDate);
    }

}


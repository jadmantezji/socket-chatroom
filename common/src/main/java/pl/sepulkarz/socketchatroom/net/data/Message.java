package pl.sepulkarz.socketchatroom.net.data;

import java.util.Date;

/**
 * A representation of message - data format part of the communication protocol. This type is immutable.
 */
public class Message {

    /**
     * Broadcast indication should be stored in {@code to} field.
     */
    public static String BROADCAST = "!BROADCAST";
    private Date date;
    private Type type;
    private String from;
    private String to;
    private String text;

    private Message(Date date, Type type, String from, String to, String text) {
        this.date = date;
        this.type = type;
        this.from = from;
        this.to = to;
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public Type getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public boolean isBroadcast() {
        return BROADCAST.equals(to);
    }

    @Override
    public String toString() {
        return String.format("[Message|date:%tc|type:%s|from:%s|to:%s|text:%s]", date, type, from, to, text);
    }

    public enum Type {
        NORMAL, HELLO, JOINED, LEFT, LOGIN_SUCCESSFUL, REJECTED_USER_NAME;
    }

    /**
     * Construction of {@code Message} is only possible with this builder type.
     */
    public static class Builder {

        private Date date = new Date();
        private String from = "";
        private String to = "";
        private Type type = Type.NORMAL;
        private String text = "";

        public Builder date(Date date) {
            this.date = date;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Message build() {
            return new Message(date, type, from, to, text);
        }
    }

}

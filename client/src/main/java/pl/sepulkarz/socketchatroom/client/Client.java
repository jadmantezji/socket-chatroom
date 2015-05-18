package pl.sepulkarz.socketchatroom.client;

import pl.sepulkarz.socketchatroom.client.gui.WelcomeFrame;
import pl.sepulkarz.socketchatroom.client.net.Communicator;
import pl.sepulkarz.socketchatroom.net.transport.Connection;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chat room client. Creates communicator and welcome window.
 */
public class Client {

	private final static Logger LOGGER = Logger.getLogger(Client.class.getName());

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Please provide server address as a parameter.");
		} else {
			try {
				new WelcomeFrame(new Communicator(args[0], Connection.DEFAULT_PORT));
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Could not create connection", e);
			}
		}
	}

}

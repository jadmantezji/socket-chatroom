package pl.sepulkarz.socketchatroom.server;

import pl.sepulkarz.socketchatroom.net.transport.Connection;
import pl.sepulkarz.socketchatroom.server.net.ClientServingThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chat room server. Accepts new connections from clients and runs threads serving them.
 */
public class Server {

	private final static Logger LOGGER = Logger.getLogger(Server.class.getName());

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		// TODO: Restrict the size of this thread pool.
		ExecutorService executorService = Executors.newCachedThreadPool();
		try {
			serverSocket = new ServerSocket(Connection.DEFAULT_PORT);
			LOGGER.info("Waiting for clients on " + serverSocket);
			while (true) {
				Socket socket = serverSocket.accept();
				executorService.submit(new ClientServingThread(socket));
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Socket opening failure", e);
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Socket closing failure", e);
			}
		}
	}

}

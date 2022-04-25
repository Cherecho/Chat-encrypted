package server;

import java.net.Socket;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class mainserver {

	private ServerSocket serverSocket;

	public mainserver(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public void startServer() {

		try {
			while (!serverSocket.isClosed()) {
				Socket socket = serverSocket.accept();
				System.out.println("A new client has connected!");
				ClientHandler clientHandler = new ClientHandler(socket);

				Thread thread = new Thread(clientHandler);
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeServerSocket() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {

		ServerSocket serverScocket = new ServerSocket(3000);
		mainserver server = new mainserver(serverScocket);
		server.startServer();

	}

}

package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client {

	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String username;

	private static Cipher cipher;
	private static String key = "1234";

	public Client(Socket socket, String username) {
		try {
			this.socket = socket;
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.username = username;
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
			e.printStackTrace();
		}
	}

	public void sendMessage() {
		try {
			bufferedWriter.write(username);
			bufferedWriter.newLine();
			bufferedWriter.flush();

			Scanner scanner = new Scanner(System.in);
			while (socket.isConnected()) {
				String messageToSend = scanner.nextLine();
				bufferedWriter.write(encrypt(key, username + ": " + messageToSend));
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
			e.printStackTrace();
		}

	}

	public void listenForMessage() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String msgFromGroupChat;

				while (socket.isConnected()) {
					try {
						msgFromGroupChat = bufferedReader.readLine();
						System.out.println(decrypt(key, msgFromGroupChat));
					} catch (IOException e) {
						closeEverything(socket, bufferedReader, bufferedWriter);
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String encrypt(final String secret, final String data) {

		byte[] decodedKey = Base64.getDecoder().decode(secret);

		try {
			Cipher cipher = Cipher.getInstance("AES");
			SecretKey originalKey = new SecretKeySpec(Arrays.copyOf(decodedKey, 16), "AES");
			cipher.init(Cipher.ENCRYPT_MODE, originalKey);
			byte[] cipherText = cipher.doFinal(data.getBytes("UTF-8"));
			return Base64.getEncoder().encodeToString(cipherText);
		} catch (Exception e) {
			throw new RuntimeException("Error occured while encrypting data", e);
		}

	}

	public static String decrypt(final String secret, final String encryptedString) {

		byte[] decodedKey = Base64.getDecoder().decode(secret);

		try {
			Cipher cipher = Cipher.getInstance("AES");
			SecretKey originalKey = new SecretKeySpec(Arrays.copyOf(decodedKey, 16), "AES");
			cipher.init(Cipher.DECRYPT_MODE, originalKey);
			byte[] cipherText = cipher.doFinal(Base64.getDecoder().decode(encryptedString));
			return new String(cipherText);
		} catch (Exception e) {
			throw new RuntimeException("Error occured while decrypting data", e);
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchPaddingException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter you username for the group chat: ");
		String username = scanner.nextLine();
		Socket socket = new Socket("localhost", 3000);
		Client client = new Client(socket, username);
		client.listenForMessage();
		client.sendMessage();
	}

}

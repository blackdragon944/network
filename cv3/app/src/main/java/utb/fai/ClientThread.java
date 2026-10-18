package utb.fai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class ClientThread extends Thread {

    private static final String LINE_END = "\r\n";

    private final Socket clientSocket;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        SocketAddress address = clientSocket.getRemoteSocketAddress();
        System.out.println("Client connected: " + address);

        try (Socket socket = clientSocket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            OutputStream out = socket.getOutputStream();

            String line;
            while ((line = in.readLine()) != null) {
                out.write((line + LINE_END).getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Connection with " + address + " lost: " + e.getMessage());
        }

        System.out.println("Client disconnected: " + address);
    }
}

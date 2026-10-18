package utb.fai;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class TelnetClient {

    private static final int CONNECT_TIMEOUT_MS = 5000;

    private final String serverIp;
    private final int port;

    public TelnetClient(String serverIp, int port) {
        this.serverIp = serverIp;
        this.port = port;
    }

    public void run() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverIp, port), CONNECT_TIMEOUT_MS);
            System.err.println("Connected to " + serverIp + ":" + port + " (type /QUIT to exit)");

            AtomicBoolean running = new AtomicBoolean(true);

            Thread receiver = new Thread(new SocketReceiver(socket, running), "socket-receiver");
            Thread sender = new Thread(new ConsoleSender(socket, running), "console-sender");
            sender.setDaemon(true);

            receiver.start();
            sender.start();

            receiver.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

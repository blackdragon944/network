package utb.fai;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple telnet client. Connects to a server and runs two independent threads:
 * one forwards everything received from the socket to standard output, the other
 * sends lines typed on standard input to the socket.
 */
public class TelnetClient {

    private static final int CONNECT_TIMEOUT_MS = 5000;

    private final String serverIp;
    private final int port;

    public TelnetClient(String serverIp, int port) {
        this.serverIp = serverIp;
        this.port = port;
    }

    /**
     * Connects to the server and blocks until the session ends, i.e. the user types
     * {@code /QUIT}, standard input reaches EOF, or the server closes the connection.
     *
     * @throws IOException if the connection cannot be established
     */
    public void run() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverIp, port), CONNECT_TIMEOUT_MS);
            System.err.println("Connected to " + serverIp + ":" + port + " (type /QUIT to exit)");

            AtomicBoolean running = new AtomicBoolean(true);

            Thread receiver = new Thread(new SocketReceiver(socket, running), "socket-receiver");
            Thread sender = new Thread(new ConsoleSender(socket, running), "console-sender");
            // The sender blocks on System.in, which cannot be interrupted; as a daemon
            // it does not keep the JVM alive once the server closes the connection.
            sender.setDaemon(true);

            receiver.start();
            sender.start();

            receiver.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

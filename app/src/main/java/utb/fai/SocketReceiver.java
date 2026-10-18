package utb.fai;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reads data from the socket and writes it unchanged to standard output.
 *
 * <p>Uses {@link InputStream#available()} so the loop never blocks in {@code read()}
 * and can stop as soon as {@code running} is cleared. When no data is waiting, a
 * 1 ms probe read detects whether the server has closed the connection, and the
 * thread then sleeps for {@value #POLL_INTERVAL_MS} ms to keep CPU load low.
 */
class SocketReceiver implements Runnable {

    private static final int POLL_INTERVAL_MS = 20;
    private static final int PROBE_TIMEOUT_MS = 1;
    private static final int BUFFER_SIZE = 4096;

    private final Socket socket;
    private final AtomicBoolean running;

    SocketReceiver(Socket socket, AtomicBoolean running) {
        this.socket = socket;
        this.running = running;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            socket.setSoTimeout(PROBE_TIMEOUT_MS);
            InputStream in = socket.getInputStream();

            while (running.get()) {
                int available = in.available();
                if (available > 0) {
                    int count = in.read(buffer, 0, Math.min(available, buffer.length));
                    write(buffer, count);
                } else if (!probe(in)) {
                    System.err.println("Connection closed by server.");
                    break;
                } else {
                    Thread.sleep(POLL_INTERVAL_MS);
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Connection lost: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            running.set(false);
        }
    }

    /**
     * Checks whether the connection is still open, printing a byte if one arrived meanwhile.
     *
     * @return {@code false} if the server has closed the connection
     */
    private boolean probe(InputStream in) throws IOException {
        try {
            int b = in.read();
            if (b == -1) {
                return false;
            }
            System.out.write(b);
            System.out.flush();
        } catch (SocketTimeoutException e) {
            // no data yet, connection is still open
        }
        return true;
    }

    private void write(byte[] data, int length) {
        System.out.write(data, 0, length);
        System.out.flush();
    }
}

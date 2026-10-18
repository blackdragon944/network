package utb.fai;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private boolean probe(InputStream in) throws IOException {
        try {
            int b = in.read();
            if (b == -1) {
                return false;
            }
            System.out.write(b);
            System.out.flush();
        } catch (SocketTimeoutException e) {
        }
        return true;
    }

    private void write(byte[] data, int length) {
        System.out.write(data, 0, length);
        System.out.flush();
    }
}

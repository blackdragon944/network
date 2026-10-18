package utb.fai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

class ConsoleSender implements Runnable {

    private static final String QUIT_COMMAND = "/QUIT";
    private static final String LINE_END = "\r\n";

    private final Socket socket;
    private final AtomicBoolean running;

    ConsoleSender(Socket socket, AtomicBoolean running) {
        this.socket = socket;
        this.running = running;
    }

    @Override
    public void run() {
        try {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            OutputStream out = socket.getOutputStream();

            String line;
            while (running.get() && (line = console.readLine()) != null) {
                if (QUIT_COMMAND.equals(line.strip())) {
                    break;
                }
                out.write((line + LINE_END).getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Connection lost: " + e.getMessage());
            }
        } finally {
            running.set(false);
        }
    }
}

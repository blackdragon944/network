package utb.fai;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App {

    private static final int DEFAULT_PORT = 12345;
    private static final int DEFAULT_MAX_THREADS = 10;

    public static void main(String[] args) {
        if (args.length > 2) {
            exitWithError("Usage: java -jar app.jar [port] [max_threads]");
        }

        int port = args.length > 0 ? parseNumber(args[0], "port", 1, 65535) : DEFAULT_PORT;
        int maxThreads = args.length > 1
                ? parseNumber(args[1], "max_threads", 1, Integer.MAX_VALUE)
                : DEFAULT_MAX_THREADS;

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Echo server listening on port " + port + " (max " + maxThreads + " clients)");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (executor.getActiveCount() >= maxThreads) {
                    System.out.println("Client " + clientSocket.getRemoteSocketAddress()
                            + " is waiting, all " + maxThreads + " slots are busy");
                }
                executor.execute(new ClientThread(clientSocket));
            }
        } catch (BindException e) {
            exitWithError("Port " + port + " is already in use");
        } catch (IOException e) {
            exitWithError("Server error: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }

    private static int parseNumber(String value, String name, int min, int max) {
        try {
            int number = Integer.parseInt(value);
            if (number >= min && number <= max) {
                return number;
            }
        } catch (NumberFormatException ignored) {
        }
        exitWithError("Invalid " + name + ": " + value + " (expected a number between " + min + " and " + max + ")");
        return -1;
    }

    private static void exitWithError(String message) {
        System.err.println(message);
        System.exit(1);
    }
}

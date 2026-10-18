package utb.fai;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class App {

    public static void main(String[] args) {
        if (args.length != 2) {
            exitWithError("Usage: java -jar app.jar <host> <port>");
        }

        String host = args[0];
        int port = parsePort(args[1]);

        try {
            new TelnetClient(host, port).run();
        } catch (UnknownHostException e) {
            exitWithError("Unknown host: " + host);
        } catch (ConnectException e) {
            exitWithError("Connection refused: " + host + ":" + port);
        } catch (SocketTimeoutException e) {
            exitWithError("Connection timed out: " + host + ":" + port);
        } catch (IOException e) {
            exitWithError("Connection error: " + e.getMessage());
        }
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port >= 1 && port <= 65535) {
                return port;
            }
        } catch (NumberFormatException ignored) {
        }
        exitWithError("Invalid port: " + value + " (expected a number between 1 and 65535)");
        return -1;
    }

    private static void exitWithError(String message) {
        System.err.println(message);
        System.exit(1);
    }
}

package utb.fai;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Entry point of the telnet client.
 *
 * <p>Usage: {@code java -jar app.jar <host> <port>}
 */
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

    /** Parses and validates the port argument, exiting with an error message if it is invalid. */
    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port >= 1 && port <= 65535) {
                return port;
            }
        } catch (NumberFormatException ignored) {
            // falls through to the error below
        }
        exitWithError("Invalid port: " + value + " (expected a number between 1 and 65535)");
        return -1; // unreachable
    }

    private static void exitWithError(String message) {
        System.err.println(message);
        System.exit(1);
    }
}

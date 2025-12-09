
import ui.ChessClient;

public class MainClient {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        int port = 8080;
        if (args.length == 1) {
            serverUrl = args[0];
            // Extract port from URL if provided
            String[] parts = serverUrl.split(":");
            if (parts.length == 3) {
                port = Integer.parseInt(parts[2]);
            }

        }

        try {
            new ChessClient(port, serverUrl).run();

        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
    }
}

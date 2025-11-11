
import server.Server;
import ui.ChessClient;

public class MainClient {
    public static void main(String[] args) {
//        String serverUrl = "http://localhost:8080";
        Server server = new Server();
        var port = server.run(0);
//        if (args.length == 1) {
//            serverUrl = args[0];
//        }

        try {
            new ChessClient(port).run();


        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
    }
}

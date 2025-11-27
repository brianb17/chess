import server.Server;

public class Main {
    public static void main(String[] args) {
        var server = new Server();
        server.run(8080);  // Or use args if required
    }
}
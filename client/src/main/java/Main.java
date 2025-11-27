import chess.*;
import client.ServerFacade;
import ui.PreloginUI;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        System.out.println("Server running on port " + port);

        ServerFacade facade = new ServerFacade(port);
        PreloginUI prelogin = new PreloginUI(facade);
        prelogin.run();
    }
}
import client.ServerFacade;
import ui.PreloginUI;
import ui.PostloginUI;
import result.LoginResult;

public class Main {
    private static final String SERVER_URL = "http://localhost:8080";

    public static void main(String[] args) {
        ServerFacade server = new ServerFacade(SERVER_URL);
        runApplication(server);
    }

    private static void runApplication(ServerFacade server) {
        boolean running = true;
        while (running) {
            PreloginUI preloginUI = new PreloginUI(server);
            LoginResult loginResult = preloginUI.run();

            if (loginResult != null) {
                PostloginUI postloginUI = new PostloginUI(server, loginResult.authToken());
                boolean loggedIn = postloginUI.run();
                if (!loggedIn) {
                    continue;
                }
            }
            running = false;
        }

        System.out.println("Thank you for using the Chess Client. Goodbye!");
    }
}
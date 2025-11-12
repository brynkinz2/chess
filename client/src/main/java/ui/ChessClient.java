package ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import client.ServerFacade;
import model.AuthData;
import model.GameData;

import static ui.EscapeSequences.*;

public class ChessClient {
    private static ServerFacade serverFacade;
    private static Scanner scanner;
    private String authToken = null;
    private String username = null;
    private List<GameData> currGamesList = null;


    public ChessClient(int port) {
        serverFacade = new ServerFacade(port);
        scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println(WHITE_KING +  "Welcome to 240 chess. Type help to get started." + WHITE_KING + "\n");

        while (true) {
            if (authToken == null) {
                preLoginUI();
            } else {
                postLoginUI();
            }
        }
    }

    public void preLoginUI() {
        System.out.print(RESET_TEXT_COLOR + "[LOGGED_OUT] ");
        var result = "";
        printPrompt();
        String input = scanner.nextLine();
        eval(input);
    }

    public void postLoginUI() {
        System.out.print("[LOGGED_IN] ");
        var result = "";
        printPrompt();
        String input = scanner.nextLine();
        eval(input);
    }

    private void preLoginHelp() {
        System.out.println(SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR + " - to create an account");
        System.out.println(SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR + " - to login");
        System.out.println(SET_TEXT_COLOR_BLUE + "quit" + RESET_TEXT_COLOR + " - playing chess");
        System.out.println(SET_TEXT_COLOR_BLUE + "help" + RESET_TEXT_COLOR + " - with possible commands");
    }

    private void postLoginHelp() {
        System.out.println(SET_TEXT_COLOR_BLUE + "create <NAME>" + RESET_TEXT_COLOR + " - a game");
        System.out.println(SET_TEXT_COLOR_BLUE + "list" + RESET_TEXT_COLOR + " - games");
        System.out.println(SET_TEXT_COLOR_BLUE + "join <ID> [WHITE|BLACK]" + RESET_TEXT_COLOR + " - a game");
        System.out.println(SET_TEXT_COLOR_BLUE + "observe <ID>" + RESET_TEXT_COLOR + " - a game");
        System.out.println(SET_TEXT_COLOR_BLUE + "logout" + RESET_TEXT_COLOR + " when you are done");
        System.out.println(SET_TEXT_COLOR_BLUE + "quit" + RESET_TEXT_COLOR + " - playing chess");
        System.out.println(SET_TEXT_COLOR_BLUE + "help" + RESET_TEXT_COLOR + " - with possible commands");
    }

    private void printPrompt() {
        System.out.print(">>> " + SET_TEXT_COLOR_GREEN);
    }

    private void register(String[] params) throws IOException {
        AuthData auth = serverFacade.register(params[0], params[1]);
        authToken = auth.authToken();
        System.out.println(RESET_TEXT_COLOR + "Logged in as " + params[0]);
    }
    private void login(String[] params) throws IOException {
        AuthData auth = serverFacade.login(params[0], params[1]);
        authToken = auth.authToken();
        System.out.println(RESET_TEXT_COLOR + "Logged in as " + params[0]);
    }

    private void createGame(String[] params) throws IOException {
        GameData game = serverFacade.createGame(params[0], authToken);
        System.out.println(RESET_TEXT_COLOR + "Created game " + params[0]);
    }

    private void listGames() throws IOException {
        currGamesList = serverFacade.listGames(authToken).games();
        for (int i = 0; i < currGamesList.size(); i++) {
            System.out.println(RESET_TEXT_COLOR + "Game " + i + ": " + currGamesList.get(i).gameID());
        }
    }

    private void joinGame(String[] params) throws IOException {
        GameData game = currGamesList.get(Integer.parseInt(params[0]));
        serverFacade.joinGame(game.gameID(), params[1].toUpperCase(), authToken);
        DrawChessGame drawBoard = new DrawChessGame();
        boolean whitePerspective = params[1].equalsIgnoreCase("WHITE");
        drawBoard.drawBoard(game.game().getBoard(), whitePerspective);
    }

    private void observeGame(String[] params) throws IOException {
        int gameIndex = Integer.parseInt(params[0]);
        if (gameIndex < 0 || gameIndex >= currGamesList.size()) {
            throw new IndexOutOfBoundsException();
        }
        GameData game = currGamesList.get(Integer.parseInt(params[0]));
        DrawChessGame drawBoard = new DrawChessGame();
        drawBoard.drawBoard(game.game().getBoard(), true);
    }

    private void logout() throws IOException {
        serverFacade.logout(authToken);
        authToken = null;
    }

    public void eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            switch (command) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "quit" -> System.exit(0);
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "logout" -> logout();
                default -> {
                    if (authToken == null) {
                        preLoginHelp();
                    } else {
                        postLoginHelp();
                    }
                }
            }
        } catch (Exception e) {
            System.out.print(SET_TEXT_COLOR_MAGENTA + "Error: ");
            if (e instanceof IndexOutOfBoundsException) {
                System.out.println("invalid game ID entered. Type list to see game ID's for input.");
            }
            else if (e.getMessage().contains("401")) {
                System.out.println("Please login first.");
            }
            else if (e.getMessage().contains("403")) {
                System.out.println("Color already taken.");
            }
            else if (e.getMessage().contains("400") || e.getMessage().contains("input")) {
                System.out.println("Invalid input. Please review the possible commands and needed information.");
                if (authToken == null) {
                    preLoginHelp();
                } else {
                    postLoginHelp();
                }
            } else {
                System.out.println(e.getMessage());
            }
            System.out.println(RESET_TEXT_COLOR);
        }
    }
}

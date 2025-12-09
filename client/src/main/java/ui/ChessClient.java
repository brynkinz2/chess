package ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import chess.ChessGame;
import client.NotificationHandler;
import client.ServerFacade;
import client.WebSocketSender;
import model.AuthData;
import model.GameData;
import websocket.messages.LoadGame;
import websocket.messages.ServerMessage;

import static ui.EscapeSequences.*;

public class ChessClient implements NotificationHandler {
    private static ServerFacade serverFacade;
    private static WebSocketSender ws;
    private static Scanner scanner;
    private String authToken = null;
    private String username = null;
    private List<GameData> currGamesList = null;
    private ChessGame game;
    private ChessGame.TeamColor playerColor = null; // null if observer
    private Integer currentGameID = null;
    private boolean inGameplay = false;


    public ChessClient(int port, String serverURL) throws Exception {
        serverFacade = new ServerFacade(port);
        scanner = new Scanner(System.in);
        ws = new WebSocketSender(serverURL, this);
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGame loadGame = (LoadGame) message;
                this.game = loadGame.getGame();
                redrawBoard();
            }
            case NOTIFICATION -> {
                websocket.messages.Notification notification = (websocket.messages.Notification) message;
                System.out.println("\n" + SET_TEXT_COLOR_YELLOW + notification.getNotificationMessage() + RESET_TEXT_COLOR);
                if (inGameplay) {
                    printGameplayPrompt();
                }
            }
            case ERROR ->  {
                websocket.messages.Error error = (websocket.messages.Error) message;
                System.err.println("\n" + SET_TEXT_COLOR_RED + error.getErrorMessage() + RESET_TEXT_COLOR);
                if (inGameplay) {
                    printGameplayPrompt();
                }
            }
        }
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

    private void printGameplayPrompt() {}

    private void register(String[] params) throws IOException {
        AuthData auth = serverFacade.register(params[0], params[1]);
        authToken = auth.authToken();
        System.out.println(RESET_TEXT_COLOR + "Logged in as " + params[0]);
    }
    private void login(String[] params) throws IOException {
        if (authToken != null) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Already logged in. Logout first to login as a different user." + RESET_TEXT_COLOR);
            return;
        }
        AuthData auth = serverFacade.login(params[0], params[1]);
        authToken = auth.authToken();
        System.out.println(RESET_TEXT_COLOR + "Logged in as " + params[0]);
    }

    private void createGame(String[] params) throws IOException {
        serverFacade.createGame(params[0], authToken);
        System.out.println(RESET_TEXT_COLOR + "Created game: " + params[0]);
    }

    private void listGames() throws IOException {
        currGamesList = serverFacade.listGames(authToken).games();
        if (currGamesList.isEmpty()) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "No games created. Type help to see how to create a game." + RESET_TEXT_COLOR);
            return;
        }
        for (int i = 0; i < currGamesList.size(); i++) {
            System.out.println(RESET_TEXT_COLOR + "Game " + i + ": " + currGamesList.get(i).gameName());
        }
    }

    private void joinGame(String[] params) throws IOException {
        if (currGamesList.isEmpty()) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Type list to see game IDs for joining/observing." + RESET_TEXT_COLOR);
        }
        GameData game = currGamesList.get(Integer.parseInt(params[0]));
        serverFacade.joinGame(game.gameID(), params[1].toUpperCase(), authToken);
        DrawChessGame drawBoard = new DrawChessGame();
        boolean whitePerspective = params[1].equalsIgnoreCase("WHITE");
        drawBoard.drawBoard(game.game().getBoard(), whitePerspective);
    }

    private void observeGame(String[] params) throws IOException {
        if (currGamesList.isEmpty()) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Type list to see game IDs for joining/observing." + RESET_TEXT_COLOR);
        }
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
        String currCommand = "";
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            currCommand = command;
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
                if (currCommand.equals("join") || currCommand.equals("observe")) {
                    System.out.println("Invalid input for joining/observing game. Type list to see game ID's for input or help to see formatting.");
                }
                else {
                    System.out.println("Invalid input for " + currCommand + ". Type help to see how to " + currCommand + ".");
                }
            }
            else if (e.getMessage().contains("401")) {
                if (currCommand.equals("register") || currCommand.equals("login")) {
                    System.out.println("Invalid username or password.");
                }
                else if (currCommand.equals("logout")) {
                    System.out.println("Not logged in yet. Type help to see how to login.");
                }
                else {
                    System.out.println("Please login first to " + currCommand + ".");
                }
            }
            else if (e.getMessage().contains("403")) {
                if (currCommand.equals("register")) {
                    System.out.println("Username already taken. Please choose a new username and try again.");
                }
                else if (currCommand.equals("join")) {
                    System.out.println("Color already taken.");
                }
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

    public void redrawBoard() {}
}

package ui;

import chess.*;
import client.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class GameplayUI implements NotificationHandler {
    private final Scanner scanner;
    private final WebSocketSender ws;
    private final String authToken;
    private final int gameID;
    private ChessGame game;
    private ChessGame.TeamColor playerColor;
    private DrawChessGame boardDrawer = new DrawChessGame();

    public GameplayUI(String serverUrl, String authToken, int gameID, ChessGame.TeamColor playerColor) throws Exception {
        this.scanner = new Scanner(System.in);
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;

        // Connect to WebSocket
        String wsUrl = serverUrl.replace("http://", "ws://").replace("https://", "wss://") + "/ws";
        this.ws = new WebSocketSender(wsUrl, this);

        // Send CONNECT command
        ws.connect(authToken, gameID);
    }

    @Override
    public void notify(ServerMessage message) {
        switch(message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGame loadGame = (LoadGame) message;
                this.game = loadGame.getGame();
                System.out.println(" ");
                drawBoard();
            }
            case NOTIFICATION -> {
                Notification notification = (Notification) message;
                System.out.println(SET_TEXT_COLOR_BLUE + "\n" + notification.getNotificationMessage() + RESET_TEXT_COLOR);
            }
            case ERROR -> {
                websocket.messages.Error error = (websocket.messages.Error) message;
                System.out.println(SET_TEXT_COLOR_RED + error.getErrorMessage() + RESET_TEXT_COLOR);
            }
        }
        System.out.print("\n>>> " + SET_TEXT_COLOR_GREEN);
    }

    public void run() {
        System.out.println(RESET_TEXT_COLOR + "You have joined the game. Type help for commands.");

        boolean running = true;
        while (running) {
            System.out.print(">>> " + SET_TEXT_COLOR_GREEN);
            String input = scanner.nextLine().trim().toLowerCase();
            System.out.print(RESET_TEXT_COLOR);
            String[] tokens = input.split("\\s+");
            try {
                switch (tokens[0]) {
                    case "help" -> help();
                    case "redraw" -> drawBoard();
                    case "leave" -> {
                        ws.leave(authToken, gameID);
                        running = false;
                    }
//                    case "move" -> makeMove(tokens);
//                    case "resign" -> resign();
//                    case "highlight" -> highlightMoves(tokens);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            ws.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println(RESET_TEXT_COLOR + "Left the game.");
    }

    public void help() {
        System.out.println(SET_TEXT_COLOR_BLUE + "redraw" + RESET_TEXT_COLOR + " - the chess board");
        System.out.println(SET_TEXT_COLOR_BLUE + "leave" + RESET_TEXT_COLOR + " - the game");
        System.out.println(SET_TEXT_COLOR_BLUE + "move <from> <to>" + RESET_TEXT_COLOR + " - make a move (e.g., 'move e2 e4')");
        System.out.println(SET_TEXT_COLOR_BLUE + "resign" + RESET_TEXT_COLOR + " - from the game");
        System.out.println(SET_TEXT_COLOR_BLUE + "highlight <position>" + RESET_TEXT_COLOR + " - highlight legal moves");
        System.out.println(SET_TEXT_COLOR_BLUE + "help" + RESET_TEXT_COLOR + " - with possible commands");
    }

    public void drawBoard() {}
}

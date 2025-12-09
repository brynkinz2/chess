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
    private boolean boardLoaded = false;

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
        System.out.flush();
        System.out.print(">>> " + SET_TEXT_COLOR_GREEN);
        boardLoaded = true;
    }

    public void run() {
        System.out.println(RESET_TEXT_COLOR + "Connecting to game...");

        // Wait for initial LOAD_GAME (with timeout)
        int waitCount = 0;
        while (!boardLoaded && waitCount < 50) { // 5 second timeout
            try {
                Thread.sleep(100);
                waitCount++;
            } catch (InterruptedException e) {
                break;
            }
        }
        System.out.println(RESET_TEXT_COLOR + "You have joined the game. Type help for commands.");
        boardLoaded = false;
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
                        Thread.sleep(20);
                        running = false;
                    }
                    case "move" -> makeMove(tokens);
                    case "resign" -> resign();
                    case "highlight" -> highlightMoves(tokens);
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
        System.out.println(RESET_TEXT_COLOR + "You have left the game.");
    }

    public void help() {
        System.out.println(SET_TEXT_COLOR_BLUE + "redraw" + RESET_TEXT_COLOR + " - the chess board");
        System.out.println(SET_TEXT_COLOR_BLUE + "leave" + RESET_TEXT_COLOR + " - the game");
        System.out.println(SET_TEXT_COLOR_BLUE + "move <from> <to>" + RESET_TEXT_COLOR + " - make a move (e.g., 'move e2 e4')");
        System.out.println(SET_TEXT_COLOR_BLUE + "resign" + RESET_TEXT_COLOR + " - from the game");
        System.out.println(SET_TEXT_COLOR_BLUE + "highlight <position>" + RESET_TEXT_COLOR + " - highlight legal moves");
        System.out.println(SET_TEXT_COLOR_BLUE + "help" + RESET_TEXT_COLOR + " - with possible commands");
    }

    public void drawBoard() {
        if (game == null) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Game has not loaded." + RESET_TEXT_COLOR);
        }
        DrawChessGame drawBoard = new DrawChessGame();
        boolean whitePerspective = (playerColor.equals(ChessGame.TeamColor.WHITE) || playerColor == null);
        drawBoard.drawBoard(game.getBoard(), whitePerspective);
    }

    private void makeMove(String[] tokens) throws Exception {
        // If observer, cannot make move.
        if (playerColor == null) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Observers cannot make moves." + RESET_TEXT_COLOR);
        }

        if (tokens.length < 3) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Invalid number of arguments." + RESET_TEXT_COLOR);
            System.out.println("To make a move use the following format: 'move <from> <to> [promotion]'");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Example with out promotion: 'move e2 e4'");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Example with promotion: 'move e2 e4 q'" + RESET_TEXT_COLOR);
            return;
        }

        ChessPosition start = parsePosition(tokens[1]);
        ChessPosition end = parsePosition(tokens[2]);

        if (start == null || end == null) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Invalid position format." + RESET_TEXT_COLOR);
        }

        //Check if promotion piece needed
        ChessPiece.PieceType promotion = null;
        if (tokens.length >= 4) {
            promotion = parsePromotion(tokens[3]);
            if (promotion == null) {
                System.out.println(SET_TEXT_COLOR_MAGENTA + "Invalid promotion piece. Use q/r/b/n" + RESET_TEXT_COLOR);
                return;
            }
        }


        try {
            ChessMove move = new ChessMove(start, end, promotion);
            ws.makeMove(authToken, gameID, move);
        } catch (Exception e) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Invalid move. Is this piece promoting? Use q/r/b/n to promote or highlight to see valid moves.");
            System.out.println("Promotion format: 'move <from> <to> [promotion]' (e.g. move e7 e2 q)" + RESET_TEXT_COLOR);
        }
    }

    private void resign() throws Exception {
        // Observers cannot resign
        if (playerColor == null) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Observers cannot resign." + RESET_TEXT_COLOR);
            return;
        }
        while (true) {
            System.out.print(SET_TEXT_COLOR_RED + "Are you sure you would like to resign? (y/n)" + RESET_TEXT_COLOR);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y")) {
                break;
            } else if (input.equals("n")) {
                return;
            } else {
                System.out.println("Invalid input please type 'y' (yes) or 'n' (no)");
            }
        }

        ws.resign(authToken, gameID);
    }

    private void highlightMoves(String[] tokens) throws Exception {
        if (tokens.length < 2) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Invalid number of arguments. Type help to see options" + RESET_TEXT_COLOR);
            return;
        }

        ChessPosition position = parsePosition(tokens[1]);
        if (position == null) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Invalid position format." + RESET_TEXT_COLOR);
            return;
        }

        if (game == null) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "Game has not loaded." + RESET_TEXT_COLOR);
            return;
        }

        try {
            var validMoves = game.validMoves(position);
            DrawChessGame drawBoard = new DrawChessGame();
            drawBoard.drawWithHighlights(validMoves);
            boolean whitePerspective = (playerColor.equals(ChessGame.TeamColor.WHITE) || playerColor == null);
            drawBoard.drawBoard(game.getBoard(), whitePerspective);
        } catch (Exception e) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "No chess piece in that position." + RESET_TEXT_COLOR);
        }
    }

    public ChessPosition parsePosition(String position) {
        if (position.length() != 2) {
            return null;
        }

        char col = position.charAt(0);
        char row = position.charAt(1);

        if (col < 'a' || col > 'h' || row < '1' || row > '8') {
            return null;
        }

        int colNum = col - 'a' + 1;
        int rowNum = row - '0';

        return new ChessPosition(rowNum, colNum);
    }

    public ChessPiece.PieceType parsePromotion(String promotion) {
        return switch (promotion.toLowerCase()) {
            case "q" ->  ChessPiece.PieceType.QUEEN;
            case "r" ->  ChessPiece.PieceType.ROOK;
            case "b" ->  ChessPiece.PieceType.BISHOP;
            case "n" ->  ChessPiece.PieceType.KNIGHT;
            default ->  null;
        };
    }
}

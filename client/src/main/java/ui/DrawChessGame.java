package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static ui.EscapeSequences.*;

public class DrawChessGame {
    // Board dimensions.
    public static final int BOARD_WIDTH = 8;
    public static final int SQUARE_SIZE_IN_PADDED_CHARS = 7;
    public static ChessBoard board;
    public static String playerColor;
    public static ChessGame game;
    private static Collection<ChessMove> moves = new ArrayList<>();

    public void drawBoard(ChessBoard board, boolean whitePerspective) {
        DrawChessGame.board = board;
        if (whitePerspective) {
            drawBoardFromWhitePerspective();
        } else {
            drawBoardFromBlackPerspective();
        }
        moves = new ArrayList<>();
    }

    public void drawWithHighlights(Collection<ChessMove> moves) {
        this.moves = moves;
    }

    private void drawBoardFromWhitePerspective() {
        drawBorder(true);

        for (int i = 8; i >= 1; i--) {
            drawRow(i, true);
        }
        drawBorder(true);

    }

    private void drawBoardFromBlackPerspective() {
        drawBorder(false);

        for (int i = 1; i <= BOARD_WIDTH; i++) {
            drawRow(i, false);
        }

        drawBorder(false);
    }

    private void drawBorder(boolean whitePerspective) {
        System.out.print(SET_BG_COLOR_DARK_GREEN + RESET_TEXT_COLOR + "   ");

        if (whitePerspective) {
            for (char c = 'a'; c <= 'h'; c++) {
                System.out.print(" " + c + " ");
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                System.out.print(" " + c + " ");
            }
        }
        System.out.println("   " + RESET_TEXT_COLOR + RESET_BG_COLOR);
    }

    private void drawRow(int row, boolean whitePerspective) {
        System.out.print(SET_BG_COLOR_DARK_GREEN + RESET_TEXT_COLOR + " " + row + " ");
        System.out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);

        if (whitePerspective) {
            for (int i = 1; i <= BOARD_WIDTH; i++) {
                drawSquare(row, i);
            }
        } else {
            for (int i = 8; i >= 1; i--) {
                drawSquare(row, i);
            }
        }
        System.out.println(SET_BG_COLOR_DARK_GREEN + RESET_TEXT_COLOR + " " + row + " " + RESET_BG_COLOR);
    }

    private void drawSquare(int row, int col) {
        ChessPosition currentPos = new ChessPosition(row, col);
        boolean lightSquare = (row + col) % 2 != 0;
        String squareBG = lightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK;

        for (ChessMove move : moves) {
            if (move.getStartPosition().equals(currentPos)) {
                squareBG = SET_BG_COLOR_YELLOW;
            } else if (move.getEndPosition().equals(currentPos)) {
                if (lightSquare) {
                    squareBG = SET_BG_COLOR_GREEN;
                } else {
                    squareBG = SET_BG_COLOR_DARK_GREEN;
                }
            }
        }

        ChessPiece piece = board.getPiece(new ChessPosition(row, col));

        System.out.print(squareBG);

        if (piece == null) {
            System.out.print("   ");
        } else {
            boolean pieceWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
            String pieceColor = pieceWhite ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
            String prettyPiece = getPiece(piece);
            System.out.print(pieceColor + prettyPiece);
        }


    }

    private String getPiece(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> BLACK_KING;
            case QUEEN -> BLACK_QUEEN;
            case KNIGHT -> BLACK_KNIGHT;
            case BISHOP -> BLACK_BISHOP;
            case ROOK -> BLACK_ROOK;
            case PAWN -> BLACK_PAWN;
            default -> null;
        };
    }

}


package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    TeamColor turn;
    ChessBoard board;
    List<ChessPiece> capturedPieces;

    public ChessGame() {

    }

    public ChessGame(ChessBoard board, TeamColor turn) {
        this.board = new ChessBoard(board.getBoard());
        this.capturedPieces = new ArrayList<>();
        this.turn = turn;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece currPiece = board.getPiece(startPosition);
        Collection<ChessMove> allMoves = currPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        // for each move that can be made, check if it will put the king under attack
        for (ChessMove move : allMoves) {
            ChessGame copyGame = new ChessGame(board, turn);
            copyGame.checkMove(move);
            if (!copyGame.isInCheck(copyGame.turn)) {
                validMoves.add(move);
            }
        }
        return currPiece.pieceMoves(board, startPosition);
    }

    public void checkMove(ChessMove move) {
        TeamColor opposingTeam;
        if (turn == TeamColor.BLACK) {
            opposingTeam = TeamColor.WHITE;
        } else {
            opposingTeam = TeamColor.BLACK;
        }

        ChessPiece currPiece = board.getPiece(move.getStartPosition());
        // get valid moves
        ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
        if (capturedPiece != null) {
            capturedPieces.add(capturedPiece);
        }
        board.setPiece(move.getEndPosition(), currPiece);
        board.setPiece(move.getStartPosition(), null);
        turn = opposingTeam;
        return;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        TeamColor opposingTeam;
        if (turn == TeamColor.BLACK) {
            opposingTeam = TeamColor.WHITE;
        }
        else { opposingTeam = TeamColor.BLACK; }

        ChessPiece currPiece = board.getPiece(move.getStartPosition());
        // if the starting position of the move does not have a piece there, throw error
        if (currPiece == null) {
            throw new InvalidMoveException("Invalid move: No piece found");
        }
        if (currPiece.getTeamColor() == opposingTeam) {
            throw new InvalidMoveException("Invalid move: That piece is not on your team");
        }
        // get valid moves
        Collection<ChessMove> currMoves = validMoves(move.getStartPosition());
        // check if the desired move is in the list of valid moves. if it is, make the move
        for (ChessMove currMove : currMoves) {
            // move the piece to that place, capture a piece if there was one there, change the team turn
            // check if pawn needs to be promoted
            if (currMove.equals(move)) {
                ChessPiece capturedPiece = board.getPiece(currMove.getEndPosition());
                if (capturedPiece != null) {
                    capturedPieces.add(capturedPiece);
                }
                board.setPiece(currMove.getEndPosition(), currPiece);
                board.setPiece(currMove.getStartPosition(), null);
                turn = opposingTeam;
                return;
            }
        }
        throw new InvalidMoveException("Invalid move: " + move);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessPiece> opposingPieces = new ArrayList<>();
        ChessPosition teamKing = null;
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece currPiece = board.getPiece(new ChessPosition(i, j));
                if (currPiece == null) { continue; }
                if (currPiece.getTeamColor() == teamColor && currPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    teamKing = new ChessPosition(i, j);
                }
            }
        }
        if (teamKing == null) {
            throw new RuntimeException("No King found");
        }
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                ChessPiece currPiece = board.getPiece(new ChessPosition(i, j));
                if (currPiece == null) { continue; }
                if (currPiece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = currPiece.pieceMoves(board, new ChessPosition(i, j));
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(teamKing)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
//        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        // TODO: go through all pieces to see if there are no possible movese
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turn == chessGame.turn && Objects.equals(board, chessGame.board) && Objects.equals(capturedPieces, chessGame.capturedPieces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board, capturedPieces);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "turn=" + turn +
                ", board=" + board +
                ", capturedPieces=" + capturedPieces +
                '}';
    }
}

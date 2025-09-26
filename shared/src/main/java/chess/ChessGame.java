package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    TeamColor turn;
    ChessBoard board;

    public ChessGame() {

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
        // TODO: Take out moves that are invalid.
        return currPiece.pieceMoves(board, startPosition);
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
            if (move == currMove) {
                // TODO: finish this
                turn = opposingTeam;
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
        throw new RuntimeException("Not implemented");
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
}

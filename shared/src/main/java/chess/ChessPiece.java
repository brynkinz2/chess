package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.KING) {}
        else if (piece.getPieceType() == PieceType.QUEEN) {}
        else if (piece.getPieceType() == PieceType.BISHOP) {
            int currRow = myPosition.getRow();
            int currCol = myPosition.getColumn();
            List<ChessMove> valid = new ArrayList<>();
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    if (i == currRow && j == currCol) {
                        continue;
                    }
                    else if (Math.abs(i - currRow) == Math.abs(j - currCol)) {
                        valid.add(new ChessMove(myPosition, new ChessPosition(i, j), null));
                    }
                }
            }
            return valid;
//            return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1, 8), null));
        }
        else if (piece.getPieceType() == PieceType.KNIGHT) {}
        else if (piece.getPieceType() == PieceType.ROOK) {}
        else if (piece.getPieceType() == PieceType.PAWN) {}

        return List.of();
    }
}

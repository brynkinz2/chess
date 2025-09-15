package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
        return pieceColor;
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
        List<ChessMove> valid = new ArrayList<>();
        int currRow = myPosition.getRow();
        int currCol = myPosition.getColumn();

        if (piece.getPieceType() == PieceType.KING) {
            // set up available directions
            int[][] moves = {{0,-1}, {1,-1}, {1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}};

            // go through each possible direction
            for (int[] move : moves) {
                int rowMove = move[0];
                int colMove = move[1];

                int checkRow = currRow + rowMove;
                int checkCol = currCol + colMove;

                //if out of bounds, move on to next iteration
                if (!(checkRow > 0 && checkRow <= 8 && checkCol > 0 && checkCol <= 8)) {
                    continue;
                }

                // Get piece in desired move spot
                ChessPiece placePiece = board.getPiece(new ChessPosition(checkRow, checkCol));
                // if there is no piece in that spot, add it as a valid move
                if (placePiece == null) {
                    valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                }
                // if there is a piece of the opposite team there, check if it is the King. If not, add as valid move
                else if (pieceColor != placePiece.getTeamColor()) {
                    if (placePiece.getPieceType() != type) {
                        valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                    }
                }
            }
        }
        else if (piece.getPieceType() == PieceType.QUEEN) {
            // set up available directions
            int[][] moves = {{0,-1}, {1,-1}, {1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}};

            // go through each possible direction
            for (int[] move : moves) {
                int rowMove = move[0];
                int colMove = move[1];

                int checkRow = currRow + rowMove;
                int checkCol = currCol + colMove;

                // if out of bounds, move on to next direction check
                while (checkRow > 0 && checkRow <= 8 && checkCol > 0 && checkCol <= 8) {

                    // Get piece in desired move spot
                    ChessPiece placePiece = board.getPiece(new ChessPosition(checkRow, checkCol));
                    // if there is no piece in that spot, add it as a valid move
                    if (placePiece == null) {
                        valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                    }
                    // if there is a piece of the opposite team there, check if it is the King. If not, add as valid move
                    else if (pieceColor != placePiece.getTeamColor()) {
                        valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                        break;
                    }
                    else {
                        break;
                    }
                    checkRow += rowMove;
                    checkCol += colMove;
                }
            }
        }
        else if (piece.getPieceType() == PieceType.BISHOP) {
            // set up available directions
            int[][] moves = {{1,1}, {-1,1}, {1,-1}, {-1,-1}};

            for (int[] move : moves) {
                int rowMove = move[0];
                int colMove = move[1];

                int checkRow = currRow + rowMove;
                int checkCol = currCol + colMove;

                while (checkRow > 0 && checkRow <= 8 && checkCol > 0 && checkCol <= 8) {
                    if (board.getPiece(new ChessPosition(checkRow, checkCol)) == null) {
                        valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                    }
                    else if (pieceColor != board.getPiece(new ChessPosition(checkRow, checkCol)).getTeamColor()) {
                        valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                        break;
                    }
                    else {
                        break;
                    }
                    checkRow += rowMove;
                    checkCol += colMove;
                }
            }
        }
        else if (piece.getPieceType() == PieceType.KNIGHT) {
            // set up available directions
            int[][] moves = {{1,-2}, {2,-1}, {2,1}, {1,2}, {-1,2}, {-2,1}, {-2,-1}, {-1,-2}};

            // go through each possible direction
            for (int[] move : moves) {
                int rowMove = move[0];
                int colMove = move[1];

                int checkRow = currRow + rowMove;
                int checkCol = currCol + colMove;

                //if out of bounds, move on to next iteration
                if (!(checkRow > 0 && checkRow <= 8 && checkCol > 0 && checkCol <= 8)) {
                    continue;
                }

                // Get piece in desired move spot
                ChessPiece placePiece = board.getPiece(new ChessPosition(checkRow, checkCol));
                // if there is no piece in that spot, add it as a valid move
                if (placePiece == null) {
                    valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                }
                // if there is a piece of the opposite team there, check if it is the King. If not, add as valid move
                else if (pieceColor != placePiece.getTeamColor()) {
                    valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                }
            }
        }
        else if (piece.getPieceType() == PieceType.ROOK) {
            // set up available directions
            int[][] moves = {{0,-1}, {1,0}, {0,1}, {-1,0}};

            for (int[] move : moves) {
                int rowMove = move[0];
                int colMove = move[1];

                int checkRow = currRow + rowMove;
                int checkCol = currCol + colMove;

                while (checkRow > 0 && checkRow <= 8 && checkCol > 0 && checkCol <= 8) {
                    if (board.getPiece(new ChessPosition(checkRow, checkCol)) == null) {
                        valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                    }
                    else if (pieceColor != board.getPiece(new ChessPosition(checkRow, checkCol)).getTeamColor()) {
                        valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                        break;
                    }
                    else {
                        break;
                    }
                    checkRow += rowMove;
                    checkCol += colMove;
                }
            }
        }
        else if (piece.getPieceType() == PieceType.PAWN) {
            // set up available directions
            int[][] moves = {{1,0}, {1,-1}, {1,1}};
            int promotionRow = 7;
            if (piece.pieceColor == ChessGame.TeamColor.BLACK) {
                moves[0][0] = -1;
                moves[1][0] = -1;
                moves[2][0] = -1;
                promotionRow = 2;
            }
            int iter = 0;           // keep track of which iteration to deal with capturing.

            // go through each possible direction
            for (int[] move : moves) {
                int rowMove = move[0];
                int colMove = move[1];

                int checkRow = currRow + rowMove;
                int checkCol = currCol + colMove;

                //if out of bounds, move on to next iteration
                if (!(checkRow > 0 && checkRow <= 8 && checkCol > 0 && checkCol <= 8)) {
                    continue;
                }

                // Get piece in desired move spot
                ChessPiece placePiece = board.getPiece(new ChessPosition(checkRow, checkCol));
                // if we are on the first iteration, pawn is moving forward and cannot capture
                if (iter == 0) {
                    // if there is no piece in that spot, add it as a valid move
                    if (placePiece == null) {
                        if (currRow == promotionRow) {
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.QUEEN));
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.ROOK));
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.BISHOP));
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.KNIGHT));
                        }
                        else {
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                        }
                        iter++;
                    }
                    // check if it is the pawns first move. if it is, it can move 1 or 2 forward
                    if ((pieceColor == ChessGame.TeamColor.WHITE && currRow == 2)|| (pieceColor == ChessGame.TeamColor.BLACK && currRow == 7) && (iter == 1)) {
                        ChessPosition checkPosition = new ChessPosition(checkRow + rowMove, checkCol);
                        if (board.getPiece(checkPosition) == null) {
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow + rowMove, checkCol), null));
                        }
                    }


                }
                else {
                    // if there is a piece of the opposite team there, add as valid move

                    if (placePiece != null && pieceColor != placePiece.getTeamColor()) {
                        if (currRow == promotionRow) {
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.QUEEN));
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.ROOK));
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.BISHOP));
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.KNIGHT));
                        }
                        else {
                            valid.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                        }
                    }
                }
                iter++;
            }
        }

        return valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }
}


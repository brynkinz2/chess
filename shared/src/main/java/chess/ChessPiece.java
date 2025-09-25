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
        Collection<ChessMove> moves = new ArrayList<>();
        int currRow = myPosition.getRow();
        int currCol = myPosition.getColumn();
        if (piece.getPieceType() == PieceType.KING) {
            // directions this piece can move
            int directions[][] = {{1,-1}, {1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}, {0,-1}};

            // loop through those direction changes, add to current row and column
            for (int direction[] : directions) {
                int checkRow =  currRow + direction[0];
                int checkCol =  currCol + direction[1];

                //check that we are within the bounds of the board
                if (checkRow <= 0 || checkCol <= 0 || checkRow > 8 || checkCol > 8) {
                    continue;
                }
                ChessPiece checkPiece = board.getPiece(new ChessPosition(checkRow, checkCol));
                // if there is no piece in that spot, add it to valid moves
                if (checkPiece == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                }
                // if there is a piece that belongs to the other team, capture it
                else if(checkPiece.pieceColor != pieceColor) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                }
            }
        }
        else if (piece.getPieceType() == PieceType.QUEEN) {
            // directions this piece can move
            int directions[][] = {{1,-1}, {1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}, {0,-1}};

            // loop through those direction changes, add to current row and column
            for (int direction[] : directions) {
                int checkRow =  currRow + direction[0];
                int checkCol =  currCol + direction[1];

                // add another iteration of that direction until out of bounds
                while (checkRow > 0 && checkCol > 0 && checkRow <= 8 && checkCol <= 8) {
                    ChessPiece checkPiece = board.getPiece(new ChessPosition(checkRow, checkCol));
                    // if there is no piece in that spot, add it to valid moves
                    if (checkPiece == null) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                    }
                    // if there is a piece that belongs to the other team, capture it and break, we cannot go further
                    else if(checkPiece.pieceColor != pieceColor) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                        break;
                    }
                    else {
                        break;
                    }
                    checkRow += direction[0];
                    checkCol += direction[1];
                }
            }
        }
        else if (piece.getPieceType() == PieceType.BISHOP) {
            // directions this piece can move
            int directions[][] = {{1,-1}, {1,1}, {-1,1}, {-1,-1}};

            // loop through those direction changes, add to current row and column
            for (int direction[] : directions) {
                int checkRow =  currRow + direction[0];
                int checkCol =  currCol + direction[1];

                // add another iteration of that direction until out of bounds
                while (checkRow > 0 && checkCol > 0 && checkRow <= 8 && checkCol <= 8) {
                    ChessPiece checkPiece = board.getPiece(new ChessPosition(checkRow, checkCol));
                    // if there is no piece in that spot, add it to valid moves
                    if (checkPiece == null) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                    }
                    // if there is a piece that belongs to the other team, capture it and break, we cannot go further
                    else if(checkPiece.pieceColor != pieceColor) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                        break;
                    }
                    else {
                        break;
                    }
                    checkRow += direction[0];
                    checkCol += direction[1];
                }
            }
        }
        else if (piece.getPieceType() == PieceType.KNIGHT) {
            int directions[][] = {{1, -2}, {2,-1}, {2,1}, {1,2}, {-1, 2}, {-2, 1}, {-2,-1}, {-1,-2}};

            // loop through those direction changes, add to current row and column
            for (int direction[] : directions) {
                int checkRow =  currRow + direction[0];
                int checkCol =  currCol + direction[1];

                //check that we are within the bounds of the board
                if (checkRow <= 0 || checkCol <= 0 || checkRow > 8 || checkCol > 8) {
                    continue;
                }
                ChessPiece checkPiece = board.getPiece(new ChessPosition(checkRow, checkCol));
                // if there is no piece in that spot, add it to valid moves
                if (checkPiece == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                }
                // if there is a piece that belongs to the other team, capture it
                else if(checkPiece.pieceColor != pieceColor) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                }
            }
        }
        else if (piece.getPieceType() == PieceType.ROOK) {
            // directions this piece can move
            int directions[][] = {{0,-1}, {1,0}, {0,1}, {-1,0}};

            // loop through those direction changes, add to current row and column
            for (int direction[] : directions) {
                int checkRow =  currRow + direction[0];
                int checkCol =  currCol + direction[1];

                // add another iteration of that direction until out of bounds
                while (checkRow > 0 && checkCol > 0 && checkRow <= 8 && checkCol <= 8) {
                    ChessPiece checkPiece = board.getPiece(new ChessPosition(checkRow, checkCol));
                    // if there is no piece in that spot, add it to valid moves
                    if (checkPiece == null) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                    }
                    // if there is a piece that belongs to the other team, capture it and break, we cannot go further
                    else if(checkPiece.pieceColor != pieceColor) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                        break;
                    }
                    else {
                        break;
                    }
                    checkRow += direction[0];
                    checkCol += direction[1];
                }
            }
        }
        else if (piece.getPieceType() == PieceType.PAWN) {
            boolean canPromote = false;
            // if on the last block before the edge, they can promote if they move
            if ((piece.pieceColor == ChessGame.TeamColor.WHITE && currRow == 7) || (piece.pieceColor == ChessGame.TeamColor.BLACK && currRow == 2)) {
                canPromote = true;
            }
            boolean firstMove = false;
            // if they are in starting row, it is their first move
            if ((piece.pieceColor == ChessGame.TeamColor.WHITE && currRow == 2) || (piece.pieceColor == ChessGame.TeamColor.BLACK && currRow == 7)) {
                firstMove = true;
            }
            int directions[][] = {{1, -1}, {1, 1}};
            if (piece.pieceColor == ChessGame.TeamColor.BLACK) {
                for (int direction[] : directions) { direction[0] = -1; }
            }

            int checkRow =  currRow + directions[0][0];
            int checkCol =  currCol;
            if (board.getPiece(new ChessPosition(checkRow, checkCol)) == null) {
                if (canPromote) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.ROOK));
                }
                else {
                    moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                }
                if (firstMove && board.getPiece(new ChessPosition(checkRow + directions[0][0], checkCol)) == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(checkRow + directions[0][0], checkCol), null));
                }
            }

            for (int direction[] : directions) {
                checkRow =  currRow + direction[0];
                checkCol =  currCol + direction[1];
                //check that we are within the bounds of the board
                if (checkRow <= 0 || checkCol <= 0 || checkRow > 8 || checkCol > 8) {
                    continue;
                }
                ChessPiece checkPiece = board.getPiece(new ChessPosition(checkRow, checkCol));
                if (checkPiece == null) {
                    continue;
                }
                if (checkPiece.pieceColor != pieceColor) {
                    if (canPromote) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), PieceType.ROOK));
                    }
                    else {
                        moves.add(new ChessMove(myPosition, new ChessPosition(checkRow, checkCol), null));
                    }
                }
            }
        }
        return moves;
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


package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private PieceType type;
    private ChessPosition position;

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
        Set<ChessMove> moves = new HashSet<>();

        // Bishop logic
        int[] bishopDirections = {-1, 1}; // Directions for diagonals: up-left, up-right, down-left, down-right
        if (this.getPieceType() == PieceType.BISHOP) {
            for (int rowDirection : bishopDirections) {
                for (int colDirection : bishopDirections) {
                    int currentRow = myPosition.getRow();
                    int currentCol = myPosition.getColumn();

                    // Check each diagonal direction
                    while (true) {
                        currentRow += rowDirection;
                        currentCol += colDirection;

                        // Check if the new position is valid
                        if (currentRow <= 0 || currentRow > 8 || currentCol <= 0 || currentCol > 8) {
                            break; // Move is outside of the board
                        }

                        ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
                        ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                        // Check if there's a piece in the new position
                        if (pieceAtNewPosition != null) {
                            // If it's an enemy piece, we can capture it
                            if (pieceAtNewPosition.getTeamColor() != this.getTeamColor()) {
                                moves.add(new ChessMove(myPosition, newPosition, null));
                            }
                            break; // Cannot move past a piece, whether it's an ally or an enemy
                        } else {
                            // The space is empty, so it's a valid move
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }
                }
            }
        }

        // King logic
        if (this.getPieceType() == PieceType.KING) {
            int[][] kingDirections = {
                    {-1, -1}, {-1, 0}, {-1, 1}, // Diagonally up and straight up
                    {0, -1}, {0, 1},            // Left and right
                    {1, -1}, {1, 0}, {1, 1}     // Diagonally down and straight down
            };

            for (int[] direction : kingDirections) {
                int newRow = myPosition.getRow() + direction[0];
                int newCol = myPosition.getColumn() + direction[1];

                // Check if new position is on the board
                if (newRow > 0 && newRow <= 8 && newCol > 0 && newCol <= 8) {
                    ChessPosition newPosition = new ChessPosition(newRow, newCol);
                    ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                    // Check if the position is either empty or occupied by an enemy piece
                    if (pieceAtNewPosition == null || pieceAtNewPosition.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }

        // Knight logic
        if (this.getPieceType() == PieceType.KNIGHT) {
            int[][] knightMoves = {
                    {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, // Upwards L-shapes
                    {1, -2}, {1, 2}, {2, -1}, {2, 1}      // Downwards L-shapes
            };

            for (int[] move : knightMoves) {
                int newRow = myPosition.getRow() + move[0];
                int newCol = myPosition.getColumn() + move[1];

                // Check if new position is on the board
                if (newRow > 0 && newRow <= 8 && newCol > 0 && newCol <= 8) {
                    ChessPosition newPosition = new ChessPosition(newRow, newCol);
                    ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                    // Check if the position is either empty or occupied by an enemy piece
                    if (pieceAtNewPosition == null || pieceAtNewPosition.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }

        // Queen logic
        if (this.getPieceType() == PieceType.QUEEN) {
            // The queen combines the movement of the rook and bishop
            addLinearMoves(moves, board, myPosition, -1, 0); // Vertical up
            addLinearMoves(moves, board, myPosition, 1, 0);  // Vertical down
            addLinearMoves(moves, board, myPosition, 0, -1); // Horizontal left
            addLinearMoves(moves, board, myPosition, 0, 1);  // Horizontal right
            addLinearMoves(moves, board, myPosition, -1, -1); // Diagonal up-left
            addLinearMoves(moves, board, myPosition, -1, 1);  // Diagonal up-right
            addLinearMoves(moves, board, myPosition, 1, -1);  // Diagonal down-left
            addLinearMoves(moves, board, myPosition, 1, 1);   // Diagonal down-right
        }




        return moves;
    }

    public void setPosition(ChessPosition position) {
        this.position = position;
    }

    private void addLinearMoves(Set<ChessMove> moves, ChessBoard board, ChessPosition startPosition, int rowDirection, int colDirection) {
        int currentRow = startPosition.getRow();
        int currentCol = startPosition.getColumn();

        while (true) {
            currentRow += rowDirection;
            currentCol += colDirection;

            if (currentRow <= 0 || currentRow > 8 || currentCol <= 0 || currentCol > 8) {
                break; // Move is outside of the board
            }

            ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
            ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

            if (pieceAtNewPosition != null) {
                if (pieceAtNewPosition.getTeamColor() != this.getTeamColor()) {
                    moves.add(new ChessMove(startPosition, newPosition, null)); // Can capture
                }
                break; // Blocked by a piece
            } else {
                moves.add(new ChessMove(startPosition, newPosition, null)); // Empty space
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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

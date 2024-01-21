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
        Set<ChessMove> moves = new HashSet<>();
        int[] directions = {-1, 1}; // Directions for diagonals: up-left, up-right, down-left, down-right

        if (this.getPieceType() == PieceType.BISHOP) {
            for (int rowDirection : directions) {
                for (int colDirection : directions) {
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

        // Logic for other piece types would go here...

        return moves;
    }
}

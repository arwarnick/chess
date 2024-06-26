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

        // Pawn logic
        if (this.getPieceType() == PieceType.PAWN) {
            int direction = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
            int startRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;

            // Check if the pawn is in the promotion row
            if (isPawnPromotionRow(myPosition)) {
                // Handle promotion moves only
                handlePawnPromotion(moves, board, myPosition);
            } else {
                // Single square move
                ChessPosition oneStep = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
                if (isPositionValid(oneStep) && board.getPiece(oneStep) == null) {
                    moves.add(new ChessMove(myPosition, oneStep, null));
                }

                // Double square move from start
                if (myPosition.getRow() == startRow) {
                    ChessPosition twoSteps = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());
                    if (isPositionValid(twoSteps) && board.getPiece(twoSteps) == null && board.getPiece(oneStep) == null) {
                        moves.add(new ChessMove(myPosition, twoSteps, null));
                    }
                }

                // Standard capture moves for pawn
                addPawnCaptureMoves(moves, board, myPosition, false);
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

        // Rook logic
        if (this.getPieceType() == PieceType.ROOK) {
            addLinearMoves(moves, board, myPosition, -1, 0); // Vertical up
            addLinearMoves(moves, board, myPosition, 1, 0);  // Vertical down
            addLinearMoves(moves, board, myPosition, 0, -1); // Horizontal left
            addLinearMoves(moves, board, myPosition, 0, 1);  // Horizontal right
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

    private boolean isPositionValid(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    private boolean isPawnPromotionRow(ChessPosition position) {
        int promotionRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;
        return position.getRow() == (this.getTeamColor() == ChessGame.TeamColor.WHITE ? 7 : 2);
    }

    private void handlePawnPromotion(Set<ChessMove> moves, ChessBoard board, ChessPosition myPosition) {
        int direction = getPawnDirection();
        // Promotion position
        ChessPosition promotionPosition = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (isPositionValid(promotionPosition) && board.getPiece(promotionPosition) == null) {
            addPromotionMoves(moves, myPosition, promotionPosition);
        }

        // Capture and promote
        addPawnCaptureMoves(moves, board, myPosition, true); // true indicates promotion
    }

    private int getPawnDirection() {
        return (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
    }

    private void addPromotionMoves(Set<ChessMove> moves, ChessPosition startPosition, ChessPosition endPosition) {
        // Add a move for each promotion type
        moves.add(new ChessMove(startPosition, endPosition, PieceType.QUEEN));
        moves.add(new ChessMove(startPosition, endPosition, PieceType.ROOK));
        moves.add(new ChessMove(startPosition, endPosition, PieceType.BISHOP));
        moves.add(new ChessMove(startPosition, endPosition, PieceType.KNIGHT));
    }

    private void addPawnCaptureMoves(Set<ChessMove> moves, ChessBoard board, ChessPosition myPosition, boolean isPromotion) {
        int[] captureCols = {myPosition.getColumn() - 1, myPosition.getColumn() + 1};
        for (int captureCol : captureCols) {
            ChessPosition capturePos = new ChessPosition(myPosition.getRow() + getPawnDirection(), captureCol);
            if (isPositionValid(capturePos)) {
                ChessPiece capturedPiece = board.getPiece(capturePos);
                if (capturedPiece != null && capturedPiece.getTeamColor() != this.getTeamColor()) {
                    if (isPromotion) {
                        addPromotionMoves(moves, myPosition, capturePos);
                    } else {
                        moves.add(new ChessMove(myPosition, capturePos, null));
                    }
                }
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

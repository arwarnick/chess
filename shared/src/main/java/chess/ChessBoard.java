package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board;

    public ChessBoard() {
        // Initialize an 8x8 array to represent the chessboard.
        // The board is empty initially.
        this.board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        // Convert the ChessPosition to array indices and place the piece.
        int row = position.getRow() - 1; // Assuming rows are 1-indexed.
        int col = position.getColumn() - 1; // Assuming columns are 1-indexed.
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            board[row][col] = piece;
        } else {
            // Optionally, you can throw an exception or handle this case if the position is out of bounds.
            System.out.println("Position is out of the chessboard bounds.");
        }
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        // Convert the ChessPosition to array indices and retrieve the piece.
        int row = position.getRow() - 1; // Assuming rows are 1-indexed.
        int col = position.getColumn() - 1; // Assuming columns are 1-indexed.
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) {
            return null; // Position is out of bounds of the board
        }
        return board[row][col];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        throw new RuntimeException("Not implemented");
    }
}

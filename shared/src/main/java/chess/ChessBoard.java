package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board;
    private ChessMove lastMove;

    public ChessBoard() {
        this.board = new ChessPiece[8][8];
    }

    public void setLastMove(ChessMove move) {
        this.lastMove = move;
    }

    public ChessMove getLastMove() {
        return this.lastMove;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        if (piece != null) {
            piece.setPosition(position); // Update the piece's position
        }
        // Convert the ChessPosition to array indices and place the piece.
        int row = position.getRow() - 1; // Assuming rows are 1-indexed.
        int col = position.getColumn() - 1; // Assuming columns are 1-indexed.
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            board[row][col] = piece;
        } else {
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
        // Clear the board
        for (int i = 0; i < 8; i++) {
            Arrays.fill(board[i], null);
        }

        // Set up the pieces
        setupRow(ChessGame.TeamColor.WHITE, 1); // White pawns
        setupRow(ChessGame.TeamColor.BLACK, 6); // Black pawns
        setupBackRow(ChessGame.TeamColor.WHITE, 0); // White back row
        setupBackRow(ChessGame.TeamColor.BLACK, 7); // Black back row
    }

    private void setupRow(ChessGame.TeamColor color, int row) {
        for (int col = 0; col < 8; col++) {
            board[row][col] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
        }
    }

    private void setupBackRow(ChessGame.TeamColor color, int row) {
        board[row][0] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        board[row][7] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        board[row][1] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        board[row][6] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        board[row][2] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        board[row][5] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        board[row][3] = new ChessPiece(color, ChessPiece.PieceType.QUEEN);
        board[row][4] = new ChessPiece(color, ChessPiece.PieceType.KING);
    }

    public ChessBoard deepCopy() {
        ChessBoard copy = new ChessBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (this.board[row][col] != null) {
                    copy.board[row][col] = new ChessPiece(this.board[row][col].getTeamColor(), this.board[row][col].getPieceType());
                }
            }
        }
        return copy;
    }

    public void movePiece(ChessPosition start, ChessPosition end) {
        ChessPiece piece = getPiece(start);
        if (piece != null) {
            addPiece(end, piece); // Move the piece to the new location
            addPiece(start, null); // Remove the piece from the original location
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ChessBoard other = (ChessBoard) obj;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = this.board[row][col];
                ChessPiece otherPiece = other.board[row][col];
                if (piece == null && otherPiece == null) continue;
                if (piece == null || otherPiece == null) return false;
                if (!piece.equals(otherPiece)) return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 7; row >= 0; row--) {
            sb.append(row + 1).append(" ");
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece == null) {
                    sb.append("- ");
                } else {
                    sb.append(pieceToChar(piece)).append(" ");
                }
            }
            sb.append("\n");
        }
        sb.append("  a b c d e f g h");
        return sb.toString();
    }

    private char pieceToChar(ChessPiece piece) {
        char c = switch (piece.getPieceType()) {
            case KING -> 'K';
            case QUEEN -> 'Q';
            case BISHOP -> 'B';
            case KNIGHT -> 'N';
            case ROOK -> 'R';
            case PAWN -> 'P';
        };
        return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? c : Character.toLowerCase(c);
    }
}

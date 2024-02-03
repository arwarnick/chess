package chess;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn = TeamColor.WHITE; // Default to white's turn at the start
    private ChessBoard board;

    public ChessGame() {
        this.board = new ChessBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
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
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return Collections.emptyList(); // No piece at the given position
        }

        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);
        // Here, need to filter out moves that would result in self-check, for example:
        // .filter(move -> !wouldResultInCheck(move))
        // For now, return all potential moves as calculated without further validation.

        return potentialMoves.stream()
                .filter(move -> isValidMove(move)) // Placeholder for future validation method
                .collect(Collectors.toSet());
    }

    private boolean isValidMove(ChessMove move) {
        // Future implementation to check for checks, pins, etc.
        // For now, simply return true to accept all moves from pieceMoves.
        return true;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // Retrieve the piece at the start position
        ChessPiece piece = board.getPiece(move.getStartPosition());

        // Check if there is a piece at the start position
        if (piece == null) {
            throw new InvalidMoveException("No piece at start position.");
        }

        // Check if it's the correct team's turn
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("It's not " + piece.getTeamColor() + "'s turn.");
        }

        // Validate the move based on piece type
        switch (piece.getPieceType()) {
            case PAWN:
                validatePawnMove(piece, move, board);
                break;
            case KNIGHT:
                validateKnightMove(piece, move, board);
                break;
            case BISHOP:
                validateBishopMove(piece, move, board);
                break;
            case ROOK:
                validateRookMove(piece, move, board);
                break;
            case QUEEN:
                validateQueenMove(piece, move, board);
                break;
            case KING:
                validateKingMove(piece, move, board);
                break;
            default:
                throw new InvalidMoveException("Invalid piece type");
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int promotionRow = piece.getTeamColor() == TeamColor.WHITE ? 8 : 1;
            if (move.getEndPosition().getRow() == promotionRow) {
                // Perform promotion
                if (move.getPromotionPiece() != null) {
                    // Replace pawn with new piece of specified type and same color
                    piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
                } else {
                    throw new InvalidMoveException("Promotion piece type must be specified.");
                }
            }
        }

        // Check if the end position is within the board limits
        if (!isValidPosition(move.getEndPosition())) {
            throw new InvalidMoveException("Invalid end position.");
        }

        // Assume the move is valid and proceed with further validations as needed
        // This includes checking for checks, valid paths, etc., which will implement later


        // Simulate the move
        ChessBoard simulatedBoard = this.board.deepCopy();
        simulatedBoard.addPiece(move.getEndPosition(), simulatedBoard.getPiece(move.getStartPosition()));
        simulatedBoard.addPiece(move.getStartPosition(), null); // Remove the piece from the start position

        // Set up a temporary ChessGame to check the state after the move
        ChessGame tempGame = new ChessGame();
        tempGame.setBoard(simulatedBoard);
        tempGame.setTeamTurn(this.teamTurn); // Assume the turn doesn't change for the simulation

        // Check if the move places or leaves the king in check
        if (tempGame.isInCheck(this.teamTurn)) {
            throw new InvalidMoveException("Move would result in check");
        }

        // For now, simply move the piece
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null); // Remove the piece from the start position

        // Switch turns
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    private void validatePawnMove(ChessPiece piece, ChessMove move, ChessBoard board) throws InvalidMoveException {
        int startRow = move.getStartPosition().getRow();
        int startColumn = move.getStartPosition().getColumn();
        int endRow = move.getEndPosition().getRow();
        int endColumn = move.getEndPosition().getColumn();
        int rowDiff = endRow - startRow;
        int colDiff = Math.abs(endColumn - startColumn);

        // Determine the direction based on the pawn's color
        int direction = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;

        // Check for standard move
        if (colDiff == 0 && ((direction == 1 && rowDiff == 1) || (direction == -1 && rowDiff == -1))) {
            // Move one square forward
            if (board.getPiece(move.getEndPosition()) != null) {
                throw new InvalidMoveException("Cannot move forward into occupied square");
            }
        } else if (colDiff == 0 && rowDiff == 2 * direction) {
            // Move two squares forward from the starting position
            if (!((startRow == 2 && piece.getTeamColor() == ChessGame.TeamColor.WHITE) ||
                    (startRow == 7 && piece.getTeamColor() == ChessGame.TeamColor.BLACK))) {
                throw new InvalidMoveException("Pawn can only move two squares forward from its starting position");
            }
            // Ensure path is clear
            if (board.getPiece(move.getEndPosition()) != null ||
                    board.getPiece(new ChessPosition(startRow + direction, startColumn)) != null) {
                throw new InvalidMoveException("Path is blocked for two square pawn move");
            }
        } else if (colDiff == 1 && Math.abs(rowDiff) == 1) {
            // Capture or en passant
            ChessPiece targetPiece = board.getPiece(move.getEndPosition());
            if (targetPiece == null || targetPiece.getTeamColor() == piece.getTeamColor()) {
                // Check for en passant conditions here if applicable, throw exception if not valid
                throw new InvalidMoveException("Invalid pawn capture move");
            }
        } else {
            // Not a valid pawn move
            throw new InvalidMoveException("Invalid pawn move");
        }

        // Check for promotion
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && endRow == 8) ||
                    (piece.getTeamColor() == ChessGame.TeamColor.BLACK && endRow == 1)) {
                if (move.getPromotionPiece() == null) {
                    throw new InvalidMoveException("Pawn promotion must specify the piece to promote to");
                }
            } else if (move.getPromotionPiece() != null) {
                throw new InvalidMoveException("Pawn promotion can only occur on the opposite end of the board");
            }
        }
    }


    private void validateKnightMove(ChessPiece piece, ChessMove move, ChessBoard board) throws InvalidMoveException {
        int startRow = move.getStartPosition().getRow();
        int startColumn = move.getStartPosition().getColumn();
        int endRow = move.getEndPosition().getRow();
        int endColumn = move.getEndPosition().getColumn();
        int rowDiff = Math.abs(endRow - startRow);
        int colDiff = Math.abs(endColumn - startColumn);

        // Check for the L-shape movement: 2 squares in one direction and 1 square in the other direction
        if (!((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2))) {
            throw new InvalidMoveException("Invalid knight move: Knights must move in an L-shape.");
        }

        // Check if the end position is occupied by a piece of the same color
        ChessPiece endPositionPiece = board.getPiece(move.getEndPosition());
        if (endPositionPiece != null && endPositionPiece.getTeamColor() == piece.getTeamColor()) {
            throw new InvalidMoveException("Invalid knight move: Cannot capture your own piece.");
        }
    }

    private void validateBishopMove(ChessPiece piece, ChessMove move, ChessBoard board) throws InvalidMoveException {
        int startRow = move.getStartPosition().getRow();
        int startColumn = move.getStartPosition().getColumn();
        int endRow = move.getEndPosition().getRow();
        int endColumn = move.getEndPosition().getColumn();

        // Bishops move diagonally, so the absolute difference between the row and column must be equal
        if (Math.abs(endRow - startRow) != Math.abs(endColumn - startColumn)) {
            throw new InvalidMoveException("Invalid bishop move: Bishops must move diagonally.");
        }

        // Check if the path is clear (no pieces between start and end positions)
        int rowDirection = Integer.signum(endRow - startRow);
        int columnDirection = Integer.signum(endColumn - startColumn);

        int currentRow = startRow + rowDirection;
        int currentColumn = startColumn + columnDirection;

        while (currentRow != endRow && currentColumn != endColumn) {
            if (board.getPiece(new ChessPosition(currentRow, currentColumn)) != null) {
                throw new InvalidMoveException("Invalid bishop move: Path is blocked.");
            }
            currentRow += rowDirection;
            currentColumn += columnDirection;
        }

        // Check if the end position is occupied by a piece of the same color
        ChessPiece endPositionPiece = board.getPiece(move.getEndPosition());
        if (endPositionPiece != null && endPositionPiece.getTeamColor() == piece.getTeamColor()) {
            throw new InvalidMoveException("Invalid bishop move: Cannot capture your own piece.");
        }
    }


    private void validateRookMove(ChessPiece piece, ChessMove move, ChessBoard board) throws InvalidMoveException {
        int startRow = move.getStartPosition().getRow();
        int startColumn = move.getStartPosition().getColumn();
        int endRow = move.getEndPosition().getRow();
        int endColumn = move.getEndPosition().getColumn();

        // Rooks move either horizontally or vertically, so either the row or the column must remain constant
        if (!((startRow == endRow) ^ (startColumn == endColumn))) {
            throw new InvalidMoveException("Invalid rook move: Rooks must move in a straight line either horizontally or vertically.");
        }

        // Determine the direction of the move
        int rowDirection = Integer.signum(endRow - startRow);
        int columnDirection = Integer.signum(endColumn - startColumn);

        // Check if the path is clear (no pieces between start and end positions)
        int currentRow = startRow + rowDirection;
        int currentColumn = startColumn + columnDirection;
        while ((currentRow != endRow || currentColumn != endColumn) && (currentRow >= 1 && currentRow <= 8 && currentColumn >= 1 && currentColumn <= 8)) {
            if (board.getPiece(new ChessPosition(currentRow, currentColumn)) != null) {
                throw new InvalidMoveException("Invalid rook move: Path is blocked.");
            }
            currentRow += rowDirection;
            currentColumn += columnDirection;
        }

        // Check if the end position is occupied by a piece of the same color
        ChessPiece endPositionPiece = board.getPiece(move.getEndPosition());
        if (endPositionPiece != null && endPositionPiece.getTeamColor() == piece.getTeamColor()) {
            throw new InvalidMoveException("Invalid rook move: Cannot capture your own piece.");
        }
    }

    private void validateQueenMove(ChessPiece piece, ChessMove move, ChessBoard board) throws InvalidMoveException {
        int startRow = move.getStartPosition().getRow();
        int startColumn = move.getStartPosition().getColumn();
        int endRow = move.getEndPosition().getRow();
        int endColumn = move.getEndPosition().getColumn();

        // The queen can move like both a rook and a bishop
        boolean isDiagonalMove = Math.abs(endRow - startRow) == Math.abs(endColumn - startColumn);
        boolean isStraightMove = startRow == endRow || startColumn == endColumn;

        if (!isDiagonalMove && !isStraightMove) {
            throw new InvalidMoveException("Invalid queen move: Queen must move in a straight line or diagonally.");
        }

        int rowDirection = Integer.signum(endRow - startRow);
        int columnDirection = Integer.signum(endColumn - startColumn);

        // Iterate through the path to ensure it's clear
        int currentRow = startRow + rowDirection;
        int currentColumn = startColumn + columnDirection;
        while ((currentRow != endRow || currentColumn != endColumn) && (currentRow >= 1 && currentRow <= 8 && currentColumn >= 1 && currentColumn <= 8)) {
            if (board.getPiece(new ChessPosition(currentRow, currentColumn)) != null) {
                throw new InvalidMoveException("Invalid queen move: Path is blocked.");
            }
            currentRow += rowDirection;
            currentColumn += columnDirection;
        }

        // Check if the end position is occupied by a piece of the same color
        ChessPiece endPositionPiece = board.getPiece(move.getEndPosition());
        if (endPositionPiece != null && endPositionPiece.getTeamColor() == piece.getTeamColor()) {
            throw new InvalidMoveException("Invalid queen move: Cannot capture your own piece.");
        }
    }

    private void validateKingMove(ChessPiece piece, ChessMove move, ChessBoard board) throws InvalidMoveException {
        int startRow = move.getStartPosition().getRow();
        int startColumn = move.getStartPosition().getColumn();
        int endRow = move.getEndPosition().getRow();
        int endColumn = move.getEndPosition().getColumn();

        // Calculate the difference in rows and columns to determine the move's legality
        int rowDifference = Math.abs(endRow - startRow);
        int columnDifference = Math.abs(endColumn - startColumn);

        // King can only move one square in any direction
        if (rowDifference > 1 || columnDifference > 1) {
            throw new InvalidMoveException("Invalid king move: King can move only one square in any direction.");
        }

        // Ensure the destination is not occupied by a friendly piece
        ChessPiece destinationPiece = board.getPiece(move.getEndPosition());
        if (destinationPiece != null && destinationPiece.getTeamColor() == piece.getTeamColor()) {
            throw new InvalidMoveException("Invalid king move: Cannot capture your own piece.");
        }

        // Additional checks for castling or moving into check could be implemented here
        // For simplicity, this example does not include those checks
    }

    private boolean isValidPosition(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        if (kingPosition == null) {
            return false; // If there's no king, technically not in check (shouldn't happen in a valid game).
        }

        // Check all opposing pieces to see if any can capture the king
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPosition position = new ChessPosition(row, column);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> potentialMoves = piece.pieceMoves(board, position);
                    for (ChessMove move : potentialMoves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true; // The king can be captured; it's in check.
                        }
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPosition position = new ChessPosition(row, column);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return position;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false; // Not in checkmate if not in check
        }

        // Iterate over all pieces of the teamColor to see if any move can remove the check
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPosition position = new ChessPosition(row, column);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> potentialMoves = piece.pieceMoves(board, position);
                    for (ChessMove move : potentialMoves) {
                        // Simulate each move to see if it would remove the check
                        if (!simulateMove(move)) {
                            return false; // Found a move that can remove the check, so not in checkmate
                        }
                    }
                }
            }
        }
        return true; // No move found to remove the check, so in checkmate
    }

    private boolean simulateMove(ChessMove move) {
        ChessBoard simulatedBoard = this.board.deepCopy();
        // Make the move on the simulated board
        simulatedBoard.addPiece(move.getEndPosition(), simulatedBoard.getPiece(move.getStartPosition()));
        simulatedBoard.addPiece(move.getStartPosition(), null); // Remove the piece from the start position

        // Set up a temporary ChessGame to check the state after the move
        ChessGame tempGame = new ChessGame();
        tempGame.setBoard(simulatedBoard);
        tempGame.setTeamTurn(this.teamTurn); // Keep the same turn for simulation

        // Return false if the move gets the team out of check, true otherwise
        return tempGame.isInCheck(this.teamTurn);
    }



    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false; // Can't be stalemate if in check
        }

        // Iterate over all pieces of the teamColor to see if any move is possible
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPosition position = new ChessPosition(row, column);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    for (ChessMove potentialMove : piece.pieceMoves(board, position)) {
                        // Simulate the move to check if it results in a state that's not check
                        if (!simulateMove(potentialMove)) {
                            return false; // Found at least one legal move, so not in stalemate
                        }
                    }
                }
            }
        }
        return true; // No legal moves found, so in stalemate
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
        return this.board;
    }
}

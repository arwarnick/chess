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

        // Check if it's the correct team's turn
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("It's not " + piece.getTeamColor() + "'s turn.");
        }

        // Check if the end position is within the board limits
        if (!isValidPosition(move.getEndPosition())) {
            throw new InvalidMoveException("Invalid end position.");
        }

        // Assume the move is valid and proceed with further validations as needed
        // This includes checking for checks, valid paths, etc., which will implement later

        // For now, simply move the piece
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null); // Remove the piece from the start position

        // Switch turns
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
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

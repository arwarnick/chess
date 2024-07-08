package chess;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
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

        // Generate potential moves for the piece
        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);

        // Castling logic for kings
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            // Attempt to add castling moves if the king has not moved
            if (!piece.hasMoved()) {
                // Check for king-side castling
                ChessPosition kingSideEndPosition = new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 2);
                if (piece.isCastlingMove(board, kingSideEndPosition, this)) {
                    potentialMoves.add(new ChessMove(startPosition, kingSideEndPosition, null));
                }

                // Check for queen-side castling
                ChessPosition queenSideEndPosition = new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 2);
                if (piece.isCastlingMove(board, queenSideEndPosition, this)) {
                    potentialMoves.add(new ChessMove(startPosition, queenSideEndPosition, null));
                }
            }
        }

        if (piece.getTeamColor() == TeamColor.WHITE) {
            return potentialMoves.stream()
                    .filter(this::moveDoesNotResultInCheckWhite)
                    .collect(Collectors.toSet());
        }

        // Filter out moves that would result in a check
        return potentialMoves.stream()
                .filter(this::moveDoesNotResultInCheck)
                .collect(Collectors.toSet());
    }

    private boolean moveDoesNotResultInCheck(ChessMove move) {
        // Create a deep copy of the board for simulation
        ChessBoard simulatedBoard = this.board.deepCopy();

        // Simulate the move on the copied board
        simulatedBoard.movePiece(move.getStartPosition(), move.getEndPosition());

        // Create a temporary game instance using the copied board to check for check condition
        ChessGame tempGame = new ChessGame();
        tempGame.setBoard(simulatedBoard);


        // It's crucial to set the turn to the opponent's turn since isInCheck checks if the current turn's king is in check
        tempGame.setTeamTurn((this.teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);

        // After the simulated move, check if the original team is in check
        // We need to check against the original team's turn because that's whose move we are simulating
        boolean isInCheckAfterMove = tempGame.isInCheck((this.teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);

        // If the team that made the move is in check after the move, then the move results in check
        return !isInCheckAfterMove;
    }

    private boolean moveDoesNotResultInCheckWhite(ChessMove move) {
        // Create a deep copy of the board for simulation
        ChessBoard simulatedBoard = this.board.deepCopy();

        // Perform the move on the simulated board
        simulatedBoard.movePiece(move.getStartPosition(), move.getEndPosition());

        // Check if the move results in the moving player's king being in check
        ChessGame tempGame = new ChessGame();
        tempGame.setBoard(simulatedBoard);


        tempGame.setTeamTurn(this.teamTurn); // Maintain the current turn

        // Now check if the king is in check after the move
        boolean kingInCheckAfterMove = tempGame.isInCheck(this.teamTurn);

        // If the king is in check after the move, the move is not valid
        return !kingInCheckAfterMove;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
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

    public boolean isPositionUnderAttack(ChessPosition position, TeamColor kingColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition attackerPosition = new ChessPosition(row, col);
                ChessPiece attacker = board.getPiece(attackerPosition);
                // Check if there's a piece, and it's of the opposite color to the king
                if (attacker != null && attacker.getTeamColor() != kingColor) {
                    // Generate potential moves for this piece
                    Set<ChessMove> potentialMoves = (Set<ChessMove>) attacker.pieceMoves(board, attackerPosition);
                    // Check if any move can attack the specified position
                    for (ChessMove move : potentialMoves) {
                        if (move.getEndPosition().equals(position)) {
                            return true; // The position is under attack
                        }
                    }
                }
            }
        }
        return false; // No pieces can attack the specified position
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
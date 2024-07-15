package chess;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a chess game, managing the game state, moves, and turn order.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;
    private ChessMove lastMove;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    public void setLastMove(ChessMove move) {
        this.lastMove = move;
    }

    public ChessMove getLastMove() {
        return this.lastMove;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Calculates all valid moves for a piece at the given position.
     * This method considers the current game state, including check situations.
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return Collections.emptyList();
        }

        Collection<ChessMove> potentialMoves = piece.pieceMoves(board, startPosition);
        addCastlingMoves(piece, startPosition, potentialMoves);

        // Filter out moves that would leave the king in check
        return filterValidMoves(piece, potentialMoves);
    }

    private void addCastlingMoves(ChessPiece piece, ChessPosition startPosition, Collection<ChessMove> moves) {
        if (piece.getPieceType() == ChessPiece.PieceType.KING && !piece.hasMoved()) {
            addKingSideCastlingMove(piece, startPosition, moves);
            addQueenSideCastlingMove(piece, startPosition, moves);
        }
    }

    private void addKingSideCastlingMove(ChessPiece piece, ChessPosition startPosition, Collection<ChessMove> moves) {
        ChessPosition kingSideEndPosition = new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 2);
        if (piece.isCastlingMove(board, kingSideEndPosition, this)) {
            moves.add(new ChessMove(startPosition, kingSideEndPosition, null));
        }
    }

    private void addQueenSideCastlingMove(ChessPiece piece, ChessPosition startPosition, Collection<ChessMove> moves) {
        ChessPosition queenSideEndPosition = new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 2);
        if (piece.isCastlingMove(board, queenSideEndPosition, this)) {
            moves.add(new ChessMove(startPosition, queenSideEndPosition, null));
        }
    }

    private Collection<ChessMove> filterValidMoves(ChessPiece piece, Collection<ChessMove> potentialMoves) {
        return potentialMoves.stream()
                .filter(move -> moveDoesNotResultInCheck(move, piece.getTeamColor()))
                .collect(Collectors.toSet());
    }

    /**
     * Checks if a move would result in the moving player's king being in check.
     * This is done by simulating the move on a temporary board.
     */
    private boolean moveDoesNotResultInCheck(ChessMove move, TeamColor teamColor) {
        ChessBoard simulatedBoard = this.board.deepCopy();
        simulatedBoard.movePiece(move.getStartPosition(), move.getEndPosition());

        ChessGame tempGame = new ChessGame();
        tempGame.setBoard(simulatedBoard);
        tempGame.setTeamTurn(teamColor);

        return !tempGame.isInCheck(teamColor);
    }

    /**
     * Executes a move on the chess board.
     * This method handles various types of moves including standard moves,
     * castling, en passant, and pawn promotion.
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        validateMove(piece, move);

        if (isCastlingMove(piece, move)) {
            executeCastlingMove(piece, move);
        } else if (isEnPassantMove(piece, move)) {
            executeEnPassantMove(piece, move);
        } else {
            executeStandardMove(piece, move);
        }

        updateGameState(piece, move);
    }

    /**
     * Validates if a move is legal according to chess rules.
     * Checks for correct turn, valid positions, and whether the move results in check.
     */
    private void validateMove(ChessPiece piece, ChessMove move) throws InvalidMoveException {
        if (piece == null) {
            throw new InvalidMoveException("No piece at start position.");
        }
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("It's not " + piece.getTeamColor() + "'s turn.");
        }
        if (!isValidPosition(move.getEndPosition())) {
            throw new InvalidMoveException("Invalid end position.");
        }
        if (moveResultsInCheck(move)) {
            throw new InvalidMoveException("Move would result in check");
        }

        ChessPiece targetPiece = board.getPiece(move.getEndPosition());
        if (targetPiece != null && targetPiece.getTeamColor() == piece.getTeamColor()) {
            throw new InvalidMoveException("Cannot capture your own piece");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move for this piece");
        }
    }

    private boolean isCastlingMove(ChessPiece piece, ChessMove move) {
        return piece.getPieceType() == ChessPiece.PieceType.KING &&
                Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 2;
    }

    /**
     * Executes a castling move, which involves moving both the king and the rook.
     */
    private void executeCastlingMove(ChessPiece piece, ChessMove move) throws InvalidMoveException {
        int direction = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
        boolean isKingSide = direction > 0;

        ChessPosition rookStartPosition = new ChessPosition(move.getStartPosition().getRow(), isKingSide ? 8 : 1);

        int rookEndColumn = isKingSide
                ? move.getEndPosition().getColumn() - 1
                : move.getEndPosition().getColumn() + 1;
        ChessPosition rookEndPosition = new ChessPosition(
                move.getEndPosition().getRow(),
                rookEndColumn
        );

        board.movePiece(move.getStartPosition(), move.getEndPosition());
        piece.setHasMoved(true);

        ChessPiece rook = board.getPiece(rookStartPosition);
        if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK) {
            board.movePiece(rookStartPosition, rookEndPosition);
            rook.setHasMoved(true);
        } else {
            throw new InvalidMoveException("Rook not in correct position for castling.");
        }
    }

    private boolean isEnPassantMove(ChessPiece piece, ChessMove move) {
        return piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                move.getStartPosition().getColumn() != move.getEndPosition().getColumn() &&
                board.getPiece(move.getEndPosition()) == null;
    }

    /**
     * Executes an en passant move, which involves capturing a pawn that has just made a double-step move.
     */
    private void executeEnPassantMove(ChessPiece piece, ChessMove move) {
        int capturedPawnRow = (piece.getTeamColor() == TeamColor.WHITE) ? move.getEndPosition().getRow() - 1 : move.getEndPosition().getRow() + 1;
        ChessPosition capturedPawnPosition = new ChessPosition(capturedPawnRow, move.getEndPosition().getColumn());
        board.addPiece(capturedPawnPosition, null); // Remove the captured pawn
        board.movePiece(move.getStartPosition(), move.getEndPosition());
    }

    private void executeStandardMove(ChessPiece piece, ChessMove move) throws InvalidMoveException {
        board.movePiece(move.getStartPosition(), move.getEndPosition());
        piece.setHasMoved(true);
        piece.setPosition(move.getEndPosition());

        if (isPawnPromotion(piece, move)) {
            promotePawn(move);
        }
    }

    private boolean isPawnPromotion(ChessPiece piece, ChessMove move) {
        return piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                (move.getEndPosition().getRow() == 8 || move.getEndPosition().getRow() == 1);
    }

    private void promotePawn(ChessMove move) throws InvalidMoveException {
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(teamTurn, move.getPromotionPiece()));
        } else {
            throw new InvalidMoveException("Promotion piece type must be specified.");
        }
    }

    private void updateGameState(ChessPiece piece, ChessMove move) {
        setLastMove(move);
        board.setLastMove(move);
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    private boolean moveResultsInCheck(ChessMove move) {
        return simulateMove(move);
    }

    /**
     * Determines if the specified team's king is in check.
     * This is done by finding the king's position and checking if it's under attack.
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        if (kingPosition == null) {
            return false;
        }

        return isPositionUnderAttack(kingPosition, teamColor);
    }

    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPosition position = new ChessPosition(row, column);
                ChessPiece piece = board.getPiece(position);
                if (isKingOfTeam(piece, teamColor)) {
                    return position;
                }
            }
        }
        return null;
    }

    private boolean isKingOfTeam(ChessPiece piece, TeamColor teamColor) {
        return piece != null &&
                piece.getPieceType() == ChessPiece.PieceType.KING &&
                piece.getTeamColor() == teamColor;
    }

    /**
     * Checks if a specific position is under attack by any piece of the opposing team.
     */
    public boolean isPositionUnderAttack(ChessPosition position, TeamColor kingColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition attackerPosition = new ChessPosition(row, col);
                ChessPiece attacker = board.getPiece(attackerPosition);
                if (isEnemyPiece(attacker, kingColor) && canAttack(attacker, attackerPosition, position)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEnemyPiece(ChessPiece piece, TeamColor kingColor) {
        return piece != null && piece.getTeamColor() != kingColor;
    }

    private boolean canAttack(ChessPiece attacker, ChessPosition attackerPosition, ChessPosition targetPosition) {
        Set<ChessMove> potentialMoves = (Set<ChessMove>) attacker.pieceMoves(board, attackerPosition);
        return potentialMoves.stream().anyMatch(move -> move.getEndPosition().equals(targetPosition));
    }

    /**
     * Determines if the specified team is in checkmate.
     * A team is in checkmate if they are in check and have no legal moves to get out of check.
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        return getAllTeamMoves(teamColor).stream().noneMatch(this::moveRemovesCheck);
    }

    private Collection<ChessMove> getAllTeamMoves(TeamColor teamColor) {
        return board.getAllPieces().entrySet().stream()
                .filter(entry -> entry.getValue().getTeamColor() == teamColor)
                .flatMap(entry -> validMoves(entry.getKey()).stream())
                .collect(Collectors.toList());
    }

    private boolean moveRemovesCheck(ChessMove move) {
        return !simulateMove(move);
    }

    private boolean simulateMove(ChessMove move) {
        ChessBoard simulatedBoard = this.board.deepCopy();
        simulatedBoard.movePiece(move.getStartPosition(), move.getEndPosition());

        ChessGame tempGame = new ChessGame();
        tempGame.setBoard(simulatedBoard);
        tempGame.setTeamTurn(this.teamTurn);

        return tempGame.isInCheck(this.teamTurn);
    }

    /**
     * Determines if the specified team is in stalemate.
     * A team is in stalemate if they are not in check but have no legal moves.
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return getAllTeamMoves(teamColor).isEmpty();
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return this.board;
    }

    private boolean isValidPosition(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 &&
                position.getColumn() >= 1 && position.getColumn() <= 8;
    }
}
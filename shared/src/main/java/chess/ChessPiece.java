package chess;

import java.util.*;

/**
 * Represents a single chess piece
 */
public class ChessPiece {

    private final ChessGame.TeamColor teamColor;
    private final PieceType type;
    private boolean hasMoved;
    private ChessPosition position;

    public ChessPiece(ChessGame.TeamColor teamColor, ChessPiece.PieceType type) {
        this.teamColor = teamColor;
        this.type = type;
        this.hasMoved = false;
        this.position = new ChessPosition(0, 0);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    public PieceType getPieceType() {
        return type;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public void setPosition(ChessPosition position) {
        this.position = position;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in danger
     *
     * @param board The current state of the chess board
     * @param myPosition The current position of this piece
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Set<ChessMove> moves = new HashSet<>();

        switch (this.type) {
            case BISHOP -> calculateBishopMoves(board, myPosition, moves);
            case KING -> calculateKingMoves(board, myPosition, moves);
            case KNIGHT -> calculateKnightMoves(board, myPosition, moves);
            case PAWN -> calculatePawnMoves(board, myPosition, moves);
            case QUEEN -> calculateQueenMoves(board, myPosition, moves);
            case ROOK -> calculateRookMoves(board, myPosition, moves);
        }

        return moves;
    }

    private void calculateBishopMoves(ChessBoard board, ChessPosition myPosition, Set<ChessMove> moves) {
        int[] directions = {-1, 1};
        for (int rowDirection : directions) {
            for (int colDirection : directions) {
                addLinearMoves(moves, board, myPosition, rowDirection, colDirection);
            }
        }
    }

    private void calculateKingMoves(ChessBoard board, ChessPosition myPosition, Set<ChessMove> moves) {
        int[][] kingDirections = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int[] direction : kingDirections) {
            addSingleMove(moves, board, myPosition, direction[0], direction[1]);
        }
    }

    private void calculateKnightMoves(ChessBoard board, ChessPosition myPosition, Set<ChessMove> moves) {
        int[][] knightMoves = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
        for (int[] move : knightMoves) {
            addSingleMove(moves, board, myPosition, move[0], move[1]);
        }
    }

    private void calculatePawnMoves(ChessBoard board, ChessPosition myPosition, Set<ChessMove> moves) {
        if (isPawnPromotionRow(myPosition)) {
            handlePawnPromotion(moves, board, myPosition);
        } else {
            handleRegularPawnMoves(moves, board, myPosition);
        }
    }

    private void calculateQueenMoves(ChessBoard board, ChessPosition myPosition, Set<ChessMove> moves) {
        int[] directions = {-1, 0, 1};
        for (int rowDirection : directions) {
            for (int colDirection : directions) {
                if (rowDirection != 0 || colDirection != 0) {
                    addLinearMoves(moves, board, myPosition, rowDirection, colDirection);
                }
            }
        }
    }

    private void calculateRookMoves(ChessBoard board, ChessPosition myPosition, Set<ChessMove> moves) {
        addLinearMoves(moves, board, myPosition, -1, 0);
        addLinearMoves(moves, board, myPosition, 1, 0);
        addLinearMoves(moves, board, myPosition, 0, -1);
        addLinearMoves(moves, board, myPosition, 0, 1);
    }

    private void addLinearMoves(Set<ChessMove> moves, ChessBoard board, ChessPosition startPosition, int rowDirection, int colDirection) {
        int currentRow = startPosition.getRow();
        int currentCol = startPosition.getColumn();

        while (true) {
            currentRow += rowDirection;
            currentCol += colDirection;

            if (!isPositionValid(currentRow, currentCol)) {
                break;
            }

            ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
            ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

            if (pieceAtNewPosition != null) {
                if (pieceAtNewPosition.getTeamColor() != this.teamColor) {
                    moves.add(new ChessMove(startPosition, newPosition, null));
                }
                break;
            } else {
                moves.add(new ChessMove(startPosition, newPosition, null));
            }
        }
    }

    private void addSingleMove(Set<ChessMove> moves, ChessBoard board, ChessPosition startPosition, int rowOffset, int colOffset) {
        int newRow = startPosition.getRow() + rowOffset;
        int newCol = startPosition.getColumn() + colOffset;

        if (isPositionValid(newRow, newCol)) {
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

            if (pieceAtNewPosition == null || pieceAtNewPosition.getTeamColor() != this.teamColor) {
                moves.add(new ChessMove(startPosition, newPosition, null));
            }
        }
    }

    private boolean isPositionValid(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private void handlePawnPromotion(Set<ChessMove> moves, ChessBoard board, ChessPosition myPosition) {
        int direction = getPawnDirection();
        ChessPosition promotionPosition = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (isPositionValid(promotionPosition.getRow(), promotionPosition.getColumn()) && board.getPiece(promotionPosition) == null) {
            addPromotionMoves(moves, myPosition, promotionPosition);
        }

        addPawnCaptureMoves(moves, board, myPosition, true);
    }

    private void handleRegularPawnMoves(Set<ChessMove> moves, ChessBoard board, ChessPosition myPosition) {
        int direction = getPawnDirection();
        int startRow = (this.teamColor == ChessGame.TeamColor.WHITE) ? 2 : 7;

        ChessPosition oneStep = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if (isPositionValid(oneStep.getRow(), oneStep.getColumn()) && board.getPiece(oneStep) == null) {
            moves.add(new ChessMove(myPosition, oneStep, null));

            if (myPosition.getRow() == startRow) {
                ChessPosition twoSteps = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());
                if (board.getPiece(twoSteps) == null) {
                    moves.add(new ChessMove(myPosition, twoSteps, null));
                }
            }
        }

        addPawnCaptureMoves(moves, board, myPosition, false);
        addEnPassantMove(moves, board, myPosition);
    }

    private void addPromotionMoves(Set<ChessMove> moves, ChessPosition startPosition, ChessPosition endPosition) {
        moves.add(new ChessMove(startPosition, endPosition, PieceType.QUEEN));
        moves.add(new ChessMove(startPosition, endPosition, PieceType.ROOK));
        moves.add(new ChessMove(startPosition, endPosition, PieceType.BISHOP));
        moves.add(new ChessMove(startPosition, endPosition, PieceType.KNIGHT));
    }

    private boolean isPawnPromotionRow(ChessPosition position) {
        return position.getRow() == (this.teamColor == ChessGame.TeamColor.WHITE ? 7 : 2);
    }

    private int getPawnDirection() {
        return (this.teamColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
    }

    private void addPawnCaptureMoves(Set<ChessMove> moves, ChessBoard board, ChessPosition myPosition, boolean isPromotion) {
        int[] captureCols = {myPosition.getColumn() - 1, myPosition.getColumn() + 1};
        for (int captureCol : captureCols) {
            ChessPosition capturePos = new ChessPosition(myPosition.getRow() + getPawnDirection(), captureCol);
            if (isPositionValid(capturePos.getRow(), capturePos.getColumn())) {
                ChessPiece capturedPiece = board.getPiece(capturePos);
                if (capturedPiece != null && capturedPiece.getTeamColor() != this.teamColor) {
                    if (isPromotion) {
                        addPromotionMoves(moves, myPosition, capturePos);
                    } else {
                        moves.add(new ChessMove(myPosition, capturePos, null));
                    }
                }
            }
        }
    }

    private void addEnPassantMove(Set<ChessMove> moves, ChessBoard board, ChessPosition myPosition) {
        ChessMove lastMove = board.getLastMove();
        if (lastMove != null && lastMove.getEndPosition().getRow() == myPosition.getRow() &&
                Math.abs(lastMove.getEndPosition().getColumn() - myPosition.getColumn()) == 1) {
            ChessPiece lastMovedPiece = board.getPiece(lastMove.getEndPosition());
            if (lastMovedPiece != null && lastMovedPiece.getPieceType() == PieceType.PAWN &&
                    lastMovedPiece.getTeamColor() != this.teamColor &&
                    Math.abs(lastMove.getStartPosition().getRow() - lastMove.getEndPosition().getRow()) == 2) {
                ChessPosition enPassantPosition = new ChessPosition(
                        myPosition.getRow() + getPawnDirection(),
                        lastMove.getEndPosition().getColumn()
                );
                moves.add(new ChessMove(myPosition, enPassantPosition, null));
            }
        }
    }

    public boolean isCastlingMove(ChessBoard board, ChessPosition endPosition, ChessGame game) {
        if (this.type != PieceType.KING || this.hasMoved) {
            return false;
        }

        int direction = endPosition.getColumn() - this.position.getColumn();
        if (Math.abs(direction) != 2) return false;

        int row = teamColor == ChessGame.TeamColor.WHITE ? 1 : 8;
        ChessPosition passingSquare = new ChessPosition(row, this.position.getColumn() + direction / 2);

        if (game.isPositionUnderAttack(this.position, teamColor) ||
                game.isPositionUnderAttack(passingSquare, teamColor) ||
                game.isPositionUnderAttack(endPosition, teamColor)) {
            return false;
        }

        ChessPosition rookPosition = new ChessPosition(row, direction > 0 ? 8 : 1);
        ChessPiece rook = board.getPiece(rookPosition);

        if (rook == null || rook.hasMoved() || rook.getPieceType() != PieceType.ROOK) {
            return false;
        }

        return isPathClearForCastling(board, row, direction);
    }

    private boolean isPathClearForCastling(ChessBoard board, int row, int direction) {
        int step = direction > 0 ? 1 : -1;
        for (int col = this.position.getColumn() + step; col != (direction > 0 ? 8 : 1); col += step) {
            if (board.getPiece(new ChessPosition(row, col)) != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChessPiece other = (ChessPiece) obj;
        return teamColor == other.teamColor && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, type);
    }
}
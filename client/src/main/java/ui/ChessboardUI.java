package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;
import java.util.Map;

public class ChessboardUI {

    public void displayBoard(ChessGame game, ChessGame.TeamColor perspective) {
        Map<ChessPosition, ChessPiece> board = game.getBoard().getAllPieces();

        if (perspective == ChessGame.TeamColor.WHITE) {
            System.out.println("White's perspective:");
            printBoardWhiteBottom(board);
        } else {
            System.out.println("Black's perspective:");
            printBoardBlackBottom(board);
        }
    }

    private void printBoardWhiteBottom(Map<ChessPosition, ChessPiece> board) {
        System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + "    a  b  c  d  e  f  g  h    " + EscapeSequences.RESET_BG_COLOR);

        for (int i = 8; i >= 1; i--) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " " + i + " " + EscapeSequences.RESET_BG_COLOR);

            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                printPiece(board.get(position), (i + j) % 2 != 0);
            }

            System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " " + i + " " + EscapeSequences.RESET_BG_COLOR);
        }

        System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + "    a  b  c  d  e  f  g  h    " + EscapeSequences.RESET_BG_COLOR);
    }

    private void printBoardBlackBottom(Map<ChessPosition, ChessPiece> board) {
        System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + "    h  g  f  e  d  c  b  a    " + EscapeSequences.RESET_BG_COLOR);

        for (int i = 1; i <= 8; i++) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " " + i + " " + EscapeSequences.RESET_BG_COLOR);

            for (int j = 8; j >= 1; j--) {
                ChessPosition position = new ChessPosition(i, j);
                printPiece(board.get(position), (i + j) % 2 != 0);
            }

            System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " " + i + " " + EscapeSequences.RESET_BG_COLOR);
        }

        System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + "    h  g  f  e  d  c  b  a    " + EscapeSequences.RESET_BG_COLOR);
    }

    private void printPiece(ChessPiece piece, boolean isLightSquare) {
        String bgColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_DARK_GREY;
        System.out.print(bgColor);

        if (piece == null) {
            System.out.print("   ");
        } else {
            String pieceColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ?
                    EscapeSequences.SET_TEXT_COLOR_BLUE : EscapeSequences.SET_TEXT_COLOR_RED;
            String symbol = getPieceSymbol(piece);
            System.out.printf(pieceColor + "%2s ", symbol);
        }

        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
        };
    }

    public void displayBoardWithHighlights(ChessGame game, ChessGame.TeamColor perspective,
                                           ChessPosition selectedPosition, Collection<ChessMove> legalMoves) {
        Map<ChessPosition, ChessPiece> board = game.getBoard().getAllPieces();

        System.out.println(perspective == ChessGame.TeamColor.WHITE ? "White's perspective:" : "Black's perspective:");
        if (perspective == ChessGame.TeamColor.WHITE) {
            printBoardWhiteBottomWithHighlights(board, selectedPosition, legalMoves);
        } else {
            printBoardBlackBottomWithHighlights(board, selectedPosition, legalMoves);
        }
    }

    private void printBoardWhiteBottomWithHighlights(Map<ChessPosition, ChessPiece> board,
                                                     ChessPosition selectedPosition,
                                                     Collection<ChessMove> legalMoves) {
        System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + "    a  b  c  d  e  f  g  h    " + EscapeSequences.RESET_BG_COLOR);

        for (int i = 8; i >= 1; i--) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " " + i + " " + EscapeSequences.RESET_BG_COLOR);

            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                boolean isHighlighted = position.equals(selectedPosition) ||
                        legalMoves.stream().anyMatch(move -> move.getEndPosition().equals(position));
                printPieceWithHighlight(board.get(position), (i + j) % 2 != 0, isHighlighted);
            }

            System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " " + i + " " + EscapeSequences.RESET_BG_COLOR);
        }

        System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY + "    a  b  c  d  e  f  g  h    " + EscapeSequences.RESET_BG_COLOR);
    }

    private void printBoardBlackBottomWithHighlights(Map<ChessPosition, ChessPiece> board,
                                                     ChessPosition selectedPosition,
                                                     Collection<ChessMove> legalMoves) {
        // Similar to printBoardWhiteBottomWithHighlights, but with reversed column order
    }

    private void printPieceWithHighlight(ChessPiece piece, boolean isLightSquare, boolean isHighlighted) {
        String bgColor = isHighlighted ? EscapeSequences.SET_BG_COLOR_GREEN :
                (isLightSquare ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_DARK_GREY);
        System.out.print(bgColor);

        if (piece == null) {
            System.out.print("   ");
        } else {
            String pieceColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ?
                    EscapeSequences.SET_TEXT_COLOR_BLUE : EscapeSequences.SET_TEXT_COLOR_RED;
            String symbol = getPieceSymbol(piece);
            System.out.printf(pieceColor + "%2s ", symbol);
        }

        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }
}
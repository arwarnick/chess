package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import ui.EscapeSequences;

import java.util.Map;

public class ChessboardUI {

    public void displayBoard(ChessGame game, boolean whiteBottom) {
        Map<ChessPosition, ChessPiece> board = game.getBoard().getAllPieces();

        if (whiteBottom) {
            printBoardWhiteBottom(board);
        } else {
            printBoardBlackBottom(board);
        }
    }

    private void printBoardWhiteBottom(Map<ChessPosition, ChessPiece> board) {
        System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        System.out.print("    a  b  c  d  e  f  g  h    ");
        System.out.println(EscapeSequences.RESET_BG_COLOR);

        for (int i = 8; i >= 1; i--) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            System.out.print(" " + i + " ");
            System.out.print(EscapeSequences.RESET_BG_COLOR);

            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                printSquare(board.get(position), (i + j) % 2 == 0);
            }

            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            System.out.print(" " + i + " ");
            System.out.println(EscapeSequences.RESET_BG_COLOR);
        }

        System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        System.out.print("    a  b  c  d  e  f  g  h    ");
        System.out.println(EscapeSequences.RESET_BG_COLOR);
    }

    private void printBoardBlackBottom(Map<ChessPosition, ChessPiece> board) {
        System.out.println(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        System.out.print("    h  g  f  e  d  c  b  a    ");
        System.out.println(EscapeSequences.RESET_BG_COLOR);

        for (int i = 1; i <= 8; i++) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            System.out.print(" " + i + " ");
            System.out.print(EscapeSequences.RESET_BG_COLOR);

            for (int j = 8; j >= 1; j--) {
                ChessPosition position = new ChessPosition(i, j);
                printSquare(board.get(position), (i + j) % 2 == 0);
            }

            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            System.out.print(" " + i + " ");
            System.out.println(EscapeSequences.RESET_BG_COLOR);
        }

        System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        System.out.print("    h  g  f  e  d  c  b  a    ");
        System.out.println(EscapeSequences.RESET_BG_COLOR);
    }

    private void printSquare(ChessPiece piece, boolean isLightSquare) {
        String bgColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_BLACK;
        System.out.print(bgColor);

        if (piece == null) {
            System.out.print("   ");
        } else {
            String pieceColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ?
                    EscapeSequences.SET_TEXT_COLOR_BLUE : EscapeSequences.SET_TEXT_COLOR_RED;
            System.out.print(pieceColor + " " + getPieceSymbol(piece) + " ");
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
}
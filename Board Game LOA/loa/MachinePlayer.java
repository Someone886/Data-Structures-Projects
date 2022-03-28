/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;

import static loa.Piece.*;

/** An automated Player.
 *  @author Qianfei(Ben) Hu
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        _depth = DEPTH;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board board = new Board(getBoard());

        int value;
        assert side() == board.turn();
        _foundMove = null;

        if (side() == WP) {
            value = findMove(board, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(board, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1 WP,
     *  and minimal value or value < ALPHA if SENSE==-1 BP. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {

        Move move = null;
        saveMove = board.gameOver();

        if (!saveMove) {
            if (sense == 1) {
                move = findBestMove(board.turn(), board, depth, beta);
            } else {
                move = findBestMove(board.turn(), board, depth, alpha);
            }
        }

        if (!saveMove) {
            _foundMove = move;
        }
        return move != null ? move.getMoveScore() : 0;
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return _depth;
    }

    /** Find the best move for piece p.
     * @param p current piece
     * @param board current board in the game
     * @param depth depth of calculating the best move
     * @param good the score that is good enough
     * @return the best move.
     * */
    Move findBestMove(Piece p, Board board, int depth, int good) {
        if (depth == 0) {
            return guessBestMove(p, board);
        }
        Move m = board.legalMoves().get(0);
        m.setMoveScore(-INFTY);
        Move bestSoFar = m;

        for (Move move : board.legalMoves()) {
            board.makeMove(move);
            Move response = findBestMove(board.turn(), board,
                    depth - 1, -bestSoFar.getMoveScore());

            if (-response.calculateValue(board) > bestSoFar.getMoveScore()) {
                bestSoFar = move;
                bestSoFar.setMoveScore(-response.getMoveScore());
                board.retract();
                if (bestSoFar.getMoveScore() >= good) {
                    break;
                }
            } else {
                board.retract();
            }
        }
        return bestSoFar;
    }

    /** Guess one best move when depth = 0..
     * @param p the current player
     * @param board the current board
     * @return the best move with depth = 0
     */
    Move guessBestMove(Piece p, Board board) {
        Move best = null;
        Integer bestValue = null;
        assert (board.turn() == p);
        if (p == WP) {
            for (Move move : board.legalMoves()) {
                board.makeMove(move);
                if (board.winner() == p) {
                    board.retract();
                    return move;
                }
                int current = eval(board, WP);
                bestValue = bestValue == null ? current : bestValue;
                if (current >= bestValue) {
                    best = move;
                    bestValue = current;
                }
                board.retract();
            }
        } else if (p == BP) {
            for (Move move : board.legalMoves()) {
                board.makeMove(move);
                if (board.winner() == p) {
                    board.retract();
                    return move;
                }
                int current = eval(board, BP);
                bestValue = bestValue == null ? current : bestValue;
                if (current <= bestValue) {
                    best = move;
                    bestValue = current;
                }
                board.retract();
            }
        }
        if (best == null) {
            best = board.legalMoves().get(0);
        }
        return best;
    }

    /** Returns an evaluation of a board.
     * WP pursues positive points while BP pursues negative points.
     * Using the difference between BP's and WP's distance as an indicator
     * of the state on the board.
     * @param board current state of the board.
     * @param side side of the board to be evaluated
     * @return a score for the current board.
     */
    public static int eval(Board board, Piece side) {
        double result = 0;
        Piece opponent = side.opposite();
        ArrayList<Square> sidePiece = board.sidePieces(side);
        ArrayList<Square> opponentPiece = board.sidePieces(opponent);
        for (Square cord1 : sidePiece) {
            for (Square cord2 : sidePiece) {
                if (cord1 != cord2) {
                    double distance = distance(cord1.col(), cord1.row(),
                            cord2.col(), cord2.row());
                    result -= 10 * distance;
                }
            }
        }
        for (Square cord1 : opponentPiece) {
            for (Square cord2 : opponentPiece) {
                if (cord1 != cord2) {
                    double distance = distance(cord1.col(), cord1.row(),
                            cord2.col(), cord2.row());
                    result += 10 * distance;
                }
            }
        }
        return (int) result;
    }

    /** Distance squared between 2 squares.
     * @param c0 col of the first square
     * @param r0 row of the first square
     * @param c1 col of the second square
     * @param r1 row of the second square
     * @return distance^2 between two squares
     */
    public static double distance(int c0, int r0, int c1, int r1) {
        return (c1 - c0) * (c1 - c0) + (r1 - r0) * (r1 - r0);
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;
    /** The current search depth. */
    private int _depth;
    /** Largest depth of searching.
     * Stop searching at 0. */
    private static final int DEPTH = 3;
}

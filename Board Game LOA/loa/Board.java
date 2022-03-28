/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Qianfei(Ben) Hu
 */
class Board {
    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        for (int r = 0; r < BOARD_SIZE; r += 1) {
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                Square thisSquare = Square.sq(c, r);
                set(thisSquare, contents[r][c]);
            }
        }
        _turn = side;
        _winner = null;
        _winnerKnown = false;
        _moves.clear();
        _subsetsInitialized = false;
        _moveLimit = DEFAULT_MOVE_LIMIT;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        _moves.clear();
        _moves.addAll(board._moves);
        _turn = board._turn;
        _subsetsInitialized = false;
        for (int r = 0; r < BOARD_SIZE; r += 1) {
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                Square thisSquare = Square.sq(c, r);
                set(thisSquare, board.get(thisSquare), null);
            }
        }
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {
        if (v != BP && v != WP && v != EMP) {
            throw new IllegalArgumentException("Wrong piece color.");
        }
        if (next != BP && next != WP && next != EMP && next != null) {
            throw new IllegalArgumentException("Wrong next turn side.");
        }
        _board[sq.index()] = v;
        if (next != null) {
            _turn = next;
        }
        _subsetsInitialized = false;
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** return the current _moveLimit. */
    int getCurrLimit() {
        return _moveLimit;
    }


    /** Assuming isLegal(MOVE), make MOVE. This function assumes that
     *  MOVE.isCapture() will return false.  If it saves the move for
     *  later retraction, makeMove itself uses MOVE.captureMove() to produce
     *  the capturing move. */
    void makeMove(Move move) {
        assert isLegal(move);
        assert !move.isCapture();

        _moveLimit -= 1;
        if (_moveLimit == 0) {
            _winner = EMP;
            _winnerKnown = true;
            return;
        }

        Square from = move.getFrom();
        Square to = move.getTo();
        Piece moved = _board[from.index()];
        Piece captured = _board[to.index()];

        if (captured != EMP) {
            set(to, EMP);
            move = move.captureMove();
        }
        set(from, EMP);
        set(to, moved);
        _moves.add(move);
        _turn = _turn.opposite();
        _subsetsInitialized = false;
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;

        Move move = _moves.remove(_moves.size() - 1);

        Square to = move.getTo();
        Square from = move.getFrom();

        Piece moved = _board[to.index()];
        boolean isCapture = move.isCapture();
        Piece captured;
        if (isCapture) {
            captured = _turn;
        } else {
            captured = EMP;
        }

        set(to, captured);
        set(from, moved);
        _turn = _turn.opposite();
        _moveLimit += 1;
        _subsetsInitialized = false;
        _winnerKnown = false;
        _winner = null;
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move. */
    boolean isLegal(Square from, Square to) {
        Move thisMove = Move.mv(from, to);
        return !blocked(from, to)
                && _board[from.index()] != null
                && _board[from.index()] == _turn
                && thisMove.length() == pieceAlong(thisMove);
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Return a sequence of all legal moves from this position. */
    List<Move> legalMoves() {
        List<Move> result = new ArrayList<>();
        String curr = _turn.abbrev();
        Square thisSquare, thatSqaure;
        for (int r0 = 0; r0 < BOARD_SIZE; r0 += 1) {
            for (int c0 = 0; c0 < BOARD_SIZE; c0 += 1) {
                thisSquare = Square.sq(c0, r0);

                if (_board[thisSquare.index()].abbrev().equals(curr)) {
                    for (int r1 = 0; r1 < BOARD_SIZE; r1 += 1) {
                        for (int c1 = 0; c1 < BOARD_SIZE; c1 += 1) {
                            if (c1 != c0 || r1 != r0) {
                                thatSqaure = Square.sq(c1, r1);
                                if (thisSquare.isValidMove(thatSqaure)
                                        && isLegal(thisSquare, thatSqaure)) {
                                    Move move = Move.mv(thisSquare, thatSqaure);
                                    result.add(move);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. */
    Piece winner() {
        computeRegions();
        if (!_winnerKnown) {
            if (_whiteRegionSizes.size() == 1) {
                if (_blackRegionSizes.size() == 1) {
                    _winner = _turn.opposite();
                } else {
                    _winner = WP;
                }
                _winnerKnown = true;
            } else if (_blackRegionSizes.size() == 1) {
                _winner = BP;
                _winnerKnown = true;
            } else {
                _winner = null;
            }
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        if (from == to) {
            throw new IllegalArgumentException("from square is to square.");
        }

        int dir = from.direction(to);
        Piece thisPiece = _board[from.index()];
        Piece target = _board[to.index()];
        if (thisPiece.abbrev().equals(target.abbrev())) {
            return true;
        }

        int fromC = from.col();
        int fromR = from.row();
        int toC = to.col();
        int toR = to.row();
        int dc = from.getDC(dir);
        int dr = from.getDR(dir);

        fromC += dc;
        fromR += dr;

        while (fromC != toC || fromR != toR) {
            Square currSquare = Square.sq(fromC, fromR);
            if (_board[currSquare.index()].abbrev()
                    .equals(_turn.opposite().abbrev())) {
                return true;
            }
            fromC += dc;
            fromR += dr;
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        int r = sq.row();
        int c = sq.col();

        if (visited[r][c]) {
            return 0;
        } else if (_board[sq.index()].abbrev().equals(p.abbrev())) {
            visited[r][c] = true;
            int result = 1;

            if (c - 1 >= 0) {
                result += numContig(Square.sq(c - 1, r), visited, p);
                if (r - 1 >= 0) {
                    result += numContig(Square.sq(c - 1, r - 1), visited, p);
                }
                if (r + 1 < BOARD_SIZE) {
                    result += numContig(Square.sq(c - 1, r + 1), visited, p);
                }
            }
            if (c + 1 < BOARD_SIZE) {
                result += numContig(Square.sq(c + 1, r), visited, p);
                if (r - 1 >= 0) {
                    result += numContig(Square.sq(c + 1, r - 1), visited, p);
                }
                if (r + 1 < BOARD_SIZE) {
                    result += numContig(Square.sq(c + 1, r + 1), visited, p);
                }
            }
            if (r - 1 >= 0) {
                result += numContig(Square.sq(c, r - 1), visited, p);
            }
            if (r + 1 < BOARD_SIZE) {
                result += numContig(Square.sq(c, r + 1), visited, p);
            }

            return  result;
        }
        return 0;
    }

    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    public void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        Square thisSquare;
        int thisClusterNum;
        Piece thisPiece;
        for (int r = 0; r < BOARD_SIZE; r += 1) {
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                thisSquare = Square.sq(c, r);
                if (_board[thisSquare.index()].abbrev().equals(BP.abbrev())) {
                    thisPiece = BP;
                    thisClusterNum = numContig(thisSquare, visited, thisPiece);
                    if (thisClusterNum != 0) {
                        _blackRegionSizes.add(thisClusterNum);
                    }
                } else if (_board[thisSquare.index()].abbrev()
                        .equals(WP.abbrev())) {
                    thisPiece = WP;
                    thisClusterNum = numContig(thisSquare, visited, thisPiece);
                    if (thisClusterNum != 0) {
                        _whiteRegionSizes.add(thisClusterNum);
                    }
                }
            }
        }

        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }

    /** Count and return the number of pieces on the direction of a move.
     * @param move the move of a piece.
     * @return the number of pieces on the direction of a move.
     */
    public int pieceAlong(Move move) {
        Square from = move.getFrom();
        Square to = move.getTo();
        int dir = from.direction(to);
        int r = from.row();
        int c = from.col();

        int count = 0;
        int i;
        Square thisSquare;
        if (dir == 0 || dir == 4) {
            for (i = 0; i < BOARD_SIZE; i += 1) {
                thisSquare = Square.sq(c, i);
                if (!_board[thisSquare.index()].abbrev().equals("-")) {
                    count += 1;
                }
            }
        } else if (dir == 2 || dir == 6) {
            for (i = 0; i < BOARD_SIZE; i += 1) {
                thisSquare = Square.sq(i, r);
                if (!_board[thisSquare.index()].abbrev().equals("-")) {
                    count += 1;
                }
            }
        } else if (dir == 1 || dir == 5) {
            for (i = -Math.min(c, r); i <= Math.min(7 - c, 7 - r); i += 1) {
                thisSquare = Square.sq(c + i, r + i);
                if (!_board[thisSquare.index()].abbrev().equals("-")) {
                    count += 1;
                }
            }
        } else if (dir == 3 || dir == 7) {
            for (i = -Math.min(c, 7 - r); i <= Math.min(7 - c, r); i += 1) {
                thisSquare = Square.sq(c + i, r - i);
                if (!_board[thisSquare.index()].abbrev().equals("-")) {
                    count += 1;
                }
            }
        } else {
            throw new IllegalArgumentException("Bad move -> wrong direction");
        }
        return count;
    }

    /** Return all squares of one side.
     * @param side the side of the pieces to be grouped.
     */
    public ArrayList<Square> sidePieces(Piece side) {
        ArrayList<Square> sidePieces = new ArrayList<>();
        for (int c = 0; c < BOARD_SIZE; c += 1) {
            for (int r = 0; r < BOARD_SIZE; r += 1) {
                Square thisSquare = sq(c, r);
                if (_board[thisSquare.index()] == side) {
                    sidePieces.add(thisSquare);
                }
            }
        }
        return sidePieces;
    }

    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();
}

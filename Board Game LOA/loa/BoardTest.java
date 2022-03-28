/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import org.junit.Test;
import static org.junit.Assert.*;
import static loa.Piece.*;
import static loa.Square.sq;
import static loa.Move.mv;

/** Tests of the Board class API.
 *  @author Qianfei Hu
 */
public class BoardTest {

    /** A "general" position. */
    static final Piece[][] BOARD1 = {
        { EMP, BP,  EMP,  BP,  BP, EMP, EMP, EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP,  BP,  BP, EMP, WP  },
        { WP,  EMP,  BP, EMP, EMP,  WP, EMP, EMP  },
        { WP,  EMP,  WP,  WP, EMP,  WP, EMP, EMP  },
        { WP,  EMP, EMP, EMP,  BP, EMP, EMP, WP  },
        { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP  },
        { EMP, BP,  BP,  BP,  EMP,  BP,  BP, EMP }
    };

    /** A position in which black, but not white, pieces are contiguous. */
    static final Piece[][] BOARD2 = {
        { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
        { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
        { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
        { EMP,  BP,  WP,  BP,  BP,  BP, EMP, EMP },
        { EMP,  WP,  BP,  WP,  WP, EMP, EMP, EMP },
        { EMP, EMP,  BP,  BP,  WP,  WP, EMP,  WP },
        { EMP,  WP,  WP,  BP, EMP, EMP, EMP, EMP },
        { EMP, EMP, EMP,  BP, EMP, EMP, EMP, EMP },
    };

    /** A position in which black, but not white, pieces are contiguous. */
    static final Piece[][] BOARD3 = {
        { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
        { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
        { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
        { EMP,  BP,  WP,  BP,  WP, EMP, EMP, EMP },
        { EMP,  WP,  BP,  WP,  WP, EMP, EMP, EMP },
        { EMP, EMP,  BP,  BP,  WP,  WP,  WP, EMP },
        { EMP,  WP,  WP,  WP, EMP, EMP, EMP, EMP },
        { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
    };

    static final String BOARD1_STRING =
        "===\n"
        + "    - b b b - b b - \n"
        + "    - - - - - - - - \n"
        + "    w - - - b - - w \n"
        + "    w - w w - w - - \n"
        + "    w - b - - w - - \n"
        + "    w - - - b b - w \n"
        + "    w - - - - - - w \n"
        + "    - b - b b - - - \n"
        + "Next move: black\n"
        + "===";

    /** Test legal moves. */
    @Test
    public void testLegality() {
        Board b = new Board(BOARD1, BP);
        assertTrue("f3-d5", b.isLegal(mv("f3-d5")));
        assertTrue("f3-h5", b.isLegal(mv("f3-h5")));
        assertTrue("f3-h1", b.isLegal(mv("f3-h1")));
        assertTrue("f3-b3", b.isLegal(mv("f3-b3")));
        assertFalse("f3-d1", b.isLegal(mv("f3-d1")));
        assertFalse("f3-h3", b.isLegal(mv("f3-h3")));
        assertFalse("f3-e4", b.isLegal(mv("f3-e4")));
        assertFalse("c4-c7", b.isLegal(mv("c4-c7")));
        assertFalse("b1-b4", b.isLegal(mv("b1-b4")));
        System.out.println(b);
        assertFalse("f3-c6", b.isLegal(mv("f3-c6")));
    }

    /** Test contiguity. */
    @Test
    public void testContiguous1() {
        Board b1 = new Board(BOARD1, BP);
        assertFalse("Board 1 black contiguous?", b1.piecesContiguous(BP));
        assertFalse("Board 1 white contiguous?", b1.piecesContiguous(WP));
        assertFalse("Board 1 game over?", b1.gameOver());

        Board b2 = new Board(BOARD2, BP);
        assertTrue("Board 2 black contiguous?", b2.piecesContiguous(BP));
        assertFalse("Board 2 white contiguous?", b2.piecesContiguous(WP));
        assertTrue("Board 2 game over", b2.gameOver());
        Board b3 = new Board(BOARD3, BP);
        System.out.println(b3);
        assertTrue("Board 3 white contiguous?", b3.piecesContiguous(WP));
        assertTrue("Board 3 black contiguous?", b3.piecesContiguous(BP));
        assertTrue("Board 3 game over", b3.gameOver());
        System.out.println(b3.winner());
        assertTrue("Board 3 game over", b3.gameOver());
    }

    /* Board 4 for testing. */
    static final Piece[][] BOARD4 = {
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP,  BP,  WP,  BP,  BP,  BP,  WP, EMP },
            { EMP,  WP,  BP,  WP,  WP, EMP, EMP, EMP },
            { EMP, EMP,  BP,  WP,  WP,  WP,  BP,  WP },
            { EMP,  WP,  WP,  BP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP,  BP, EMP, EMP, EMP, EMP },
    };
    @Test
    public void testWinner() {
        Board b4 = new Board(BOARD4, WP);
        assertFalse("g4-g1", b4.isLegal(mv("g4-g1")));
        System.out.println(b4);
        assertTrue("g4-g2", b4.isLegal(mv("g4-g2")));
        b4.makeMove(mv("g4-g6"));
        b4.piecesContiguous(BP);
        b4.piecesContiguous(WP);
        b4.gameOver();
        System.out.println(b4);
        assertEquals(WP, b4.winner());
    }

    /* Board 5 for testing. */
    static final Piece[][] BOARD5 = {
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, EMP, EMP, EMP, EMP, EMP },
            { EMP,  BP,  WP,  BP,  BP,  BP,  WP, EMP },
            { EMP,  WP,  BP,  WP,  WP, EMP, EMP, EMP },
            { EMP, EMP,  BP,  WP,  WP,  WP,  BP,  WP },
            { EMP,  WP,  WP,  BP, EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP,  BP, EMP, EMP, EMP, EMP },
    };

    @Test
    public void testEquals1() {
        Board b1 = new Board(BOARD1, BP);
        Board b2 = new Board(BOARD1, BP);
        assertEquals("Board 1 equals Board 2", b1, b2);

        Board b6 = new Board(BOARD1, BP);
        Board b7 = new Board(BOARD1, WP);
        assertNotEquals("Board 6 not equals Board 7", b6, b7);

        Board b4 = new Board(BOARD4, BP);
        Board b5 = new Board(BOARD5, BP);
        assertEquals("Board 4 equals Board 5", b4, b5);

    }

    @Test
    public void testMove1() {
        Board b0 = new Board(BOARD1, BP);
        Board b1 = new Board(BOARD1, BP);
        b1.makeMove(mv("f3-d5"));
        assertEquals("square d5 after f3-d5", BP, b1.get(sq(3, 4)));
        assertEquals("square f3 after f3-d5", EMP, b1.get(sq(5, 2)));
        assertEquals("Check move count for board 1 after one move",
                     1, b1.movesMade());
        b1.retract();
        assertEquals("Check for board 1 restored after retraction", b0, b1);
        assertEquals("Check move count for board 1 after move + retraction",
                     0, b1.movesMade());
    }

    /* Board 6 for testing. */
    static final Piece[][] BOARD6 = {
            { WP,  WP,  EMP, EMP, EMP, EMP, BP,  EMP },
            { EMP, EMP, EMP, EMP, EMP, WP,  EMP, EMP },
            { EMP, BP,  BP,  EMP, EMP, EMP, EMP, EMP },
            { WP,  BP,  EMP, EMP, EMP, EMP, WP,  WP  },
            { EMP, WP,  BP,  BP,  EMP, EMP, EMP, EMP },
            { EMP, EMP, EMP, BP,  EMP, EMP, EMP, EMP },
            { WP,  EMP, EMP, EMP, BP,  EMP, EMP, WP  },
            { EMP, EMP, EMP, EMP, EMP, WP,  EMP, EMP },
    };

    @Test
    public void testComputeRegions() {
        Board board = new Board(BOARD6, BP);
        System.out.println(board);
        board.computeRegions();
        System.out.println(board.gameOver());
        assertFalse(board.gameOver());
        System.out.println(board.winner());
    }

    static final Piece[][] BOARD7 = {
            { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
            { WP,  WP,  EMP, EMP, EMP, EMP, EMP, WP },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP },
            { WP,  EMP, EMP, BP,  EMP, EMP, EMP, WP },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP },
            { WP,  BP,  EMP, EMP, EMP, BP,  EMP, WP },
            { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP },
            { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };
    @Test
    public void testLegalMoves() {
        Board b2 = new Board(BOARD1, BP);
        System.out.println(b2);
        System.out.println(b2.legalMoves());
        System.out.println(b2.legalMoves().size());
    }

}

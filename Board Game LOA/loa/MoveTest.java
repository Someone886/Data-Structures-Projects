package loa;

import org.junit.Test;
import static loa.Piece.*;

public class MoveTest {
    Piece[][] _BOARD1 = {
            {EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP},
            {WP,  EMP, EMP, EMP, EMP, WP,  EMP, WP },
            {WP,  EMP, EMP, EMP, WP,  EMP, EMP, WP },
            {WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP },
            {WP,  EMP, EMP, EMP, WP,  EMP, EMP, WP },
            {WP,  EMP, EMP, EMP, EMP, WP,  EMP, WP },
            {WP,  EMP, EMP, EMP, EMP, EMP, WP,  WP },
            {EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP}
    };

    @Test
    public void testScore() {
        Board b = new Board(_BOARD1, BP);
        System.out.println(b);
        int score = 0;
        for (Move m : b.legalMoves()) {
            m.setMoveScore(score);
            score += 1;
        }
    }

    @Test
    public void testGetScore() {
        Board b = new Board(_BOARD1, BP);
        System.out.println(b);
        int score = 0;
        for (Move m : b.legalMoves()) {
            m.setMoveScore(score);
            score += 2;
        }

        for (Move m : b.legalMoves()) {
            System.out.println(m.getMoveScore());
        }
    }
}

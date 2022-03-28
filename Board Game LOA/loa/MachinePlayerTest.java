package loa;

import org.junit.Test;
import static loa.Piece.*;

public class MachinePlayerTest {
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
    public void testEval() {
        Board b = new Board(_BOARD1, BP);
        System.out.println(b);
        for (Move m : b.legalMoves()) {
            b.makeMove(m);
            System.out.println(MachinePlayer.eval(b, WP));
            System.out.println(MachinePlayer.eval(b, BP));
            b.retract();
        }
    }
}

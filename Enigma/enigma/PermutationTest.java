package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Qianfei(Ben) Hu
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void testSize() {
        Alphabet omega = new Alphabet();
        Permutation p = new Permutation(
                "(AELTPHQXRU)(BKNW)(CMOY)(DFG)(IV)(JZ)(S)", omega);
        assertEquals(p.size(), 26);

        Alphabet alpha2 = new Alphabet("ABCDE");
        Permutation p2 = new Permutation("(ABC)(ED)", alpha2);
        assertEquals(p2.size(), 5);

        Permutation p3 = new Permutation("(ABC)", alpha2);
        assertEquals(p3.size(), 5);
        assertNotEquals(p3.size(), 3);

        Permutation p4 = new Permutation("", omega);
        assertEquals(p4.size(), 26);
        assertNotEquals(p4.size(), 0);

        Alphabet alpha3 = new Alphabet("");
        Permutation p5 = new Permutation("", alpha3);
        assertEquals(p5.size(), 0);
    }

    @Test
    public void testPermute() {
        Alphabet omega = new Alphabet();
        Permutation p = new Permutation(
                "(AELTPHQXRU)(BKNW)(CMOY)(DFG)(IV)(JZ)(S)", omega);
        assertEquals(p.permute('L'), 'T');
        assertEquals(p.permute('Y'), 'C');
        assertEquals(p.permute('S'), 'S');
        assertEquals(p.permute('V'), 'I');
        assertEquals(p.permute('I'), 'V');
        assertEquals(p.permute(0), 4);
        assertEquals(p.permute(21), 8);
        assertEquals(p.permute(18), 18);
        assertEquals(p.permute(21), 8);
        assertEquals(p.permute(8), 21);
        assertEquals(p.permute(26), 4);
        assertEquals(p.permute(27), 10);
        assertEquals(p.permute(-8), 18);
        assertNotEquals(p.permute(24), 14);

        Alphabet alpha2 = new Alphabet("ABCD");
        Permutation p2 = new Permutation("(ABC)", alpha2);
        assertEquals(p2.permute('A'), 'B');
        assertEquals(p2.permute('D'), 'D');
        assertEquals(p2.permute(0), 1);
        assertEquals(p2.permute(3), 3);

        perm = p2;
        checkPerm("Wrong match", "ABCD", "BCAD");
    }

    @Test
    public void testInvert() {
        Alphabet omega = new Alphabet();
        Permutation p = new Permutation(
                "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)", omega);
        assertEquals(p.invert('T'), 'L');
        assertEquals(p.invert('S'), 'S');
        assertEquals(p.invert('C'), 'Y');
        assertEquals(p.invert('V'), 'I');
        assertEquals(p.invert('I'), 'V');
        assertEquals(p.invert(4), 0);
        assertEquals(p.invert(8), 21);
        assertEquals(p.invert(18), 18);
        assertEquals(p.invert(21), 8);
        assertEquals(p.invert(8), 21);
        assertEquals(p.invert(-8), 18);
        assertEquals(p.invert(4), 0);
        assertEquals(p.invert(10), 1);
        assertNotEquals(p.invert(14), 24);

        Alphabet alpha2 = new Alphabet("ABCD");
        Permutation p2 = new Permutation("(ABC)", alpha2);
        assertEquals(p2.invert('B'), 'A');
        assertEquals(p2.invert('D'), 'D');
        assertEquals(p2.invert(1), 0);
        assertEquals(p2.invert(3), 3);
    }

    @Test
    public void testAlphabet() {
        Alphabet alpha1 = new Alphabet();
        Permutation p = new Permutation(
                "(AELTPHQXRU) (BKNW) (CMOY) (DFG)(IV)(JZ)(S)", alpha1);
        Permutation p2 = new Permutation(
                "(AELTPHQXRU)(BKNW)(CMOY)(DFG)(IV)(JZ)(S)", alpha1);
        Alphabet alpha2 = p.alphabet();
        Alphabet alpha3 = new Alphabet();
        Alphabet alpha4 = p2.alphabet();
        assertEquals(alpha1, alpha2);
        assertEquals(alpha2, alpha4);
        assertNotEquals(alpha3, alpha2);
    }

    @Test
    public void testDerangement() {
        Alphabet omega = new Alphabet();
        Permutation p1 = new Permutation("(ABCDEFGHIJKLMNOPQRSTUVWXYZ)", omega);
        Permutation p2 = new Permutation(
                "(BCDEFGHIJKLMNOPQRSTUVWXYZ)(A)", omega);
        Permutation p3 = new Permutation("(ABCDE)(FGH)", omega);
        assertTrue(p1.derangement());
        assertFalse(p2.derangement());
        assertFalse(p3.derangement());

        Alphabet alpha2 = new Alphabet("A");
        Permutation p4 = new Permutation("(A)", alpha2);
        Permutation p5 = new Permutation("", alpha2);
        assertFalse(p4.derangement());
        assertFalse(p5.derangement());
    }
}

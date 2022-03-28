package enigma;

import java.util.ArrayList;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Qianfei(Ben) Hu
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;

        if (checkRepetition(cycles)) {
            throw error("char repetition in cycles.", cycles);
        }

        int nextLBracket = cycles.indexOf('('), nextRBracket;
        while (nextLBracket != -1) {
            nextRBracket = cycles.indexOf(")", nextLBracket);
            String toAdd;
            toAdd = cycles.substring(nextLBracket + 1, nextRBracket);
            for (int i = 0; i < toAdd.length(); i += 1) {
                if (!alphabet.contains(toAdd.charAt(i))) {
                    throw error("char not found in the alphabet", cycles);
                }
            }
            addCycle(toAdd);
            nextLBracket = cycles.indexOf('(', nextRBracket);
        }
    }

    /** Check for char repetition in permutation cycles.
     * @param cycles the permutation cycles.
     * @return return true if a char is used more than once;
     *         return false otherwise. */
    public static boolean checkRepetition(String cycles) {
        for (int i = 0; i < cycles.length(); i += 1) {
            for (int j = i + 1; j < cycles.length(); j += 1) {
                char toCheck = cycles.charAt(i);
                if (toCheck != '(' && toCheck != ')'
                        && toCheck != ' ' && toCheck == cycles.charAt(j)) {
                    return true;
                }
            }
        }
        return false;
    }


    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _cycles.add(cycle);
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        p = wrap(p);
        char target = _alphabet.toChar(p);
        char result = permute(target);
        return _alphabet.toInt(result);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        c = wrap(c);
        char target = _alphabet.toChar(c);
        char result = invert(target);
        return _alphabet.toInt(result);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (_cycles != null) {
            for (String cycle : _cycles) {
                int potentialIndex = cycle.indexOf(p);
                if (potentialIndex > -1) {
                    if (potentialIndex == cycle.length() - 1) {
                        return cycle.charAt(0);
                    } else {
                        return cycle.charAt(potentialIndex + 1);
                    }
                }
            }
        }

        if (_alphabet.contains(p)) {
            return p;
        } else {
            throw error("char not found in the alphabet", p);
        }
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (_cycles != null) {
            for (String cycle : _cycles) {
                int potentialIndex = cycle.indexOf(c);
                if (potentialIndex > -1) {
                    if (potentialIndex == 0) {
                        return cycle.charAt(cycle.length() - 1);
                    } else {
                        return cycle.charAt(potentialIndex - 1);
                    }
                }
            }
        }

        if (_alphabet.contains(c)) {
            return c;
        } else {
            throw error("char not found in the alphabet", c);
        }
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int totalArrangement = 0;

        for (String cycle: _cycles) {
            if (cycle.length() == 1) {
                return false;
            }
            totalArrangement += cycle.length();
        }

        return totalArrangement == _alphabet.size();
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Circle of this permutation.*/
    private ArrayList<String> _cycles = new ArrayList<String>();
}

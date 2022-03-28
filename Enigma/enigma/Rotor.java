package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Qianfei(Ben) Hu
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        positionSetting = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return positionSetting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        if (posn >= alphabet().getChars().length()) {
            throw error("posn longer than the alphabet", posn);
        }
        positionSetting = _permutation.wrap(posn);
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        positionSetting = alphabet().toInt(cposn);
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        p = _permutation.wrap(p + positionSetting);
        int output = _permutation.permute(p);
        int result = _permutation.wrap(output - positionSetting);
        return result;
    }

    /** Return the forward conversion of char c in char form.
     * @param c the char to be converted forward. */
    char convertForward(char c) {
        int intermediate = convertForward(alphabet().toInt(c));
        char out = alphabet().toChar(intermediate);
        return out;
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        e = _permutation.wrap(e + positionSetting);
        int output = _permutation.invert(e);
        int result = _permutation.wrap(output - positionSetting);
        return result;
    }

    /** Return the backward conversion of char c in char form.
     * @param c the char to be converted backward*/
    char convertBackward(char c) {
        int intermediate = convertBackward(alphabet().toInt(c));
        char out = alphabet().toChar(intermediate);
        return out;
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** The current setting of position. */
    private int positionSetting;

}

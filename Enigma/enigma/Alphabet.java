package enigma;

import static enigma.EnigmaException.error;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Qianfei(Ben) Hu
 */
class Alphabet {
    /** @param _alphabet the chars used in this alphabet. */
    private String _alphabet;

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        this._alphabet = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Return chars defined in this Alphabet object. */
    String getChars() {
        return _alphabet;
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _alphabet.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _alphabet.indexOf(ch) != -1;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _alphabet.charAt(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        int potentialIndex = _alphabet.indexOf(ch);
        if (potentialIndex == -1) {
            throw error("char not found in the alphabet.");
        }
        return potentialIndex;
    }

    /** Change the alphabet's first char and its order.
     * @param first the first char of the alphabet
     * according to the ring setting info. */
    void setFirst(char first) {
        int index = _alphabet.indexOf(first);
        if (index == -1) {
            throw error("Intended first char not in alphabet", first);
        }
        _alphabet = _alphabet.substring(index) + _alphabet.substring(0, index);
    }
}

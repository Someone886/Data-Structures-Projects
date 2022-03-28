package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Qianfei(Ben) Hu
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        if (notches == null || notches.equals("") || notches.equals(" ")) {
            _notches = null;
        } else {
            _notches = new char[notches.length()];
            for (int i = 0; i < notches.length(); i += 1) {
                _notches[i] = notches.charAt(i);
            }
        }
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        if (_notches == null) {
            return false;
        }

        for (char i: _notches) {
            int notchPosition = alphabet().toInt(i);
            if (setting() == notchPosition) {
                return true;
            }
        }
        return false;
    }

    @Override
    void advance() {
        set(permutation().wrap((setting() + 1)));
    }

    /** A list of notches' indexes. */
    private char[] _notches;

}

package enigma;

import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Qianfei(Ben) Hu
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        if (numRotors <= 1) {
            throw error("numRotors must >= 1.");
        }
        if (pawls < 0 || pawls >= numRotors) {
            throw error("# of pawls is incorrect.");
        }

        _alphabet = alpha;
        _rotorsNum = numRotors;
        _pawlsNum = pawls;
        _allRotors = allRotors;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _rotorsNum;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawlsNum;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        int curr = 1;
        int fixedRotorNum = 0;
        boolean insertedMoving = false;
        for (String rotor: rotors) {
            boolean inserted = false;
            for (Rotor next : _allRotors) {
                if (next.name().equals(rotor)) {
                    if (curr == 1) {
                        if (!next.reflecting()) {
                            throw error("First rotor must be a R", next);
                        }
                    } else {
                        if (next.reflecting()) {
                            throw error("Try to insert another R", next);
                        }
                    }
                    if (!next.rotates()) {
                        fixedRotorNum += 1;
                    } else {
                        insertedMoving = true;
                    }
                    if (insertedMoving & !next.rotates()) {
                        throw error("Wrong: moving rotor|fixed rotor");
                    }
                    _rotors.put(curr, next);
                    inserted = true;
                }
            }
            if (!inserted) {
                throw error("Failed to insert.");
            }
            curr += 1;
        }

        if (fixedRotorNum != numRotors() - numPawls()) {
            throw error("Wrong number of fixed rotors.");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != _rotors.size() - 1) {
            throw error("Incorrect length of setting",
                    "Setting: " + setting.length()
                            + " | rotors: " + _rotors.size());
        }

        try {
            for (int i = 0; i < setting.length(); i += 1) {
                _rotors.get(i + 2).set(setting.charAt(i));
            }
        } catch (EnigmaException e) {
            throw error("char not found in the alphabet.", setting);
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugPerm = plugboard;
    }

    /** Rotate the rotors before converting. */
    void rotate() {
        boolean[] rotating = new boolean[_rotorsNum + 1];
        rotating[_rotorsNum] = true;

        for (int index = _rotorsNum;
             index > _rotorsNum - _pawlsNum; index -= 1) {
            rotating[index - 1] = _rotors.get(index).atNotch();
        }

        for (int index = _rotorsNum; index > _rotorsNum - _pawlsNum; index--) {
            Rotor thisRotor = _rotors.get(index);
            Rotor leftRotor = _rotors.get(index - 1);
            if ((rotating[index] & thisRotor.rotates())
                    | (rotating[index - 1] & leftRotor.rotates())) {
                thisRotor.advance();
            }
        }
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        rotate();

        int in, out;
        char intermediate;
        in = _plugPerm.permute(c);
        Rotor thisRotor;

        for (int index = _rotorsNum; index > 0; index -= 1) {
            thisRotor = _rotors.get(index);
            intermediate = thisRotor.convertForward(_alphabet.toChar(in));
            out = _alphabet.toInt(intermediate);
            in = out;
        }

        for (int index = 2; index <= _rotorsNum; index += 1) {
            thisRotor = _rotors.get(index);
            intermediate = thisRotor.convertBackward(_alphabet.toChar(in));
            out = _alphabet.toInt(intermediate);
            in = out;
        }

        out = _plugPerm.invert(in);

        return out;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        msg = msg.replaceAll("\\s", "");

        char[] chars = new char[msg.length()];
        for (int i = 0; i < msg.length(); i += 1) {
            chars[i] = _alphabet.toChar(
                    convert(_alphabet.toInt(msg.charAt(i))));
        }

        return new String(chars);
    }

    /** Set the machine's rings based on the input config info.
     * All the characters in the setting info must be in the alphabet.
     * The ring setting should cover all rotors except the reflector.
     * @param ringInfo the ring setting info from config */
    void setRing(String ringInfo) {
        if (ringInfo.length() != _rotors.size() - 1) {
            throw error("Incorrect # of chars for ring", ringInfo);
        }
        for (int index = 2; index < _rotors.size() + 1; index += 1) {
            _rotors.get(index).alphabet().setFirst(ringInfo.charAt(index - 2));
        }
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Permutation of the plugboard. */
    private Permutation _plugPerm;

    /** Number of rotors. */
    private int _rotorsNum;

    /** Number of pawls. */
    private int _pawlsNum;

    /** Rotors' slots. */
    private HashMap<Integer, Rotor> _rotors = new HashMap<>();

    /** All rotors that could be used. */
    private Collection<Rotor> _allRotors;
}

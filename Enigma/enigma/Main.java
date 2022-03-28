package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Qianfei(Ben) Hu
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        _machine = readConfig();
        boolean setUp = false;

        while (_input.hasNextLine()) {
            String info = _input.nextLine();
            Scanner scannedInfo = new Scanner(info);
            if (info.startsWith("*")) {
                String skip = scannedInfo.next();
                setUp = true;
                String[] rotors = new String[_machine.numRotors()];
                for (int i = 0; i < _machine.numRotors(); i++) {
                    rotors[i] = scannedInfo.next();
                }
                checkRepetition(rotors);
                _machine.insertRotors(rotors);

                String positionSetting = scannedInfo.next();
                if (positionSetting.length() != _machine.numRotors() - 1) {
                    throw error("Bad position setting", positionSetting);
                }

                boolean setRing = false;
                StringBuilder plugCycles = new StringBuilder();
                while (scannedInfo.hasNext()) {
                    String next = scannedInfo.next();
                    if (next.contains("(")) {
                        plugCycles.append(next);
                    } else if (!setRing) {
                        _machine.setRing(next);
                        setRing = true;
                    } else {
                        throw error("plugPerm info is wrong");
                    }
                }

                setUpRotorsSetting(_machine, positionSetting);
                _machine.setPlugboard(
                        new Permutation(plugCycles.toString(), _alphabet));
            } else if (info.trim().isEmpty()) {
                _output.append("\n");
            } else if (setUp) {
                String output = _machine.convert(info);
                printMessageLine(output);
            } else {
                throw error("Incorrect config", _input);
            }
        }
    }

    /** Check if a rotor is used more than once when setting up the machine.
     * @param rotors a string array of rotors' names. */
    public static void checkRepetition(String[] rotors) {
        for (int i = 0; i < rotors.length; i += 1) {
            for (int j = i + 1; j < rotors.length; j += 1) {
                if (rotors[i].equals(rotors[j])) {
                    throw error("rotor repetition in setting",
                            Arrays.toString(rotors));
                }
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next()
                    .replaceAll("\\s", ""));
            int rotorsNum = _config.nextInt();
            int pawlsNum = _config.nextInt();
            if (rotorsNum <= pawlsNum) {
                throw error("pawlsNum must be < rotorsNum",
                        pawlsNum + "|" + rotorsNum);
            }

            readRotors();
            return new Machine(_alphabet, rotorsNum, pawlsNum, _allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Add rotors to _allRotors, reading its description from _config. */
    private void readRotors() {
        try {
            ArrayList<String> rotorsInfo = new ArrayList<>();
            String rotorName = _config.hasNext() ? _config.next() : null;

            while (_config.hasNext()) {
                checkUsage(rotorName, rotorsInfo);
                String information = _config.next();
                String type, notches;
                type = checkRotorType(information.charAt(0));
                notches = information.substring(1);
                for (int index = 0; index < notches.length(); index += 1) {
                    if (!_alphabet.contains(notches.charAt(index))) {
                        throw error("notch not found in alphabet", notches);
                    }
                }

                StringBuilder permutation = new StringBuilder();
                String nextInfo = _config.next();
                while (nextInfo.contains("(")) {
                    permutation.append(nextInfo);
                    if (_config.hasNext()) {
                        nextInfo = _config.next();
                    } else {
                        break;
                    }
                }
                addRotor(type, rotorName, permutation.toString(), notches);

                rotorName = nextInfo;
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotors description");
        }
    }

    /** Add the rotor to _allRotors based on this rotor's info.
     * @param notches the notches of the rotor to be added.
     * @param permutation the permutation of the rotor to be added.
     * @param rotorName the name of the rotor to be added.
     * @param type the type of the rotor to be added. */
    public void addRotor(String type, String rotorName,
                         String permutation, String notches) {
        if (type.equals("movingRotor")) {
            _allRotors.add(new MovingRotor(rotorName,
                            new Permutation(permutation,
                                    new Alphabet(_alphabet.getChars())),
                                            notches));
        } else if (type.equals("fixedRotor")) {
            _allRotors.add(new FixedRotor(rotorName,
                            new Permutation(permutation,
                                    new Alphabet(_alphabet.getChars()))));
        } else {
            _allRotors.add(new Reflector(rotorName,
                            new Permutation(permutation,
                                    new Alphabet(_alphabet.getChars()))));
        }
    }

    /** Check if a rotor is already used in the machine.
     * @param rotorsInfo an ArrayList that
     *                   keeps the names of rotors added so for.
     * @param rotorName the name of the current rotor
     *                  to be checked for name usage. */
    public static void checkUsage(String rotorName,
                                  ArrayList<String> rotorsInfo) {
        for (String s : rotorsInfo) {
            if (rotorName.equals(s)) {
                throw error("rotor already used during readConfig", rotorName);
            }
        }
        rotorsInfo.add(rotorName);
    }

    /** Decide the type of the rotor
     * based on the first char of "information" in readConfig().
     * @param typeInfo a char used to determine the type of this rotor.
     * @return the type of this rotor. */
    public static String checkRotorType(char typeInfo) {
        String type;
        if (typeInfo == 'M') {
            type = "movingRotor";
        } else if (typeInfo == 'R') {
            type = "reflector";
        } else if (typeInfo == 'N') {
            type = "fixedRotor";
        } else {
            throw error("Rotor's type not found", typeInfo);
        }
        return type;
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUpRotorsSetting(Machine M, String settings) {
        M.setRotors(settings);
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        while (msg.length() > 0) {
            if (msg.length() > 5) {
                _output.append(msg.substring(0, 5)).append(" ");
                msg = msg.substring(5);
            } else {
                _output.append(msg).append("\n");
                break;
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** The machine that is being setup. */
    private Machine _machine;

    /** All rotors read from the config file
     * and to be used in the machine. */
    private ArrayList<Rotor> _allRotors = new ArrayList<>();
}

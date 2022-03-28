package gitlet;

import java.io.IOException;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 * @author Qianfei(Ben) Hu
 */
public class Main {
    /** The main method.
     *
     * @param args the input arguments.
     * @throws IOException
     */
    public static void main(String... args) throws IOException {
        if (args.length <= 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("init")) {
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.init();
                System.exit(res);
            }
        } else if (args[0].equals("add")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.add(args[1]);
                System.exit(res);
            }
        } else if (args[0].equals("rm")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.rm(args[1]);
                System.exit(res);
            }
        } else if (args[0].equals("commit")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.commit(args[1]);
                System.exit(res);
            }
        } else if (args[0].equals("log")) {
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.log();
                System.exit(res);
            }
        } else if (args[0].equals("global-log")) {
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.globalLog();
                System.exit(res);
            }
        } else if (args[0].equals("find")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.find(args[1]);
                System.exit(res);
            }
        } else {
            mainHelper(args);
        }
    }

    /** The helper function of Main.
     * @param args the args passed in.
     * */
    public static void mainHelper(String[] args) throws IOException {
        if (args[0].equals("status")) {
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.status();
                System.exit(res);
            }
        } else if (args[0].equals("checkout")) {
            if (args.length == 2) {
                int res = GitletCommandRunner.checkoutBranch(args[1]);
                System.exit(res);
            } else if (args.length == 3 && args[1].equals("--")) {
                int res = GitletCommandRunner.checkoutFile(args[2]);
                System.exit(res);
            } else if (args.length == 4 && args[2].equals("--")) {
                int res = GitletCommandRunner.
                        checkoutCommitFile(args[1], args[3]);
                System.exit(res);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else if (args[0].equals("branch")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.branch(args[1]);
                System.exit(res);
            }
        } else if (args[0].equals("rm-branch")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.rmBranch(args[1]);
                System.exit(res);
            }
        } else if (args[0].equals("reset")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.reset(args[1]);
                System.exit(res);
            }
        } else if (args[0].equals("merge")) {
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                int res = GitletCommandRunner.merge(args[1]);
                System.exit(res);
            }
        } else {
            remoteHelper(args);
        }
        System.exit(0);
    }

    /** Remote commands.
     * @param args the args passed in.
     * */
    public static void remoteHelper(String[] args) throws IOException {
        if (args[0].equals("add-remote")) {
            if (args.length == 3) {
                GitletCommandRunner.addRemove(args[1], args[2]);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else if (args[0].equals("rm-remote")) {
            if (args.length == 2) {
                GitletCommandRunner.rmRemote(args[1]);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else if (args[0].equals("fetch")) {
            if (args.length == 3) {
                GitletCommandRunner.fetch(args[1], args[2]);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else if (args[0].equals("pull")) {
            if (args.length == 3) {
                GitletCommandRunner.pull(args[1], args[2]);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else if (args[0].equals("push")) {
            if (args.length == 3) {
                GitletCommandRunner.push(args[1], args[2]);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else {
            System.out.println("No command with that name exists.");
        }
        System.exit(0);
    }
}

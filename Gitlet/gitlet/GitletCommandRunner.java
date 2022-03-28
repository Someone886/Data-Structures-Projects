package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Qianfei(Ben) Hu
 * */
public class GitletCommandRunner {

    /** The INIT command.
     *
     * @return exit code.
     * */
    public static int init() {
        File gitletFolder = new File(".gitlet");
        boolean exists = gitletFolder.exists();

        if (exists) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return 0;
        } else {
            GitletCommit firstCommit = new GitletCommit(null);
            firstCommit.finish("initial commit");

            GitletData gitletData = new GitletData();
            gitletData.addGitletCommit(firstCommit);

            gitletFolder.mkdirs();

            File gitletDataFile = new File(".gitlet/data");
            Utils.writeObject(gitletDataFile, gitletData);
            return 0;
        }
    }

    /** The ADD command.
     * @param filename the name of the file to be added
     * @return exit code.
     */
    public static int add(String filename) throws IOException {
        File gitletFile = new File(filename);
        boolean exists = gitletFile.exists();

        if (!exists) {
            System.out.println("File does not exist.");
            return 0;
        } else {
            GitletData gitletData = GitletData.loadGitletData();
            GitletStage gitletStage = GitletStage.loadGitletStage();

            GitletBlob oldGitletBlob = gitletData.getBlob(filename);
            String contentHash = Utils.sha1(Utils.
                    readContentsAsString(gitletFile));

            gitletStage.getRemovedBlobMap().remove(filename);
            GitletStage.saveGitletStage(gitletStage);

            if (oldGitletBlob != null && oldGitletBlob.getHash().
                    equals(contentHash)) {
                if (gitletStage.getAddedBlobMap().containsKey(filename)) {
                    File dest = GitletStage.getStageFile(
                            gitletStage.getAddedBlobMap().get(filename));
                    Utils.restrictedDelete(dest);
                    gitletStage.unstageAddedBlob(filename);
                } else {
                    return 0;
                }
            } else {
                int version = 1;
                if (oldGitletBlob != null) {
                    version = oldGitletBlob.getVersion() + 1;
                }
                GitletBlob updateBlob = new GitletBlob(filename, version);
                File dest = GitletStage.getStageFile(updateBlob);
                Utils.writeContents(dest, Utils.
                        readContentsAsString(gitletFile));
                gitletStage.stageAddedBlob(filename, updateBlob);
            }

            GitletStage.saveGitletStage(gitletStage);
            return 0;
        }
    }

    /** The RM command.
     * @param filename the name of file
     * @return exit code.
     */
    public static int rm(String filename) {
        File gitletFile = new File(filename);
        boolean exists = gitletFile.exists();

        GitletData gitletData = GitletData.loadGitletData();
        GitletStage gitletStage = GitletStage.loadGitletStage();

        if (gitletStage.getAddedBlobMap().containsKey(filename)) {
            gitletStage.unstageAddedBlob(filename);
        } else if (gitletData.getCurrentCommit().getBlob(filename) != null) {
            gitletStage.stageRemovedBlod(filename);
            if (exists) {
                Utils.restrictedDelete(gitletFile);
            }
        } else {
            System.out.println("No reason to remove the file.");
            return 0;
        }

        GitletStage.saveGitletStage(gitletStage);
        return 0;
    }

    /** The COMMIT command.
     * @param message the message to be printed out.
     * @param commit the commit to be committed.
     * @return An object of GitletData.
     */
    public static GitletData commit(String message, GitletCommit commit) {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
        }

        GitletStage gitletStage = GitletStage.loadGitletStage();

        GitletData gitletData = GitletData.loadGitletData();
        GitletCommit parent = commit.getParent();

        if (parent != null) {
            HashMap<String, GitletBlob> blobMap = parent.getBlobMap();
            for (Entry<String, GitletBlob> entry : blobMap.entrySet()) {
                commit.addBlob(entry.getKey(), entry.getValue());
            }
        }

        for (Entry<String, GitletBlob> entry : gitletStage.
                getAddedBlobMap().entrySet()) {
            commit.addBlob(entry.getKey(), entry.getValue());
        }
        for (String fileName : gitletStage.getRemovedBlobMap()) {
            commit.removeBlob(fileName);
        }
        gitletStage.clear();

        commit.finish(message);
        gitletData.setCurrentCommit(commit);
        GitletData.saveGitletData(gitletData);
        GitletStage.saveGitletStage(gitletStage);
        return gitletData;
    }

    /** The COMMIT command.
     *
     * @param message the message to be committed.
     * @return exit code.
     */
    public static int commit(String message) {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return 0;
        }

        GitletStage gitletStage = GitletStage.loadGitletStage();
        if (gitletStage == null || gitletStage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return 0;
        }

        GitletData gitletData = GitletData.loadGitletData();
        GitletCommit currentCommit = gitletData.getCurrentCommit();

        GitletCommit commit = new GitletCommit(currentCommit);
        if (currentCommit != null) {
            HashMap<String, GitletBlob> blobMap = currentCommit.getBlobMap();
            for (Entry<String, GitletBlob> entry : blobMap.entrySet()) {
                commit.addBlob(entry.getKey(), entry.getValue());
            }
        }

        for (Entry<String, GitletBlob> entry : gitletStage.
                getAddedBlobMap().entrySet()) {
            commit.addBlob(entry.getKey(), entry.getValue());
        }
        for (String fileName : gitletStage.getRemovedBlobMap()) {
            commit.removeBlob(fileName);
        }
        gitletStage.clear();

        commit.finish(message);
        gitletData.addGitletCommit(commit);
        GitletData.saveGitletData(gitletData);
        GitletStage.saveGitletStage(gitletStage);
        return 0;
    }

    /** The LOG command.
     *
     * @return exit code.
     */
    public static int log() {
        GitletData gitletData = GitletData.loadGitletData();
        GitletCommit currentCommit = gitletData.getCurrentCommit();

        GitletCommit tmp = currentCommit;
        SimpleDateFormat dateFm = new SimpleDateFormat("Z");
        while (tmp != null) {
            System.out.println("===");
            System.out.println("commit " + tmp.getHash());

            String dateStr = dateFm.format(tmp.getDate());
            String date = (tmp.getDate() + "").replace(" PDT", "");
            System.out.println("Date: " + date + " " + dateStr);
            System.out.println(tmp.getMessage());
            System.out.println();

            tmp = tmp.getParent();
        }

        return 0;
    }

    /** The GLOGBALLOG command.
     *
     * @return exit code.
     */
    public static int globalLog() {
        GitletData gitletData = GitletData.loadGitletData();

        SimpleDateFormat dateFm = new SimpleDateFormat("Z");
        for (Entry<String, GitletCommit> entry : gitletData.
                getCommitMap().entrySet()) {

            GitletCommit tmp = entry.getValue();
            System.out.println("===");
            System.out.println("commit " + tmp.getHash());

            String dateStr = dateFm.format(tmp.getDate());
            String date = (tmp.getDate() + "").replace(
                    " PDT", "");
            System.out.println("Date: " + date + " " + dateStr);
            System.out.println(tmp.getMessage());
            System.out.println();

        }
        return 0;
    }

    /** The FIND command.
     * @param message the message that needs to be printed.
     * @return exit code.
     */
    public static int find(String message) {
        GitletData gitletData = GitletData.loadGitletData();
        boolean found = false;

        for (Entry<String, GitletCommit> entry : gitletData.
                getCommitMap().entrySet()) {

            GitletCommit tmp = entry.getValue();
            if (tmp.getMessage().equals(message)) {
                found = true;
                System.out.println(tmp.getHash());
            }

        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }

        return 0;
    }

    /** The BRANCH command.
     * @param branchName the name of the branch
     * @return exit code.
     */
    public static int branch(String branchName) {
        GitletData gitletData = GitletData.loadGitletData();

        if (gitletData.getBranchMap().containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return 0;
        }

        gitletData.branch(branchName);
        GitletData.saveGitletData(gitletData);

        return 0;
    }

    /** The RMBRANCH command.
     * @param branchName the name of the branch
     * @return exit code.
     */
    public static int rmBranch(String branchName) {
        GitletData gitletData = GitletData.loadGitletData();

        if (!gitletData.getBranchMap().containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return 0;
        }

        if (gitletData.getCurrentBranch().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return 0;
        }

        gitletData.removeBranch(branchName);
        GitletData.saveGitletData(gitletData);

        return 0;
    }

    /** The STATUS command.
     *
     * @return exit code.
     */
    public static int status() {
        GitletData gitletData
                = GitletData.loadGitletData();
        String currentBranch = gitletData.getCurrentBranch();
        GitletCommit currentCommit = gitletData.getCurrentCommit();

        if (currentCommit == null) {
            System.out.println("Not in an initialized Gitlet directory.");
            return 0;
        }

        GitletStage gitletStage = GitletStage.loadGitletStage();

        List<String> branchList
                = new ArrayList<>(gitletData.getBranchMap().keySet());
        Collections.sort(branchList);
        System.out.println("=== Branches ===");
        System.out.println("*" + currentBranch);
        for (String branch : branchList) {
            if (!branch.equals(currentBranch)) {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> addedList = new ArrayList<>(
                gitletStage.getAddedBlobMap().keySet());
        Collections.sort(addedList);
        for (String addedfile : addedList) {
            System.out.println(addedfile);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        return statusHelper(gitletStage, currentCommit);
    }

    /** Status helper of Status().
     * @param gitletStage the current stage.
     * @param currentCommit the current commit.
     * @return the exit code.
     * */
    public static int statusHelper(GitletStage gitletStage,
                                   GitletCommit currentCommit) {
        List<String> removedList
                = new ArrayList<>(gitletStage.getRemovedBlobMap());
        Collections.sort(removedList);
        for (String removedFile : removedList) {
            System.out.println(removedFile);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> filenameList = Utils.plainFilenamesIn(".");
        List<String> notStagedList = new ArrayList<String>();
        for (String filename : filenameList) {
            if (currentCommit.getBlobMap().containsKey(filename)) {
                if (!gitletStage.getAddedBlobMap().containsKey(filename)) {
                    File gitletFile = new File(filename);
                    File commitBlobFile = currentCommit.getBlobMap().
                            get(filename).getFile();
                    String contentHash = Utils.sha1(Utils.
                            readContentsAsString(gitletFile));
                    String commitBlobContentHash = Utils.sha1(
                            Utils.readContentsAsString(commitBlobFile));

                    if (!contentHash.equals(commitBlobContentHash)) {
                        notStagedList.add(filename + " (modified)");
                    }
                }
            }
        }

        for (String filename : currentCommit.getBlobMap().keySet()) {
            if (filenameList.indexOf(filename) < 0
                    && !gitletStage.getRemovedBlobMap().contains(filename)) {
                notStagedList.add(filename + " (deleted)");
            }
        }
        Collections.sort(notStagedList);
        for (String file : notStagedList) {
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        List<String> untrackedList = new ArrayList<String>();
        for (String filename : filenameList) {
            if (!currentCommit.getBlobMap().containsKey(filename)
                    && !gitletStage.getAddedBlobMap().containsKey(filename)
                    && !gitletStage.getRemovedBlobMap().contains(filename)) {
                untrackedList.add(filename);
            }
        }

        Collections.sort(untrackedList);
        for (String file : untrackedList) {
            System.out.println(file);
        }
        System.out.println();

        return 0;
    }

    /** The RESET command.
     * @param commitId the id of the commit
     * @return exit code.
     */
    public static int reset(String commitId) {
        GitletData gitletData = GitletData.loadGitletData();
        GitletCommit currentCommit = gitletData.getCurrentCommit();
        GitletStage gitletStage = GitletStage.loadGitletStage();

        if (!gitletData.getCommitMap().containsKey(commitId)) {
            System.out.println("No commit with that id exists.");
            return 0;
        }

        List<String> filenameList = Utils.plainFilenamesIn(".");
        for (String filename : filenameList) {
            if (!filename.startsWith(".")
                    && !currentCommit.getBlobMap().containsKey(filename)
                    && !gitletStage.getAddedBlobMap().containsKey(filename)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return 0;
            }
        }

        GitletCommit destCommit = gitletData.getCommitMap().get(commitId);
        for (String filename : filenameList) {
            Utils.restrictedDelete(filename);
        }
        for (Entry<String, GitletBlob> entry
                : destCommit.getBlobMap().entrySet()) {
            GitletBlob blob = entry.getValue();
            File workFile = new File(blob.getFilename());
            boolean exists = workFile.exists();
            if (exists) {
                Utils.restrictedDelete(workFile);
            }

            File gitFile = blob.getFile();
            Utils.writeContents(workFile, Utils.readContentsAsString(gitFile));
        }

        gitletStage.clear();
        GitletStage.saveGitletStage(gitletStage);
        gitletData.setCurrentCommit(destCommit);
        GitletData.saveGitletData(gitletData);

        return 0;
    }

    /** Merge two branches.
     * @param mergedName name of the merged branch.
     * @return exit code.
     * */
    public static int merge(String mergedName) {
        GitletData data = GitletData.loadGitletData();
        String curr = data.getCurrentBranch();
        GitletCommit current = data.getCurrentCommit();
        GitletStage stage = GitletStage.loadGitletStage();
        if (requirementCheck(stage, mergedName, data, current) == 0) {
            return 0;
        }
        boolean conflict = false;
        GitletCommit merged = data.getBranchMap().get(mergedName);
        HashSet<String> set = new HashSet<>();
        HashMap<Integer, GitletCommit> res = getAnc(current, merged, set);
        if (!res.containsKey(0)) {
            GitletCommit commit = new GitletCommit(current, merged);
            for (String file : set) {
                GitletBlob blob = current.getBlob(file);
                GitletBlob given = merged.getBlob(file);
                GitletBlob ancestor = (res.get(1) == null)
                        ? null : res.get(1).getBlob(file);
                if (!modified(ancestor, blob)) {
                    if (exist(given)) {
                        commit.addBlob(file, given);
                        stage.stageAddedBlob(file, given);
                        Utils.restrictedDelete(new File(file));
                        Utils.writeContents(new File(file), Utils.
                                readContentsAsString(given.getFile()));
                    } else {
                        rm(file);
                        stage.stageRemovedBlod(file);
                    }
                } else if (!modified(ancestor, given)
                        && !isEmpty(set, merged)) {
                    continue;
                } else {
                    if (given != null
                            && blob.getHash().equals(given.getHash())) {
                        commit.addBlob(file, blob);
                    } else if (!isEmpty(set, merged) || (isEmpty(set, merged)
                            && merged.getParent().getBlob(file) != null)) {
                        Utils.restrictedDelete(new File(file));
                        Utils.writeContents(new File(file), cont(blob, given));
                        conflict = true;
                        int vers = 1;
                        if (blob != null) {
                            vers = Math.max(vers, blob.getVersion() + 1);
                        }
                        if (blob != null && given != null) {
                            vers = Math.max(vers, given.getVersion() + 1);
                        }
                        GitletBlob updateBlob = new GitletBlob(file, vers);
                        File destFile = GitletStage.getStageFile(updateBlob);
                        Utils.writeContents(destFile, cont(blob, given));
                        stage.stageAddedBlob(file, updateBlob);
                    }
                }
            }
            saveGitletData(stage, data, mergedName, curr, commit, conflict);
        }
        return 0;
    }

    /** Merge helper.
     * @param stage current stage.
     * @param branchName current branch's name.
     * @param data current data.
     * @param currentCommit current commit.
     * @return true iff there is no error.
     * */
    public static int requirementCheck(
            GitletStage stage, String branchName,
            GitletData data, GitletCommit currentCommit) {
        if (!stage.getAddedBlobMap().isEmpty()
                || !stage.getRemovedBlobMap().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return 0;
        }
        if (!data.getBranchMap().containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return 0;
        }
        if (branchName.equals(data.getCurrentBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            return 0;
        }
        List<String> fileList = Utils.plainFilenamesIn(".");
        for (String f : fileList) {
            if (!currentCommit.getBlobMap().containsKey(f)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return 0;
            }
        }
        return 1;
    }

    /** Get ancestor commit of two branches.
     * @param set set of all set.
     * @param currentCommit current commit.
     * @param merged commit of merged branch.
     * @return the ancestortor commit.
     * */
    public static HashMap<Integer, GitletCommit> getAnc(
            GitletCommit currentCommit,
            GitletCommit merged,
            HashSet<String> set) {
        GitletCommit ancestortor = null;
        HashSet<GitletCommit> ancestortorSet = new HashSet<>();
        GitletCommit curr = currentCommit;
        HashMap<Integer, GitletCommit> result = new HashMap<>();
        while (curr != null) {
            ancestortorSet.add(curr);
            curr = curr.getParent();
        }
        curr = merged;
        while (curr != null) {
            if (ancestortorSet.contains(curr)) {
                ancestortor = curr;
                break;
            }
            curr = curr.getParent();
        }
        if (ancestortor != null && ancestortor.getHash().
                equals(merged.getHash())) {
            System.out.println("Given branch is an ancestortor of "
                    + "the current branch.");
            result.put(0, ancestortor);
            return result;
        }
        if (ancestortor != null
                && ancestortor.getHash().equals(currentCommit.
                getHash())) {
            System.out.println("Current branch fast-forwarded.");
        }
        HashMap<String, GitletBlob> currBlobMap = currentCommit.getBlobMap();
        for (Map.Entry<String, GitletBlob> blob : currBlobMap.entrySet()) {
            set.add(blob.getKey());
        }
        for (Map.Entry<String, GitletBlob> blob : merged.getBlobMap().
                entrySet()) {
            set.add(blob.getKey());
        }
        curr = merged.getParent();
        while (curr != null) {
            if (currentCommit.getSendParent() != null) {
                if (currentCommit.getSendParent().equals(curr)) {
                    for (Map.Entry<String, GitletBlob> entry
                            : currBlobMap.entrySet()) {
                        String key = entry.getKey();
                        if (curr.getBlobMap().keySet().contains(key)
                                && !merged.getBlobMap().
                                keySet().contains(key)) {
                            rm(key);
                        }
                    }
                }
            }
            curr = curr.getParent();
        }
        result.put(1, ancestortor);
        return result;
    }

    /** String of the file with conflict.
     * @param blob the current GitletBlob.
     * @param givenBlob the merged GitletBlob.
     * @return the new content.
     * */
    public static String cont(GitletBlob blob, GitletBlob givenBlob) {
        String content = "<<<<<<< HEAD";
        if (!exist(blob)) {
            content = content + "\n=======";
        } else {
            content = content + "\n";
            content = content + Utils.
                    readContentsAsString(blob.getFile());
            content = content + "=======";
        }
        if (!exist(givenBlob)) {
            content = content + "\n>>>>>>>\n";
        } else {
            content = content + "\n";
            content = content + Utils.
                    readContentsAsString(givenBlob.getFile());
            content = content + ">>>>>>>\n";
        }
        return content;
    }

    /** Check branch for its emptiness.
     * @param set the set.
     * @param merged the commit of merged branch.
     * @return empty branch or not.
     */
    public static boolean isEmpty(HashSet<String> set, GitletCommit merged) {
        boolean isEmpty = true;
        GitletBlob blob;
        for (String file : set) {
            blob = merged.getBlob(file);
            if (blob != null) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }



    /** Save data.
     * @param gitletStage current GitletStage.
     * @param currentBranch current Branch name.
     * @param gitletCommit current commit.
     * @param gitletData current data.
     * @param branchName name of the merged branch.
     * @param conflict if there is a conflict.
     */
    public static void saveGitletData(
            GitletStage gitletStage, GitletData
            gitletData, String branchName, String currentBranch,
            GitletCommit gitletCommit, boolean conflict) {
        GitletStage.saveGitletStage(gitletStage);
        GitletData.saveGitletData(gitletData);
        String message =
                "Merged " + branchName + " into " + currentBranch + ".";
        gitletData = commit(message, gitletCommit);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        GitletData.saveGitletData(gitletData);
        gitletCommit.setSendParent(gitletData.getBranchMap().get(branchName));
        GitletData.saveGitletData(gitletData);
    }

    /** Check whether the current blob is modified.
     * @param ancestorBlob the ancestor of the blob
     * @param blob the current blob
     * @return true if the current blob is modified from the ancestortor blob.
     */
    private static boolean modified(GitletBlob ancestorBlob, GitletBlob blob) {
        if (ancestorBlob == null && blob == null) {
            return false;
        }
        if (blob == null || ancestorBlob == null) {
            return true;
        }
        return !ancestorBlob.getHash().equals(blob.getHash());
    }

    /** The method of checking whether the blob exists.
     * @param blob the current blob.
     * @return true iff blob is not null.
     */
    private static boolean exist(GitletBlob blob) {
        return blob != null;
    }

    /** The checkoutBranch command.
     * @param branchName the name of the current branch.
     * @return exit code.
     */
    public static int checkoutBranch(String branchName) {
        GitletData gitletData = GitletData.loadGitletData();

        if (!gitletData.getBranchMap().containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return 0;
        }
        if (gitletData.getCurrentBranch().equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return 0;
        }

        GitletCommit currentCommit = gitletData.getCurrentCommit();
        GitletCommit destCommit = gitletData.getBranchMap().get(branchName);
        List<String> filenameList = Utils.plainFilenamesIn(".");
        for (String filename : filenameList) {
            if (!currentCommit.getBlobMap().containsKey(filename)
                    && destCommit.getBlobMap().containsKey(filename)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return 0;
            }
        }

        for (Entry<String, GitletBlob> entry : destCommit.getBlobMap().
                entrySet()) {
            GitletBlob blob = entry.getValue();
            File workFile = new File(blob.getFilename());
            boolean exists = workFile.exists();
            if (exists) {
                Utils.restrictedDelete(workFile);
            }

            File gitFile = blob.getFile();
            Utils.writeContents(workFile, Utils.readContentsAsString(gitFile));
        }

        for (String filename : filenameList) {
            if (!destCommit.getBlobMap().containsKey(filename)
                    && currentCommit.getBlobMap().containsKey(filename)) {
                Utils.restrictedDelete(filename);
            }
        }

        gitletData.setCurrentBranch(branchName);
        GitletData.saveGitletData(gitletData);
        return 0;
    }

    /** The checkoutFile command.
     * @param fileName the name of the file to checkout.
     * @return exit code.
     */
    public static int checkoutFile(String fileName) {
        GitletData gitletData = GitletData.loadGitletData();

        GitletCommit currentCommit = gitletData.getCurrentCommit();

        if (!currentCommit.getBlobMap().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return 0;
        }

        File workFile = new File(fileName);
        boolean exists = workFile.exists();
        if (exists) {
            Utils.restrictedDelete(workFile);
        }

        File gitFile = currentCommit.getBlobMap().get(fileName).getFile();
        Utils.writeContents(workFile, Utils.readContentsAsString(gitFile));

        GitletData.saveGitletData(gitletData);
        return 0;
    }

    /** The checkoutCommitFile command.
     * @param commitId the id of commit to checkout.
     * @param fileName the name of file to checkout.
     * @return exit code.
     */
    public static int checkoutCommitFile(String commitId, String fileName) {
        GitletData gitletData = GitletData.loadGitletData();
        GitletCommit commit = null;
        for (Entry<String, GitletCommit> entry : gitletData.
                getCommitMap().entrySet()) {
            if (entry.getKey().startsWith(commitId)) {
                commit = entry.getValue();
            }
        }

        if (commit == null) {
            System.out.println("No commit with that id exists.");
            return 0;
        }

        if (!commit.getBlobMap().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return 0;
        }

        File workFile = new File(fileName);
        boolean exists = workFile.exists();
        if (exists) {
            Utils.restrictedDelete(workFile);
        }

        File gitFile = commit.getBlobMap().get(fileName).getFile();
        Utils.writeContents(workFile, Utils.readContentsAsString(gitFile));

        GitletData.saveGitletData(gitletData);
        return 0;
    }

    /** Add a remote branch.
     * @param branchName the remote branch's name.
     * @param remoteDir the remote dir's name.
     */
    public static void addRemove(String branchName, String remoteDir) {
        GitletData data = GitletData.loadGitletData();
        if (data.getRemote().containsKey(branchName)) {
            System.out.println("A remote with that name already exists.");
            return;
        }
        data.getRemote().put(branchName, remoteDir);
        GitletData.saveGitletData(data);
    }

    /** Remove a remote branch.
     * @param branchName the branch's name.
     */
    public static void rmRemote(String branchName) {
        GitletData data = GitletData.loadGitletData();
        if (!data.getRemote().containsKey(branchName)) {
            System.out.println("A remote with that name does not exist.");
            return;
        }
        data.getRemote().remove(branchName);
        GitletData.saveGitletData(data);
    }

    /** Fetch the commits in a remote branch.
     * @param remoteName remote name
     * @param remoteBranch remote branch name
     */
    public static void fetch(String remoteName, String remoteBranch)
            throws IOException {
        GitletData data = GitletData.loadGitletData();
        String remote = data.getRemote().get(remoteName);
        File remoteDirectory = new File(remote);
        if (!remoteDirectory.exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        if (data.getBranchMap().containsKey(remoteBranch)) {
            String rcID = data.getBranchCommit(remoteBranch).getHash();
            while (!data.getAllCommitsID().contains(rcID)) {
                GitletCommit rc = data.getCommitMap().get(rcID);
                data.getAllCommitsID().add(rcID);
                File lc = new File(rcID);
                if (!lc.exists()) {
                    lc.createNewFile();
                }
                for (String str : rc.getBlobMap().keySet()) {
                    File rBlob = new File(rc.getBlobMap().get(str).getHash());
                    File lBlob = new File(rc.getBlobMap().get(str).getHash());
                    if (!lBlob.exists()) {
                        lBlob.createNewFile();
                    }
                    Utils.writeContents(lBlob,
                            Utils.readContents(rBlob));
                }
                Utils.writeObject(lc, rc);
                rcID = rc.getParent().getHash();
            }
            String name = remoteName + "/" + remoteBranch;
            data.getBranchMap().
                    put(name, data.getBranchMap().get(remoteBranch));
            GitletData.saveGitletData(data);
        } else {
            System.out.println("That remote does not have that branch.");
            return;
        }
    }

    /** To push a branch to the remote dic.
     * @param remoteName remote name
     * @param remoteBranch remote branch name
     */
    public static void push(String remoteName, String remoteBranch)
            throws IOException {
        GitletData data = GitletData.loadGitletData();
        String remotepath = data.getRemote().get(remoteName);
        File remotedir = new File(remotepath);
        if (!remotedir.exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        GitletCommit commit = data.getCurrentCommit();
        String localID = commit.getHash();
        GitletCommit local = commit.getParent();
        ArrayList<String> allancestors = new ArrayList<>();
        while (local != null) {
            allancestors.add(local.getHash());
            local = local.getParent();
        }
        String rcID = data.getBranchMap().get(remoteBranch).getHash();
        if (allancestors.contains(rcID)) {
            while (!rcID.equals(localID)) {
                GitletCommit lcommit = data.getCommitMap().get(localID);
                data.getAllCommitsID().add(localID);
                File targetcommit = new File(localID);
                if (!targetcommit.exists()) {
                    targetcommit.createNewFile();
                }
                for (String str : lcommit.getBlobMap().keySet()) {
                    File lBlob = new File(lcommit.getBlob(str).getHash());
                    File rBlob = new File(lcommit.getBlob(str).getHash());
                    if (!rBlob.exists()) {
                        rBlob.createNewFile();
                    }
                    Utils.writeContents(rBlob,
                            Utils.readContents(lBlob));
                }
                Utils.writeObject(targetcommit, lcommit);
                localID = lcommit.getParent().getHash();
            }
            data.getBranchMap().put(remoteBranch, data.getCurrentCommit());
            GitletData.saveGitletData(data);
        } else {
            System.out.println("Please pull down remote "
                    + "changes before pushing.");
            return;
        }
    }

    /** Pull a remote branch.
     * @param remoteName remote name
     * @param remoteBranch remote branch name
     * @throws IOException
     */
    public static void pull(String remoteName, String remoteBranch)
            throws IOException {
        fetch(remoteName, remoteBranch);
        String name = remoteName + "/" + remoteBranch;
        merge(name);
    }

}

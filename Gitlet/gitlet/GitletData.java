package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** GitletData class.
 * @author Qianfei(Ben) Hu
 * */
public class GitletData implements Serializable {
    /** Saving the commits. */
    private HashMap<String, GitletCommit> commitMap;
    /** Saving the branches. */
    private HashMap<String, GitletCommit> branchfMap;
    /** Saving the current branch. */
    private String currentBranch = "master";
    /** Remote branch and directory. */
    private HashMap<String, String> remoteMap;
    /** All commits ID. */
    private ArrayList<String> commitsID;

    /** Constructor of GitletData. */
    public GitletData() {
        commitMap = new HashMap<>();
        branchfMap = new HashMap<>();
        remoteMap = new HashMap<>();
        commitsID = new ArrayList<>();
    }

    /** Return remote map. */
    public HashMap<String, String> getRemote() {
        return remoteMap;
    }

    /** Return all commits ID. */
    public ArrayList<String> getAllCommitsID() {
        return commitsID;
    }

    /** Return the data of the gitlet. */
    public static GitletData loadGitletData() {
        File gitletDataFile = new File(".gitlet/data");
        if (gitletDataFile.exists()) {
            GitletData gitletData
                    = Utils.readObject(gitletDataFile, GitletData.class);
            return gitletData;
        } else {
            GitletData gitletData = new GitletData();
            return gitletData;
        }
    }

    /** To Save the data into the current gitlet.
     * @param gitletData the data to be saved. */
    public static void saveGitletData(GitletData gitletData) {
        File gitletDataFile = new File(".gitlet/data");
        Utils.writeObject(gitletDataFile, gitletData);
    }

    /** Return the name of the current Branch. */
    public String getCurrentBranch() {
        return currentBranch;
    }

    /** Set the current branch.
     * @param curr the current branch to be set. */
    public void setCurrentBranch(String curr) {
        currentBranch = curr;
    }

    /** Return the hash map of the branches. */
    HashMap<String, GitletCommit> getBranchMap() {
        return this.branchfMap;
    }

    /** Put the branch named branchName into the current branches map.
     * @param branchName the name of the branch. */
    public void branch(String branchName) {
        this.branchfMap.put(branchName, branchfMap.get(currentBranch));
    }

    /** Remove the branch named branchName.
     * @param branchName the name of branch to be removed. */
    public void removeBranch(String branchName) {
        this.commitsID.remove(branchfMap.get(branchName).getHash());
        this.branchfMap.remove(branchName);
    }

    /** Add the commit named gitletCommit into the current Gitlet.
     * @param gitletCommit the commit to be added. */
    public void addGitletCommit(GitletCommit gitletCommit) {
        commitMap.put(gitletCommit.getHash(), gitletCommit);
        branchfMap.remove(currentBranch);
        branchfMap.put(currentBranch, gitletCommit);
        commitsID.add(gitletCommit.getHash());
    }

    /** Return the current commit. */
    public GitletCommit getCurrentCommit() {
        return branchfMap.get(currentBranch);
    }

    /** Return the commit from the branch named branchName.
     * @param branchName the name of the branch to be returned. */
    public GitletCommit getBranchCommit(String branchName) {
        return branchfMap.get(branchName);
    }

    /** Set the current commit from gitletCommit.
     * @param gitletCommit the commit to be set. */
    public void setCurrentCommit(GitletCommit gitletCommit) {
        commitMap.put(gitletCommit.getHash(), gitletCommit);
        branchfMap.remove(currentBranch);
        branchfMap.put(currentBranch, gitletCommit);
        commitsID.add(gitletCommit.getHash());
    }

    /** Return the hashMap of commits. */
    public HashMap<String, GitletCommit> getCommitMap() {
        return this.commitMap;
    }

    /** Return the Gitlet blob from the file named fileName.
     * @param fileName the name of the file to be returned. */
    public GitletBlob getBlob(String fileName) {
        if (branchfMap.get(currentBranch) == null) {
            return null;
        }
        return branchfMap.get(currentBranch).getBlob(fileName);
    }

}

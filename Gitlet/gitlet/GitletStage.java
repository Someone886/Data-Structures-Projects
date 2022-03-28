package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/** GitletStage class.
 * @author Qianfei(Ben) Hu
 * */
public class GitletStage implements Serializable {
    /** Saving the Added Blobs. */
    private HashMap<String, GitletBlob> addedBlobMap;

    /** Saving the Removed Blobs. */
    private HashSet<String> removedBlobSet;

    /** Constructor of GitletStage. */
    public GitletStage() {
        removedBlobSet = new HashSet<>();
        addedBlobMap = new HashMap<>();
    }

    /** Return the gitlet stage. */
    public static GitletStage loadGitletStage() {
        File gitletStageFile = new File(".gitlet/stage");
        if (gitletStageFile.exists()) {
            GitletStage gitletStage = Utils.readObject(gitletStageFile,
                    GitletStage.class);
            return gitletStage;
        } else {
            GitletStage gitletStage = new GitletStage();
            return gitletStage;
        }
    }

    /** To save the gitlet stage to gitletStage.
     * @param gitletStage the stage to be saved. */
    public static void saveGitletStage(GitletStage gitletStage) {
        File gitletStageFile = new File(".gitlet/stage");
        Utils.writeObject(gitletStageFile, gitletStage);
    }

    /** Remove the file named fileName from the addedbBlobMap.
     * @param fileName the name of the file to be removed
     *                    from the staging place. */
    public void unstageAddedBlob(String fileName) {
        this.addedBlobMap.remove(fileName);
    }

    /** Add the file named fileName and with blob into the staging place.
     * @param fileName the name of the file.
     * @param gitletBlob the blob of the file. */
    public void stageAddedBlob(String fileName, GitletBlob gitletBlob) {
        this.addedBlobMap.put(fileName, gitletBlob);
    }

    /** Remove the file named fileName from the Removed Blob Set.
     * @param fileName the name of the file to be removed. */
    public void stageRemovedBlod(String fileName) {
        this.removedBlobSet.add(fileName);
    }

    /** Return the hashMap of the Added Blob Map. */
    public HashMap<String, GitletBlob> getAddedBlobMap() {
        return addedBlobMap;
    }

    /** Return the hashMap of the Removed Blob collection. */
    public HashSet<String> getRemovedBlobMap() {
        return removedBlobSet;
    }

    /** Return true iff the addedBlobMap and the removedBlobSet are
     *  both empty. */
    public boolean isEmpty() {
        return this.addedBlobMap.isEmpty() && this.removedBlobSet.isEmpty();
    }

    /** Clear the two collections of blobs. */
    public void clear() {
        this.addedBlobMap.clear();
        this.removedBlobSet.clear();
    }

    /** Return the staged file from blob.
     * @param blob the content of the file to be staged. */
    public static File getStageFile(GitletBlob blob) {
        File file = new File(".gitlet/" + blob.getHash());
        return file;
    }
}

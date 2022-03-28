package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/** GitletCommit class.
 * @author Qianfei(Ben) Hu
 * */
public class GitletCommit implements Serializable {
    /** The date saved in the commit message. */
    private Date date = null;
    /** The hashcode of the commit. */
    private String hash;
    /** The parent of the current commit. */
    private GitletCommit parent = null;
    /** The send Parent of the current commit. */
    private GitletCommit sendParent = null;
    /** The message to be committed. */
    private String message;
    /** A hash map to save the blobs. */
    private HashMap<String, GitletBlob> blobMap
            = new HashMap<String, GitletBlob>();

    /** Constructor of GitletCommit.
     *  @param p the parent of the commit.
     *  */
    public GitletCommit(GitletCommit p) {
        this.setParent(p);
    }
    /** Overload Constructor of GitletCommit.
     * * @param p the parent of the current commit.
     * * @param send the send parent of the current commit.
     * */
    public GitletCommit(GitletCommit p, GitletCommit send) {
        this.setParent(p);
        this.setSendParent(send);
    }

    /** Return the hashcode of the commit. */
    public String getHash() {
        return hash;
    }

    /** Return the message of the commit. */
    public String getMessage() {
        return message;
    }

    /** Return the hashmap of blobs of the commit. */
    public HashMap<String, GitletBlob> getBlobMap() {
        return this.blobMap;
    }
    /** Return GitletBlob from file named fileName.
     * * @param fileName the file name of the blob.
     * */
    public GitletBlob getBlob(String fileName) {
        return blobMap.get(fileName);
    }

    /** Put the file named fileName and holding content of blob
     * into the Blob Map.
     * @param blob the blob to be added.
     * @param fileName the file name of this blob.
     * */
    public void addBlob(String fileName, GitletBlob blob) {
        blobMap.put(fileName, blob);
    }

    /** Remove the blob named fileName from the Blob map.
     * @param fileName the file name of the blob to be removed from this commit.
     * */
    public void removeBlob(String fileName) {
        blobMap.remove(fileName);
    }

    /** Finish the commit with the String of message.
     * @param m the message of this commit.
     * */
    public void finish(String m) {
        message = m;
        this.date = new Date();
        this.hash = Utils.sha1(Utils.serialize(this));
    }

    /** Return the parent of the current commit. */
    public GitletCommit getParent() {
        return parent;
    }

    /** Send the parent to the current commit.
     * @param p the parent commit of this commit.
     * */
    public void setParent(GitletCommit p) {
        parent = p;
    }

    /** Return the date. */
    public Date getDate() {
        return date;
    }

    /** Return the sendParent. */
    public GitletCommit getSendParent() {
        return sendParent;
    }

    /** Set sendParent as the sendParent of the curr commit.
     * @param send the sendParent of this commit.
     * */
    public void setSendParent(GitletCommit send) {
        sendParent = send;
    }
}

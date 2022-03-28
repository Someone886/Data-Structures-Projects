package gitlet;

import java.io.File;
import java.io.Serializable;

/** GitletBlob class of the file.
 * @author Qianfei(Ben) Hu
 */

public class GitletBlob implements Serializable {
    /** Return the version of the file. */
    public int getVersion() {
        return version;
    }

    /** Constructor of GitletBlob.
     * @param file the name of the file to be inited.
     * @param ver the version of the file to be inited.
     */
    public GitletBlob(String file, int ver) {
        filename = file;
        version = ver;
        hashcode = Utils.sha1(Utils.readContentsAsString(new File(filename)));
    }

    /** Return the name of the file. */
    public String getFilename() {
        return filename;
    }

    /** Return the hash code of the file. */
    public String getHash() {
        return hashcode;
    }

    /** GETFILE method to get the FILE through hashcode.
     * @return the file of this blob.
     * */
    public File getFile() {
        return new File(".gitlet/" + hashcode);
    }

    /** The version of this blob. */
    private int version;
    /** The filename of this blob. */
    private String filename;
    /** The hashcode of this blob. */
    private String hashcode;
}

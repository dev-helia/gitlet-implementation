package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

/** Represents a gitlet commit object.
 * 方法: getID
 *
 *  @author Helia
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    // Basic info of this commitment.
    private String message; // Commit message.
    private Date timestamp; // Commit time. <- Date util class.

    // Commit structure.
    private String parent; // ✅Parent ID.
    private String parent2; // 第二个 parent（如果是 merge）
    private HashMap<String, String> blobs;// Blob.(和文件的映射关系)

    /* TODO: fill in the rest of this class. */

    /**
     * Constructor.
     *
     * @param message The message(description) of this commitment.
     * @param parent The parent commitment.
     */
    public Commit(String message, String parent, String parent2) {
        // Handle the paras传入参数.
        this.message = message;
        this.parent = parent;
        this.parent2 = parent2;
        // 初始化.
        this.timestamp = (parent == null)
                ?new Date(0)//✅new Date(0)：创建一个 时间戳 = 1970-01-01 00:00:00 UTC 的时间对象
                //这个操作就是 Git 里 "initial commit"（初始提交）使用 UNIX 时间 0 的方式
                :new Date(); //✅如果 parent 不是空（说明这个 commit 继承自某个 commit）
        //new Date()：使用当前系统时间，创建一个新的时间戳（即提交时的实际时间）
        this.blobs = new HashMap<String, String>();
    }

    public Commit(String message, String parent) {
        this(message, parent, null);
    }

    /**
     * Represent the hashcode of current commitment.
     * Method: Utils.sha1(objects).
     *
     * @return The SHA-1 value of this commitment.
     */
    public String getId() {
        return Utils.sha1(message, timestamp.toString(), parent, blobs.toString());
    }

    public void setBlobs(HashMap<String, String> blobs) {
        this.blobs = new HashMap<String, String>(blobs);
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getParent() {
        return parent;
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

}

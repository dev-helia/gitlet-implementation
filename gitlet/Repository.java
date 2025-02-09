package gitlet;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 * 方法:
 * 1. 初始化.init
 * 2. 文件加载缓存区.add
 * 3.commit 提交
 *
 *  @author Helia
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** 当前的所有git文件目录. */
    // 存储commits和blobs.
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    // 存储当前分支HEAD.
    public static final File REPO_DIR = join(GITLET_DIR, "repos");
    // 存储当前commit.
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /* TODO: fill in the rest of this class. */

    /**
     * 静态方法.
     * 后面才创目录.
     */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdirs();
        OBJECTS_DIR.mkdirs();
        REPO_DIR.mkdirs();
        // IO:
        /** 第一次commit. */
        Commit initialCommit = new Commit("Initial commit.", null);
        /** 得到第一次commit的ID */
        String commitId = initialCommit.getId();

        // 目录管理:
        /** 为第一个commit存储到objects,  创建文件 */
        File commitFile = Utils.join(OBJECTS_DIR, commitId);
        Utils.writeObject(commitFile, initialCommit);

        // 创建master分支 REFS:
        // 设置ref文件.
        // master 这个文件存的是 master 分支最新的 commit ID。

        File masterBranch = Utils.join(REPO_DIR, "master");
        Utils.writeContents(masterBranch, commitId);

        /** 设置HEAD */
        Utils.writeObject(HEAD, "refs/master");
    }

    /**
     * 路径: file -> objects:blob ->index
     * @param fileName 需要add的file.
     */
    public static void add(String fileName) {
        File file = new File(fileName);
        /** If the file not found. */
        if (!file.exists()) {
            System.out.println("File does not exist: " + fileName);
            return;
        }

        /** Read the content. */
        byte[] content = Utils.readContents(file);
        /** Get the blob Id */
        String blobId = Utils.sha1(content);

        /** Save the blob.(Object dir : .gitlet/objects/) */
        File blobFile = Utils.join(OBJECTS_DIR, blobId); // .gitlet/objects/blobId
        /** 如果这个文件不存在，就把 content 写进去 */
        if (!blobFile.exists()) {
            Utils.writeContents(blobFile, content);
        }

        /**  Update index(缓存区). */
        File indexFile = Utils.join(REPO_DIR, "index");
        HashMap<String, String> index = indexFile.exists()
                ? Utils.readObject(indexFile, HashMap.class)
                : new HashMap<String, String>();
        index.put(fileName, blobId);
        Utils.writeObject(indexFile, index);
    }

    public static void commit(String message) {
        /** edge case */
        if (message == null || message.isEmpty()) {
            System.out.println("Please provide a commit message.");
            return;
        }

        // Get current head;(Gitlet 需要知道当前在哪个分支，才能正确更新 commit)
        String branch = Utils.readContentsAsString(HEAD); // example: refs/master
        File branchFile = Utils.join(REPO_DIR, branch); //存着 master 分支最新的 commit ID
        // 如果你 checkout feature，HEAD 会变成 refs/feature
        // 然后 Gitlet 就会操作 feature 分支，而不是 master

        // Get current commit ID
        String parentCommitId = branchFile.exists()
                ? Utils.readContentsAsString(branchFile) : null;

        // Read index
        // Gitlet 需要知道有哪些文件被 add 过，才能提交正确的文件
        // 如果 index 为空，说明 commit 里没有任何文件，这时候应该报错
        // 1️⃣ 找到 index 文件（暂存区）
        File indexFile = Utils.join(REPO_DIR, "index");
        // 2️⃣ 如果 index 不存在，说明 `commit` 里没有文件，直接返回
        if (!indexFile.exists()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        // 3️⃣ 读取 index，获取所有 `add` 过的文件
        // 这里 index 变量是一个 HashMap，存着所有 add 过的文件
        HashMap<String, String> index = Utils.readObject(indexFile, HashMap.class);

        // 创建新的commit
        Commit newCommit = new Commit(message, parentCommitId);
        newCommit.setBlobs(index);

        // 计算commit ID, 并且存入.gitlet/objects
        String newCommitId = newCommit.getId(); // 计算 commit ID
        File commitFile = Utils.join(OBJECTS_DIR, newCommitId); // 找到存 commit 的文件路径
        Utils.writeObject(commitFile, newCommit); // 把 commit 存入 .gitlet/objects/

        // 更新master 让 master 分支指向最新 commit
        Utils.writeContents(branchFile, newCommitId);

        // 清空index (Commit之后的暂存区应该是空的)
        index.clear();
        Utils.writeObject(indexFile, index); // 把清空后的 index 存回 .gitlet/repos/index

        System.out.println("Committed successfully.");
    }

    /**
     * 当前分支的提交历史.
     */
    public static void log() {
        String commitId = Utils.readContentsAsString(HEAD); // 读取 HEAD，找到当前 commit ID
        while (commitId != null) { // 2️⃣ 沿着 parent 追溯所有 commit
            // 找到 .gitlet/objects/commitId 这个文件
            //用 readObject() 反序列化，把文件内容恢复成 Commit 对象
            // 返回 Commit，让 Gitlet 可以读取 commit 里的 message、parent 等信息
            Commit commit = Utils.readObject(new File(OBJECTS_DIR, commitId), Commit.class);
            System.out.println("===\nCommit" + commitId);
            System.out.println(commit.getMessage() + "\n");
            commitId = commit.getParent(); //一直追溯到 initial commit（parent = null）
        }
    }

    /**
     * 让 Gitlet 切换到指定的 commit，并更新 HEAD
     * 不会创建新 commit，只是把 HEAD 指向旧的 commit
     * 让 log、checkout 之类的操作能找到正确的 commit
     *
     * @param commitId 需要切换到的ID
     */
    public static void checkout(String commitId) {
        // 1️⃣ 先检查 commit 是否存在
        File commitFile = Utils.join(OBJECTS_DIR, commitId);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        // 2️⃣ 更新 HEAD，指向这个 commit
        Utils.writeContents(HEAD, commitId);
    }

    /**
     * 创建新分支.
     *
     * @param branchName
     */
    public static void branch(String branchName) {
        // 1️⃣ 找到新分支的路径
        File branchFile = Utils.join(REPO_DIR, branchName);
        // 2️⃣ 如果分支已经存在，就报错
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        // 3️⃣ 读取当前 commit ID
        String commitId = Utils.readContentsAsString(HEAD);
        // 4️⃣ ⚠️ 这里有 BUG！它错误地把 commitId 写进 HEAD，而不是新分支
        Utils.writeContents(branchFile, commitId);
    }

    /**
     *
     * @param commitId 要退回的id.
     */
    public static void reset(String commitId) {
        File commitFile = Utils.join(OBJECTS_DIR, commitId);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        String branch = Utils.readContentsAsString(HEAD); // HEAD 里存的是当前分支（比如 "refs/master"）读取当前的branch.
        File branchFile = Utils.join(REPO_DIR, branch); // 拿到branch file 里面是commitID.
        Utils.writeContents(branchFile, commitId); //让 master 分支指向新的 commitId
        //这样 master 以后就从 commitId 开始，而不是之前的 commit 了！

        Utils.writeObject(HEAD, commitId); // 直接把commitid写在head里面
    }

    /**
     * 删掉index暂存区里面add的某个文件.
     *
     * @param fileName 某个文件.
     */
    public static void rm(String fileName) {
        // 找到暂存区
        File indexFile = Utils.join(REPO_DIR, "index");

        // 读取index
        if (!indexFile.exists()) {
            System.out.println("No index file exists.");
            return;
        }

        HashMap<String, String> index = Utils.readObject(indexFile, HashMap.class);

        if (!index.containsKey(fileName)) {
            System.out.println("No commit with that id exists.");
            return;
        }

        index.remove(fileName);
        Utils.writeObject(indexFile, index);
    }

    /**
     * 删一个branch.
     * @param branchName
     */
    public static void rmBranch(String branchName) {
        File branchFile = Utils.join(REPO_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("No branch with that id exists.");
            return;
        }

        String currentBranch = Utils.readContentsAsString(HEAD);
        if (currentBranch.equals("refs/" + branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        branchFile.delete();
    }
}

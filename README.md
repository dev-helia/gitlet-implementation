# 项目概述

这是一个实现版本控制系统的项目（类似 Git 的简化版）。项目要求我们通过 Java 编程，创建一个支持基本 Git 功能的版本控制系统，称为 **Gitlet**。以下是对关键部分的详细解析：

Gitlet 是一个简化版的版本控制工具，支持以下核心功能：

1. **保存文件目录的快照**（commit）。
2. **恢复特定版本的文件或目录**（checkout）。
3. **查看历史记录**（log）。
4. **管理分支**（branch）。
5. **合并分支**（merge）。

### **为什么需要版本控制？**

版本控制系统的主要目的是在开发复杂项目时帮助我们保存不同的版本，允许回滚到之前的状态，避免丢失重要修改。同时，多个开发者也可以通过版本控制系统协作开发。

在 Gitlet 中，所有提交的文件和历史会保存在一个名为 **`.gitlet`** 的目录中。

---

## **Gitlet 的内部结构**

在 Gitlet 中，我们使用以下三种主要对象来管理版本控制的核心数据：

### **1. Blob（文件内容）**

- 每个文件内容保存为一个独立的 blob。
- 如果一个文件内容被修改，就会生成一个新的 blob。

### **2. Commit（提交）**

- 每次提交时，Gitlet 会保存整个文件目录的快照。
- 每个 commit 包含：
    - 提交的时间戳和日志信息。
    - 指向其父 commit 的引用。
    - 指向每个 blob 的映射（文件名到 blob 的映射）。

### **3. Branch（分支）**

- 分支是指向某个 commit 的指针。
- HEAD 是一个指向当前活跃分支（branch）的指针。

---

## **Gitlet 的功能及实现**

Gitlet 支持以下命令。每个命令实现时需要注意细节和失败情况。

### **1. 初始化（init）**

- **命令**：`java gitlet.Main init`
- **功能**：在当前目录中创建一个 `.gitlet` 文件夹，初始化一个版本控制系统。
- **初始状态**：
    - 包含一个空文件快照的初始 commit（`initial commit`）。
    - 默认分支名为 `master`，HEAD 指向 `master`。

#### **失败情况**

- 如果 `.gitlet` 已存在，打印错误信息：`A Gitlet version-control system already exists in the current directory.`

---

### **2. 添加文件（add）**

- **命令**：`java gitlet.Main add [file name]`
- **功能**：将文件添加到暂存区（staging area）。
- **行为**：
    - 如果文件未被修改或已在当前提交中，则从暂存区移除。
    - 修改后的文件会覆盖暂存区的旧版本。

#### **失败情况**

- 如果文件不存在，打印错误信息：`File does not exist.`

---

### **3. 提交（commit）**

- **命令**：`java gitlet.Main commit [message]`
- **功能**：
    - 保存当前暂存区和上次提交的文件快照。
    - 创建一个新的 commit 节点。

#### **注意**

- 如果没有暂存的文件，打印错误：`No changes added to the commit.`

---

### **4. 查看日志（log）**

- **命令**：`java gitlet.Main log`
- **功能**：从 HEAD 开始，向后打印每个 commit 的历史信息，包括：
    - commit id（SHA-1 哈希值）
    - 时间戳
    - 提交信息

#### **示例输出**

```
===
commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
Date: Thu Nov 9 20:00:05 2017 -0800
Added new feature.
```

---

### **5. 检出文件（checkout）**

**支持以下三种用法**：

6. **检出当前 commit 的文件**：
    
    - **命令**：`java gitlet.Main checkout -- [file name]`
    - **功能**：从当前 commit 中获取文件，覆盖工作目录中的同名文件。
7. **检出指定 commit 的文件**：
    
    - **命令**：`java gitlet.Main checkout [commit id] -- [file name]`
    - **功能**：从指定的 commit 中获取文件。
8. **切换分支**：
    
    - **命令**：`java gitlet.Main checkout [branch name]`
    - **功能**：切换到指定分支，并更新工作目录。

---

### **6. 分支（branch）**

- **命令**：`java gitlet.Main branch [branch name]`
- **功能**：
    - 创建一个新的分支。
    - 分支是一个指向当前 commit 的指针。

---

### **7. 合并（merge）**

- **命令**：`java gitlet.Main merge [branch name]`
- **功能**：
    - 将指定分支的修改合并到当前分支。

---

## **实现提示**

### **1. 数据存储**

- 所有的文件内容（blob）、提交记录（commit）等都需要存储在 `.gitlet` 目录中。
- 使用 Java 的序列化（Serialization）将对象保存为字节流。

### **2. SHA-1 哈希值**

- 使用 SHA-1 生成唯一的哈希值作为 blob 和 commit 的标识符。
- 示例：
    
    ```java
    String sha1 = Utils.sha1(fileContents);
    ```
    

---

## **测试和调试**

### **1. 测试工具**

- 提供了 `tester.py` 脚本，可以自动运行测试用例。
- 命令：
    
    ```bash
    make check
    python3 tester.py --verbose FILE.in
    ```
    

### **2. 集成测试**

- 测试用例使用 `.in` 文件定义输入和期望的输出。
- 示例 `.in` 文件内容：
    
    ```txt
    > init
    <<<
    
    + wug.txt wug.txt
    > add wug.txt
    <<<
    
    > commit "Added wug"
    <<<
    ```
    

## 实现功能

| 功能     | 所在类               | 作用                            | **优先级** | **状态** |
| ------ | ----------------- | ----------------------------- | ------- | ------ |
| `init` | `Repository.java` | 初始化 Gitlet 版本库                | 🟢 1    | 🚧 还没做 |
| `add`  | `Repository.java` | 添加文件到暂存区                      | 🟢 2    | 🚧 还没做 |
|        |                   | **实现 `checkout`，回到旧的 commit** | 🟢 3    | 🚧 还没做 |
|        |                   | **实现 `branch`，创建新分支**         | 🟡 4    | 🚧 还没做 |
|        |                   | **实现 `merge`，合并分支**           | 🔴 5    | 🚧 还没做 |


---
# 文件和数据结构
## 目录结构

Gitlet 在 `.gitlet/` 目录下存储所有数据，主要有：

```
.gitlet/
│── objects/      # 存储 commit 和文件 blob（快照）
│── refs/         # 存储分支 HEAD 指针
│── HEAD          # 指向当前所在的 commit
│── index         # 暂存区（staging area）
```

- `objects/`：存储 **commit** 和 **文件的 blob**，使用 SHA-1 哈希命名。
- `refs/`：存储 **分支（branch）** 的 HEAD 位置。
- `HEAD`：存储 **当前 commit** 的 SHA-1 哈希值 或者 **当前分支的字段**。
- `index`：存储 **暂存区**，记录 `add` 过的文件。

|**存储位置**|**存的内容**|
|---|---|
|`.gitlet/objects/`|**所有 `commit` 和 `blob`**（文件快照）|
|`.gitlet/HEAD`|**指向当前 commit ID**|
|`commit` 结构|**指向 `parent` 和 `blob`**|
|`blob` 结构|**文件内容的 SHA-1 快照**|


具体举例:
```bash
.gitlet/
│── objects/                          # 存储 commit 和 blob（文件快照）
│   ├── 3a/5c6c8...                    # a.txt 第一次提交的 blob
│   │   ├── 内容：hello world          # 🔹 具体内容存储在这个 blob 里
│   ├── e7/9e32d...                    # a.txt 第二次提交的 blob
│   │   ├── 内容：new content          # 🔹 第二次提交时 a.txt 的快照
│   ├── 5f/4a3b2...                    # "First commit" 的 commit 对象
│   │   ├── 类型：commit               # 🔹 这个对象是一个 commit
│   │   ├── parent: null               # 🔹 没有父 commit（初始提交）
│   │   ├── timestamp: 2025-02-08 12:00:00
│   │   ├── message: "First commit"    # 🔹 commit 提交的说明信息
│   │   ├── files: { "a.txt" -> 3a5c6c8... }  # 🔹 关联的文件和对应 blob
│   ├── a8/b93e1...                    # "Second commit" 的 commit 对象
│   │   ├── 类型：commit               # 🔹 这个对象也是一个 commit
│   │   ├── parent: 5f4a3b2...         # 🔹 继承自 "First commit"
│   │   ├── timestamp: 2025-02-08 12:05:00
│   │   ├── message: "Second commit"   # 🔹 commit 提交的说明信息
│   │   ├── files: { "a.txt" -> e79e32d... }  # 🔹 更新了 a.txt 的内容
│
│── refs/                              # 存储分支信息
│   ├── heads/                         # 🔹 记录各个分支的 commit ID
│   │   ├── master                     # 🔹 master 分支的 commit ID
│   │   │   ├── a8b93e1...             # 🔹 目前 master 指向 "Second commit"
│
│── HEAD                               # 指向当前分支（master）
│   ├── 内容：ref: refs/heads/master   # 🔹 说明当前在 master 分支
│
│── index                              # 记录暂存区信息
│   ├── 内容：                         # 🔹 当前暂存的文件
│   │   ├── a.txt -> e79e32d...        # 🔹 a.txt 已暂存，指向最新 blob

```
## 数据结构
> 强调色标注核心类.
- ==Commit类==:代表一次提交（包含提交信息、时间戳、父 commit 指针、文件快照.）
	- 实现成员变量.
	- 实现构造函数.
	- 实现getID方法.
- Dumpable接口:打印有用信息.
- DumpObj类:debug的类.
- GitletException:产生error.
- ==Main类==:解析命令行参数,调用Repository里面的方法, 实现功能.
- MakeFile:不知道是什么.
- ==Repository类==:管理 `.gitlet/` 目录，处理 `add`、`commit`、`checkout` 等操作
	- 实现成员变量.
	- 创建管理系统-目录.
	- 实现init初始化方法.
	- 实现add加载文件到暂存区方法.
	- 实现commit提交方法.
- sentinel:为啥是空的.
- Utils类:一些工具类.(**SHA-1计算**和**文件IO**.)
	- `writeObject()` 存java对象:`commit` ,`index`（HashMap）
	- `writeContents()`存纯文本:`commit ID`、`branch`
#### Utils详解

|**方法**|**作用**|**示例**|
|---|---|---|
|`sha1()`|计算 SHA-1 哈希|`Utils.sha1("hello")`|
|`readContents()`|读取文件（字节数组）|`Utils.readContents(file)`|
|`readContentsAsString()`|读取文件（字符串）|`Utils.readContentsAsString(file)`|
|`writeContents()`|写入文件（覆盖）|`Utils.writeContents(file, "new data")`|
|`writeObject()`|序列化对象|`Utils.writeObject(file, commit)`|
|`readObject()`|反序列化对象|`Commit c = Utils.readObject(file, Commit.class)`|
|`join()`|拼接路径|`File f = Utils.join(".gitlet", "objects", "123456")`|


>[!note]
>自己的踩坑点:
>- Git 里说的“指针”其实就是文件里的一串文本（commit ID）
>- **`HEAD` 有两种不同的情况**
>	1. **HEAD 指向一个分支**（例如 `"refs/master"`）
>	2. **HEAD 直接存 `commitId`**（“detached HEAD” 模式）




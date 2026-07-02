# 本地项目推送到 GitHub 简明教程

这份文档适合新手照着做，目标是把你当前的本地项目安全地推送到 GitHub 仓库中。下面的步骤按实际常见流程编写，尽量通俗易懂。

## 一、准备工作

在开始之前，先确认你已经准备好下面几样东西：

1. 已安装 Git
   - 可以在终端输入 `git --version` 检查。
   - 如果能看到版本号，说明 Git 已安装成功。

2. 已有 GitHub 账号
   - 没有账号先去 GitHub 注册。

3. 已创建远程仓库
   - 在 GitHub 上新建一个仓库。
   - 仓库名可以和本地项目名一致，也可以不同。
   - 如果是首次推送，建议仓库先保持空仓库，不要勾选自动创建 README、.gitignore 或 License，避免冲突。

4. 知道本地项目所在目录
   - 例如你的项目根目录里通常会有 `README.md`、`app/`、`src/`、`build.gradle` 等文件。
   - 请确保你操作的是项目根目录，不要进错文件夹。

## 二、查看 git 状态

进入项目根目录后，先看看当前状态：

```bash
git status
```

常见结果说明：

- `nothing to commit, working tree clean`
  - 说明当前没有未提交改动。
- `Untracked files`
  - 说明有新文件还没有加入版本管理。
- `Changes not staged for commit`
  - 说明文件已修改，但还没有加入暂存区。

如果你想看看具体改了什么，可以用：

```bash
git diff
```

如果项目刚下载下来，还没有初始化 git，可以先执行：

```bash
git init
```

## 三、配置 remote

remote 的意思就是“远程仓库地址”，通常指 GitHub 上的仓库。

先查看当前有没有配置远程地址：

```bash
git remote -v
```

如果没有任何输出，说明还没配置。可以添加远程仓库：

```bash
git remote add origin https://github.com/你的用户名/你的仓库名.git
```

如果已经有 `origin`，但地址不对，可以先删除再重新添加：

```bash
git remote remove origin
git remote add origin https://github.com/你的用户名/你的仓库名.git
```

你也可以检查是否配置成功：

```bash
git remote -v
```

如果显示的是 GitHub 地址，就说明成功了。

## 四、首次提交

首次提交一般分为 3 步：添加、提交、推送。

### 1. 把文件加入暂存区

```bash
git add .
```

这表示把当前目录下所有改动都加入暂存区。

如果你只想添加某几个文件，也可以写具体文件名：

```bash
git add README.md
```

### 2. 提交到本地仓库

```bash
git commit -m "第一次提交"
```

提交信息建议写清楚一点，比如：

- `首次提交项目`
- `添加基础结构`
- `完善 README`

### 3. 指定主分支并推送

有些新仓库默认分支叫 `main`，有些旧项目可能叫 `master`。如果你不确定，可以先查看当前分支：

```bash
git branch
```

如果当前分支是 `main`，可执行：

```bash
git push -u origin main
```

如果当前分支是 `master`，可执行：

```bash
git push -u origin master
```

`-u` 的作用是建立上游关联，后面你再推送时就可以直接用 `git push`。

## 五、后续提交

以后只要你改了代码或文档，通常按下面流程来：

### 1. 查看改动

```bash
git status
```

### 2. 添加改动

```bash
git add .
```

### 3. 提交改动

```bash
git commit -m "更新说明"
```

### 4. 推送到 GitHub

```bash
git push
```

如果你还没有设置上游分支，第一次推送时才需要加 `-u origin 分支名`。

## 六、push 到 GitHub 的完整常用流程

如果你想记最常用的一套命令，可以直接照着下面做：

```bash
git status
git add .
git commit -m "更新内容"
git push
```

如果是第一次推送，则用：

```bash
git status
git add .
git commit -m "首次提交"
git push -u origin main
```

## 七、结合当前项目的常见场景

像你当前这种本地项目，常见情况有下面几种：

### 场景 1：只改了文档

例如你修改了 `README.md`、说明文档、使用指南等。

建议这样做：

```bash
git status
git add README.md
git commit -m "更新项目文档"
git push
```

### 场景 2：改了代码后想同步到 GitHub

例如你修改了应用逻辑、界面、配置文件等。

建议这样做：

```bash
git status
git add .
git commit -m "修复问题并优化功能"
git push
```

### 场景 3：新项目第一次上传

如果这是你第一次把整个项目放到 GitHub，常见流程是：

```bash
git init
git add .
git commit -m "首次提交项目"
git remote add origin https://github.com/你的用户名/你的仓库名.git
git push -u origin main
```

## 八、常见错误与解决办法

### 1. `fatal: not a git repository`

**原因**：你不在 git 项目目录里。

**解决**：
- 确认当前路径是项目根目录。
- 如果项目还没初始化，先执行 `git init`。

### 2. `fatal: remote origin already exists`

**原因**：已经配置过 `origin`。

**解决**：

```bash
git remote remove origin
git remote add origin https://github.com/你的用户名/你的仓库名.git
```

### 3. `src refspec main does not match any`

**原因**：本地没有 `main` 分支，或者还没有提交。

**解决**：
- 先执行 `git commit -m "首次提交"`
- 再查看分支名：`git branch`
- 按实际分支名推送，比如 `git push -u origin master`

### 4. `rejected` 或 `failed to push some refs`

**原因**：远程仓库已有提交，本地和远程历史不一致。

**解决**：
- 先拉取远程内容再推送：

```bash
git pull --rebase origin main
git push
```

- 如果远程分支是 `master`，把 `main` 换成 `master`。

### 5. 需要输入账号密码但失败

**原因**：GitHub 现在通常不再支持直接使用账号密码推送。

**解决**：
- 使用 GitHub Personal Access Token 代替密码。
- 或者配置 SSH 方式推送。

### 6. 中文文件名或路径显示乱码

**原因**：终端编码问题。

**解决**：
- 尽量使用支持 UTF-8 的终端。
- Windows 下可尝试切换编码环境。

## 九、推荐你记住的最小命令集

如果你是新手，只记住下面几条也够用了：

```bash
git status
git add .
git commit -m "说明本次修改"
git push
```

第一次推送时再加上远程仓库地址和 `-u`：

```bash
git remote add origin https://github.com/你的用户名/你的仓库名.git
git push -u origin main
```

## 十、最后检查

推送完成后，去 GitHub 仓库页面刷新一下，如果能看到最新代码和文档，说明已经成功。

如果没看到，先检查：

- 你是否真的执行了 `git commit`
- 远程仓库地址是否正确
- 分支名是否一致
- 是否遇到报错但没有处理

---

如果你愿意，可以把这个流程当成固定习惯：先看状态，再添加、提交、推送。多做几次就会很熟练。

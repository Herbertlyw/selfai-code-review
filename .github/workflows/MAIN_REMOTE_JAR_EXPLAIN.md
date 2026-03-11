# GitHub Actions 工作流 - main-remote-jar.yml 详细解析

## 工作流概述

`main-remote-jar.yml` 是一个 GitHub Actions 工作流配置文件，用于在代码推送到 `master` 分支或创建 pull request 到 `master` 分支时，自动执行代码审查流程。该工作流使用预构建的 SDK JAR 包来执行代码审查，并通过环境变量传递必要的配置信息。

## 详细代码解析

### 1. 工作流名称与触发条件

```yaml
name: Build and Run OpenAiCodeReview By Main Maven Jar

on:
  push:
    branches:
      - master
  pull_request:
    branches:
      - master
```

**解析**：
- `name`：工作流的名称，用于在 GitHub Actions 界面中标识该工作流
- `on`：定义工作流的触发条件
  - `push`：当代码推送到指定分支时触发
  - `pull_request`：当创建或更新 pull request 到指定分支时触发
  - `branches: [master]`：只对 `master` 分支的操作触发工作流

**作用**：确保代码审查在代码合并到主分支前或推送到主分支后自动执行，保证代码质量。

### 2. 工作定义

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
```

**解析**：
- `jobs`：定义工作流中的任务
- `build`：任务名称
- `runs-on: ubuntu-latest`：指定任务在最新版本的 Ubuntu 虚拟机上运行

**作用**：创建一个名为 `build` 的任务，并指定其运行环境。

### 3. 任务步骤

#### 3.1 检出代码库

```yaml
- name: Checkout repository
  uses: actions/checkout@v2
  with:
    fetch-depth: 2
```

**解析**：
- `name`：步骤名称
- `uses: actions/checkout@v2`：使用 GitHub 官方提供的 `checkout` 动作，版本为 v2
- `with: fetch-depth: 2`：设置获取的代码深度为 2，即获取最近的 2 次提交

**作用**：将代码库检出到 GitHub Actions 运行环境中，`fetch-depth: 2` 确保能够获取到前一次提交的代码，以便计算代码差异。

#### 3.2 设置 JDK 环境

```yaml
- name: Set up JDK 11
  uses: actions/setup-java@v2
  with:
    distribution: 'adopt'
    java-version: '11'
```

**解析**：
- `name`：步骤名称
- `uses: actions/setup-java@v2`：使用 GitHub 官方提供的 `setup-java` 动作，版本为 v2
- `with`：设置动作参数
  - `distribution: 'adopt'`：使用 AdoptOpenJDK 发行版
  - `java-version: '11'`：设置 JDK 版本为 11

**作用**：配置运行环境的 Java 版本，确保能够运行 Java 应用程序。

#### 3.3 创建 libs 目录

```yaml
- name: Create libs directory
  run: mkdir -p ./libs
```

**解析**：
- `name`：步骤名称
- `run: mkdir -p ./libs`：执行 shell 命令，创建 `./libs` 目录，`-p` 参数确保即使父目录不存在也能创建

**作用**：创建一个目录用于存放下载的 SDK JAR 包。

#### 3.4 下载 SDK JAR 包

```yaml
- name: Download openai-code-review-sdk JAR
  run: wget -O ./libs/openai-code-review-sdk-1.0.jar https://github.com/Herbertlyw/openai-code-review/releases/download/v1.0/openai-code-review-sdk-1.0.jar
```

**解析**：
- `name`：步骤名称
- `run: wget -O ./libs/openai-code-review-sdk-1.0.jar https://github.com/Herbertlyw/openai-code-review/releases/download/v1.0/openai-code-review-sdk-1.0.jar`：使用 `wget` 命令下载 SDK JAR 包
  - `-O`：指定输出文件路径
  - 后面是 JAR 包的下载地址

**作用**：下载预构建的 SDK JAR 包，用于执行代码审查。

#### 3.5 获取仓库名称

```yaml
- name: Get repository name
  id: repo-name
  run: echo "REPO_NAME=${GITHUB_REPOSITORY##*/}" >> $GITHUB_ENV
```

**解析**：
- `name`：步骤名称
- `id: repo-name`：设置步骤 ID，用于后续引用
- `run: echo "REPO_NAME=${GITHUB_REPOSITORY##*/}" >> $GITHUB_ENV`：执行 shell 命令
  - `${GITHUB_REPOSITORY##*/}`：从 `GITHUB_REPOSITORY` 环境变量中提取仓库名称（去掉所有者部分）
  - `>> $GITHUB_ENV`：将结果追加到 `GITHUB_ENV` 文件，使其成为环境变量

**作用**：获取当前仓库的名称，用于后续的代码审查过程。

#### 3.6 获取分支名称

```yaml
- name: Get branch name
  id: branch-name
  run: echo "BRANCH_NAME=${GITHUB_REF#refs/heads/}" >> $GITHUB_ENV
```

**解析**：
- `name`：步骤名称
- `id: branch-name`：设置步骤 ID
- `run: echo "BRANCH_NAME=${GITHUB_REF#refs/heads/}" >> $GITHUB_ENV`：执行 shell 命令
  - `${GITHUB_REF#refs/heads/}`：从 `GITHUB_REF` 环境变量中提取分支名称（去掉 `refs/heads/` 前缀）
  - `>> $GITHUB_ENV`：将结果追加到 `GITHUB_ENV` 文件

**作用**：获取当前操作的分支名称，用于后续的代码审查过程。

#### 3.7 获取提交作者

```yaml
- name: Get commit author
  id: commit-author
  run: echo "COMMIT_AUTHOR=$(git log -1 --pretty=format:'%an <%ae>')" >> $GITHUB_ENV
```

**解析**：
- `name`：步骤名称
- `id: commit-author`：设置步骤 ID
- `run: echo "COMMIT_AUTHOR=$(git log -1 --pretty=format:'%an <%ae>')" >> $GITHUB_ENV`：执行 shell 命令
  - `git log -1 --pretty=format:'%an <%ae>'`：获取最近一次提交的作者信息，格式为 "作者名 <邮箱>"
  - `>> $GITHUB_ENV`：将结果追加到 `GITHUB_ENV` 文件

**作用**：获取当前提交的作者信息，用于后续的代码审查过程和通知。

#### 3.8 获取提交消息

```yaml
- name: Get commit message
  id: commit-message
  run: echo "COMMIT_MESSAGE=$(git log -1 --pretty=format:'%s')" >> $GITHUB_ENV
```

**解析**：
- `name`：步骤名称
- `id: commit-message`：设置步骤 ID
- `run: echo "COMMIT_MESSAGE=$(git log -1 --pretty=format:'%s')" >> $GITHUB_ENV`：执行 shell 命令
  - `git log -1 --pretty=format:'%s'`：获取最近一次提交的提交消息
  - `>> $GITHUB_ENV`：将结果追加到 `GITHUB_ENV` 文件

**作用**：获取当前提交的提交消息，用于后续的代码审查过程和通知。

#### 3.9 打印信息

```yaml
- name: Print repository, branch name, commit author, and commit message
  run: |
    echo "Repository name is ${{ env.REPO_NAME }}"
    echo "Branch name is ${{ env.BRANCH_NAME }}"
    echo "Commit author is ${{ env.COMMIT_AUTHOR }}"
    echo "Commit message is ${{ env.COMMIT_MESSAGE }}"
```

**解析**：
- `name`：步骤名称
- `run: |`：执行多行 shell 命令
- 打印之前获取的仓库名称、分支名称、提交作者和提交消息

**作用**：在日志中显示获取的信息，便于调试和查看工作流执行情况。

#### 3.10 运行代码审查

```yaml
- name: Run Code Review
  run: java -jar ./libs/openai-code-review-sdk-1.0.jar
  env:
    GITHUB_REVIEW_LOG_URI: ${{ secrets.CODE_REVIEW_LOG_URI }}
    GITHUB_TOKEN: ${{ secrets.CODE_TOKEN }}
    COMMIT_PROJECT: ${{ env.REPO_NAME }}
    COMMIT_BRANCH: ${{ env.BRANCH_NAME }}
    COMMIT_AUTHOR: ${{ env.COMMIT_AUTHOR }}
    COMMIT_MESSAGE: ${{ env.COMMIT_MESSAGE }}
    # 微信配置 「https://mp.weixin.qq.com/debug/cgi-bin/sandboxinfo?action=showinfo&t=sandbox/index」
    WEIXIN_APPID: ${{ secrets.WEIXIN_APPID }}
    WEIXIN_SECRET: ${{ secrets.WEIXIN_SECRET }}
    WEIXIN_TOUSER: ${{ secrets.WEIXIN_TOUSER }}
    WEIXIN_TEMPLATE_ID: ${{ secrets.WEIXIN_TEMPLATE_ID }}
    # OpenAi - ChatGLM 配置「https://open.bigmodel.cn/api/paas/v4/chat/completions」、「https://open.bigmodel.cn/usercenter/apikeys」
    CHATGLM_APIHOST: ${{ secrets.CHATGLM_APIHOST }}
    CHATGLM_APIKEYSECRET: ${{ secrets.CHATGLM_APIKEYSECRET }}
```

**解析**：
- `name`：步骤名称
- `run: java -jar ./libs/openai-code-review-sdk-1.0.jar`：执行 Java 命令，运行 SDK JAR 包
- `env`：设置环境变量
  - `GITHUB_REVIEW_LOG_URI`：代码审查结果存储的 GitHub 仓库地址，从 GitHub Secrets 获取
  - `GITHUB_TOKEN`：GitHub 访问令牌，从 GitHub Secrets 获取
  - `COMMIT_PROJECT`：项目名称，使用之前获取的环境变量
  - `COMMIT_BRANCH`：分支名称，使用之前获取的环境变量
  - `COMMIT_AUTHOR`：提交作者，使用之前获取的环境变量
  - `COMMIT_MESSAGE`：提交消息，使用之前获取的环境变量
  - 微信配置：从 GitHub Secrets 获取微信公众号相关信息
  - ChatGLM 配置：从 GitHub Secrets 获取 ChatGLM API 相关信息

**作用**：运行代码审查工具，使用环境变量传递必要的配置信息，执行完整的代码审查流程。

## 工作流执行流程

1. **触发**：当代码推送到 `master` 分支或创建 pull request 到 `master` 分支时，工作流被触发
2. **准备环境**：
   - 检出代码库
   - 设置 JDK 11 环境
   - 创建 libs 目录并下载 SDK JAR 包
3. **收集信息**：
   - 获取仓库名称
   - 获取分支名称
   - 获取提交作者
   - 获取提交消息
   - 打印收集到的信息
4. **执行代码审查**：
   - 运行 SDK JAR 包
   - 通过环境变量传递配置信息
   - 执行代码差异获取、AI 审查、结果存储和微信通知

## 关键技术点

### 1. GitHub Secrets 的使用

工作流使用了多个 GitHub Secrets 来存储敏感信息，如：
- `CODE_REVIEW_LOG_URI`：代码审查结果存储的仓库地址
- `CODE_TOKEN`：GitHub 访问令牌
- 微信公众号相关信息
- ChatGLM API 相关信息

**作用**：避免在配置文件中明文存储敏感信息，提高安全性。

### 2. 环境变量的设置与使用

工作流通过以下方式设置和使用环境变量：
- 使用 `echo "KEY=VALUE" >> $GITHUB_ENV` 设置环境变量
- 使用 `${{ env.VARIABLE_NAME }}` 引用环境变量
- 使用 `${{ secrets.SECRET_NAME }}` 引用 GitHub Secrets

**作用**：在工作流的不同步骤之间传递信息，确保代码审查工具能够获取到必要的配置。

### 3. Git 命令的使用

工作流使用了以下 Git 命令：
- `git log -1 --pretty=format:'%an <%ae>'`：获取提交作者信息
- `git log -1 --pretty=format:'%s'`：获取提交消息

**作用**：收集代码提交的相关信息，用于代码审查和通知。

### 4. wget 命令的使用

工作流使用 `wget` 命令下载预构建的 SDK JAR 包：

```bash
wget -O ./libs/openai-code-review-sdk-1.0.jar https://github.com/Herbertlyw/openai-code-review/releases/download/v1.0/openai-code-review-sdk-1.0.jar
```

**作用**：获取预构建的 SDK JAR 包，避免在每次运行时重新构建，提高工作流执行效率。

## 总结

`main-remote-jar.yml` 是一个设计合理的 GitHub Actions 工作流配置文件，它通过以下步骤实现了自动代码审查：

1. **触发条件**：在代码推送到 `master` 分支或创建 pull request 到 `master` 分支时触发
2. **环境准备**：设置 JDK 环境，下载 SDK JAR 包
3. **信息收集**：获取仓库、分支、作者和提交消息信息
4. **执行审查**：运行 SDK JAR 包，执行完整的代码审查流程
5. **配置管理**：使用 GitHub Secrets 存储敏感信息，通过环境变量传递配置

这种设计使得代码审查过程自动化，确保了代码质量，同时也提高了开发效率。通过预构建 SDK JAR 包的方式，减少了工作流的执行时间，使得整个流程更加高效。
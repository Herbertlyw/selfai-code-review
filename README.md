# selfai-code-review

一个基于 AI 的代码审查工具，使用 ChatGLM 进行智能代码评审，并通过微信通知发送评审结果。

## 功能特性

- 🤖 **AI 代码审查**：使用 ChatGLM 模型进行智能代码评审
- 📊 **自动分析**：自动获取代码差异并进行审查
- 📝 **结果存储**：将审查结果存储到 GitHub 仓库
- 💬 **微信通知**：通过微信模板消息发送审查结果
- 🔄 **自动化**：集成 GitHub Actions，实现代码推送时自动审查

## 工作原理

1. **代码差异获取**：通过 Git 命令获取代码变更
2. **AI 审查**：将代码差异发送给 ChatGLM 模型进行分析
3. **结果存储**：将审查结果提交到指定的 GitHub 仓库
4. **通知发送**：通过微信模板消息发送审查结果链接

## 安装和使用

### 方法一：GitHub Actions 自动运行（推荐）

1. **Fork 本仓库**：将仓库复制到你的 GitHub 账号

2. **配置 GitHub Secrets**：在仓库的 `Settings > Secrets and variables > Actions` 中添加以下 secrets：

   | Secret 名称 | 描述 | 示例 |
   |------------|------|------|
   | `CODE_REVIEW_LOG_URI` | 代码审查结果存储的 GitHub 仓库地址 | `https://github.com/your-username/code-review-logs` |
   | `CODE_TOKEN` | GitHub 访问令牌（需要 repo 权限） | `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx` |
   | `WEIXIN_APPID` | 微信公众号 AppID | `wx1234567890abcdef` |
   | `WEIXIN_SECRET` | 微信公众号 AppSecret | `1234567890abcdef1234567890abcdef` |
   | `WEIXIN_TOUSER` | 接收通知的用户 OpenID | `o1234567890abcdef1234567890abcdef` |
   | `WEIXIN_TEMPLATE_ID` | 微信模板消息 ID | `1234567890abcdef1234567890abcdef` |
   | `CHATGLM_APIHOST` | ChatGLM API 地址 | `https://open.bigmodel.cn/api/paas/v4/chat/completions` |
   | `CHATGLM_APIKEYSECRET` | ChatGLM API 密钥 | `1234567890abcdef1234567890abcdef` |

3. **配置工作流**：修改 `.github/workflows/main-remote-jar.yml` 文件，将 JAR 包下载地址修改为你的发布地址

4. **推送代码**：当你向 `master` 分支推送代码或创建 PR 时，GitHub Actions 会自动运行代码审查

### 方法二：本地运行

1. **克隆仓库**：
   ```bash
   git clone https://github.com/Herbertlyw/selfai-code-review.git
   cd selfai-code-review
   ```

2. **构建项目**：
   ```bash
   cd selfai-code-review-sdk
   mvn clean package
   ```

3. **设置环境变量**：
   ```bash
   export GITHUB_REVIEW_LOG_URI="https://github.com/your-username/code-review-logs"
   export GITHUB_TOKEN="ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
   export COMMIT_PROJECT="your-project-name"
   export COMMIT_BRANCH="master"
   export COMMIT_AUTHOR="Your Name <your-email@example.com>"
   export COMMIT_MESSAGE="Your commit message"
   export WEIXIN_APPID="wx1234567890abcdef"
   export WEIXIN_SECRET="1234567890abcdef1234567890abcdef"
   export WEIXIN_TOUSER="o1234567890abcdef1234567890abcdef"
   export WEIXIN_TEMPLATE_ID="1234567890abcdef1234567890abcdef"
   export CHATGLM_APIHOST="https://open.bigmodel.cn/api/paas/v4/chat/completions"
   export CHATGLM_APIKEYSECRET="1234567890abcdef1234567890abcdef"
   ```

4. **运行审查**：
   ```bash
   java -jar target/selfai-code-review-sdk-1.0.jar
   ```

## 配置说明

### 微信模板消息配置

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 进入「模板消息」功能
3. 创建一个模板，包含以下字段：
   - `repo_name`：项目名称
   - `branch_name`：分支名称
   - `commit_author`：提交作者
   - `commit_message`：提交消息
4. 获取模板 ID 并添加到 GitHub Secrets

### ChatGLM 配置

1. 访问 [智谱 AI 开放平台](https://open.bigmodel.cn/)
2. 注册并获取 API 密钥
3. 将 API 地址和密钥添加到 GitHub Secrets

## 工作流程

1. **代码推送**：开发者向 `master` 分支推送代码或创建 PR
2. **触发 Action**：GitHub Actions 工作流被触发
3. **环境准备**：设置 JDK 环境，下载 SDK JAR 包
4. **信息收集**：获取仓库、分支、作者和提交消息信息
5. **代码审查**：运行 SDK，获取代码差异并进行 AI 审查
6. **结果存储**：将审查结果提交到指定的 GitHub 仓库
7. **通知发送**：通过微信模板消息发送审查结果链接

## 项目结构

```
selfai-code-review/
├── .github/workflows/          # GitHub Actions 工作流配置
├── selfai-code-review-sdk/     # 核心 SDK 代码
│   ├── src/main/java/top/lywovo/sdk/   # 源代码
│   │   ├── domain/            # 领域模型和服务
│   │   ├── infrastructure/     # 基础设施（Git、OpenAI、微信）
│   │   ├── types/utils/        # 工具类
│   │   └── OpenAiCodeReview.java  # 主类
│   └── pom.xml                # Maven 配置
└── pom.xml                    # 父项目配置
```

## 核心功能

### 1. 代码差异获取

通过 Git 命令获取代码变更，支持分析提交的代码差异。

### 2. AI 代码审查

使用 ChatGLM 模型对代码差异进行智能分析，提供专业的代码审查建议。

### 3. 结果存储

将审查结果提交到指定的 GitHub 仓库，方便查看历史审查记录。

### 4. 微信通知

通过微信模板消息发送审查结果链接，及时通知相关人员。

## 常见问题

### Q: 为什么运行时出现 `NoClassDefFoundError` 错误？
A: 这通常是因为包路径配置错误。请确保 Maven 配置文件中的主类路径正确，并且代码中没有引用错误的包路径。

### Q: 为什么微信通知没有发送？
A: 请检查微信公众号配置是否正确，包括 AppID、AppSecret、OpenID 和模板 ID。同时确保服务器能够访问微信 API。

### Q: 为什么代码审查结果为空？
A: 请检查 Git 仓库是否有代码变更，以及 ChatGLM API 是否配置正确。

## 技术栈

- **Java**：核心开发语言
- **Maven**：项目构建工具
- **Git**：代码版本控制
- **ChatGLM**：AI 代码审查模型
- **GitHub Actions**：自动化工作流
- **微信公众号**：通知服务

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 许可证

本项目采用 MIT 许可证。

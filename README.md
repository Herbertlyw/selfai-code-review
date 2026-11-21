# SelfAI Code Review

读取当前 Git 仓库最近一次提交的差异，生成一份 Markdown 报告。可以在本机先看文件变更摘要，也可以将差异发给支持 Chat Completions 的模型，获得逐项评审意见。

## 运行第一个示例

需要 Git、JDK 11+ 和 Maven 3.8+。在本仓库根目录运行：

```sh
mvn clean verify
java -jar selfai-code-review-sdk/target/selfai-code-review-sdk-1.0.jar --summary
cat review.md
```

摘要由 Git 生成，无需密钥。要审查其他项目，在该项目的工作目录运行同一个 JAR 的绝对路径。工具读取 `HEAD` 提交，包括首次提交；合并提交按第一父提交比较。报告默认写入工作目录的 `review.md`，可用 `REVIEW_OUTPUT` 指定路径。

## 模型评审

通过终端环境或 CI Secrets 提供 `CHATGLM_APIKEYSECRET`，然后去掉 `--summary` 运行。默认请求智谱的 Chat Completions 地址，模型名为 `glm-4-flash`。使用其他兼容端点时，设置 `CHATGLM_APIHOST` 为完整的请求地址，`CHATGLM_MODEL` 为账号可用的模型名。服务使用 Bearer 鉴权。提交差异会发送给所配置的端点，超过 200 KB 的文本差异会终止处理。

```sh
java -jar selfai-code-review-sdk/target/selfai-code-review-sdk-1.0.jar
```

## 归档与通知

| 目的 | 环境变量 |
| --- | --- |
| 写入日志仓库 | `GITHUB_REVIEW_LOG_URI` 为已有 GitHub 仓库的 HTTPS 地址，`GITHUB_TOKEN` 对该仓库有内容写权限 |
| 发送微信模板消息 | 配置日志仓库后，设置 `WEIXIN_APPID`、`WEIXIN_SECRET`、`WEIXIN_TOUSER`、`WEIXIN_TEMPLATE_ID` |
| 通知中的提交信息 | `COMMIT_PROJECT`、`COMMIT_BRANCH`、`COMMIT_AUTHOR`、`COMMIT_MESSAGE` |

本地报告先落盘，随后才归档和通知。模型、推送或通知失败时，命令以非零状态退出。GitHub Actions 在 push 和 PR 时从源码构建并上传摘要；默认分支推送且已配置模型密钥时，或手动运行时，工作流会生成 AI 报告。云端归档读取 Secrets 中的 `CODE_REVIEW_LOG_URI` 和 `CODE_TOKEN`，微信配置沿用上表的变量名。

## 验证方式

`mvn clean verify` 会建立一个只有首次提交的临时仓库，启动本机 HTTP 测试服务，检查实际发送的差异、鉴权头、Markdown 输出与失败退出码。线上模型、GitHub 归档和微信通知需要各自的账号配置。

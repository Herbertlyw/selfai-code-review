package top.lywovo.sdk;

import top.lywovo.sdk.domain.service.impl.OpenAiCodeReviewService;
import top.lywovo.sdk.infrastructure.git.GitCommand;
import top.lywovo.sdk.infrastructure.openai.impl.ChatGLM;
import top.lywovo.sdk.infrastructure.weixin.WeiXin;

import java.nio.file.Files;
import java.nio.file.Path;

public class OpenAiCodeReview {
    public static void main(String[] args) throws Exception {
        Path report = Path.of(env("REVIEW_OUTPUT", "review.md"));
        if (args.length == 1 && "--summary".equals(args[0])) {
            Files.writeString(report, "# 本次提交的文件变更\n\n```text\n" + GitCommand.show("--stat") + "```\n");
        } else if (args.length == 0) {
            String remote = env("GITHUB_REVIEW_LOG_URI", "");
            GitCommand git = new GitCommand(remote, remote.isEmpty() ? "" : required("GITHUB_TOKEN"),
                    env("COMMIT_PROJECT", "local"), env("COMMIT_BRANCH", "HEAD"),
                    env("COMMIT_AUTHOR", ""), env("COMMIT_MESSAGE", ""));
            WeiXin notification = null;
            if (!env("WEIXIN_APPID", "").isEmpty()) {
                if (remote.isEmpty()) throw new IllegalArgumentException("微信通知需要 GITHUB_REVIEW_LOG_URI");
                notification = new WeiXin(required("WEIXIN_APPID"), required("WEIXIN_SECRET"),
                        required("WEIXIN_TOUSER"), required("WEIXIN_TEMPLATE_ID"));
            }
            ChatGLM client = new ChatGLM(env("CHATGLM_APIHOST", "https://open.bigmodel.cn/api/paas/v4/chat/completions"),
                    required("CHATGLM_APIKEYSECRET"));
            new OpenAiCodeReviewService(git, client, notification).exec();
        } else {
            throw new IllegalArgumentException("Usage: java -jar selfai-code-review-sdk-1.0.jar [--summary]");
        }
        System.out.println("报告已写入 " + report.toAbsolutePath());
    }

    public static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String required(String key) {
        String value = env(key, "");
        if (value.isEmpty()) throw new IllegalArgumentException("缺少环境变量 " + key);
        return value;
    }
}

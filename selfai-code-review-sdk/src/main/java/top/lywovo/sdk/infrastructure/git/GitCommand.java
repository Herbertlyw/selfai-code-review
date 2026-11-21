package top.lywovo.sdk.infrastructure.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;
import top.lywovo.sdk.OpenAiCodeReview;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

public class GitCommand {
    private final String remote;
    private final String token;
    private final String project;
    private final String branch;
    private final String author;
    private final String message;

    public GitCommand(String remote, String token, String project, String branch, String author, String message) {
        this.remote = remote;
        this.token = token;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
    }

    public static String show(String format) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("git", "show", "--first-parent", "--format=", "--no-ext-diff",
                "--no-textconv", "--no-color", format, "HEAD", "--").redirectError(ProcessBuilder.Redirect.INHERIT).start();
        try {
            byte[] output = process.getInputStream().readNBytes(200_001);
            if (output.length > 200_000) throw new IOException("提交差异超过 200 KB，请拆分提交");
            if (process.waitFor() != 0) throw new IOException("无法读取 HEAD，请在有提交记录的 Git 工作区运行");
            return new String(output, StandardCharsets.UTF_8);
        } finally {
            process.destroy();
        }
    }

    public String diff() throws IOException, InterruptedException { return show("--patch"); }

    public String commitAndPush(String review) throws Exception {
        Path local = Path.of(OpenAiCodeReview.env("REVIEW_OUTPUT", "review.md"));
        Files.writeString(local, review);
        if (remote.isEmpty()) return local.toUri().toString();

        String origin = remote.replaceFirst("(?:\\.git)?/?$", "");
        Path checkout = Files.createTempDirectory("review-log-");
        UsernamePasswordCredentialsProvider auth = new UsernamePasswordCredentialsProvider("x-access-token", token);
        try (Git git = Git.cloneRepository().setURI(origin + ".git").setDirectory(checkout.toFile())
                .setCredentialsProvider(auth).call()) {
            String path = LocalDate.now() + "/" + UUID.randomUUID() + ".md";
            Files.createDirectories(checkout.resolve(path).getParent());
            Files.writeString(checkout.resolve(path), review);
            git.add().addFilepattern(path).call();
            String commit = git.commit().setAuthor("Code Review", "review@localhost")
                    .setCommitter("Code Review", "review@localhost").setMessage("Add review for " + project).call().name();
            for (PushResult result : git.push().setCredentialsProvider(auth).call()) {
                for (RemoteRefUpdate update : result.getRemoteUpdates()) {
                    if (update.getStatus() != RemoteRefUpdate.Status.OK) {
                        throw new IOException("日志仓库推送失败：" + update.getStatus());
                    }
                }
            }
            return origin + "/blob/" + commit + "/" + path;
        } finally {
            FileUtils.delete(checkout.toFile(), FileUtils.RECURSIVE | FileUtils.RETRY);
        }
    }

    public String getProject() { return project; }
    public String getBranch() { return branch; }
    public String getAuthor() { return author; }
    public String getMessage() { return message; }
}

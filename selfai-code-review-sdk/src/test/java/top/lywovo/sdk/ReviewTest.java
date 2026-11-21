package top.lywovo.sdk;

import com.alibaba.fastjson2.JSON;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class ReviewTest {
    @Test
    public void runsFirstCommitAgainstLocalModelAndPropagatesErrors() throws Exception {
        Path dir = Files.createTempDirectory("review-test-");
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        try {
            git(dir, "init", "-q");
            Files.writeString(dir.resolve("hello.txt"), "hello\n");
            git(dir, "add", "hello.txt");
            git(dir, "-c", "user.name=Review", "-c", "user.email=review@localhost", "commit", "-qm", "first");
            server.createContext("/model", exchange -> {
                var payload = JSON.parseObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String patch = payload.getJSONArray("messages").getJSONObject(1).getString("content");
                boolean valid = patch.contains("+hello") && "Bearer test-key".equals(exchange.getRequestHeaders().getFirst("Authorization"));
                byte[] reply = "{\"choices\":[{\"message\":{\"content\":\"checked hello.txt\"}}]}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(valid ? 200 : 400, reply.length);
                exchange.getResponseBody().write(reply);
                exchange.close();
            });
            server.start();
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            assertEquals(0, run(dir, base + "/model"));
            assertEquals("checked hello.txt", Files.readString(dir.resolve("review.md")));
            assertNotEquals(0, run(dir, base + "/absent"));
        } finally {
            server.stop(0);
            org.eclipse.jgit.util.FileUtils.delete(dir.toFile(), org.eclipse.jgit.util.FileUtils.RECURSIVE);
        }
    }

    private static int run(Path dir, String endpoint) throws Exception {
        String cp = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", cp, OpenAiCodeReview.class.getName()).directory(dir.toFile());
        process.environment().remove("GITHUB_REVIEW_LOG_URI");
        process.environment().remove("WEIXIN_APPID");
        process.environment().put("REVIEW_OUTPUT", "review.md");
        process.environment().put("CHATGLM_APIHOST", endpoint);
        process.environment().put("CHATGLM_APIKEYSECRET", "test-key");
        return process.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start().waitFor();
    }

    private static void git(Path dir, String... args) throws Exception {
        var command = new java.util.ArrayList<String>();
        command.add("git");
        command.addAll(java.util.Arrays.asList(args));
        assertEquals(0, new ProcessBuilder(command).directory(dir.toFile()).inheritIO().start().waitFor());
    }
}

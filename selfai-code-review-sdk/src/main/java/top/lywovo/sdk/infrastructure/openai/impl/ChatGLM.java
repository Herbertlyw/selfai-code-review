package top.lywovo.sdk.infrastructure.openai.impl;

import com.alibaba.fastjson2.JSON;
import top.lywovo.sdk.infrastructure.openai.IOpenAI;
import top.lywovo.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import top.lywovo.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ChatGLM implements IOpenAI {
    private final URI endpoint;
    private final String apiKey;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public ChatGLM(String endpoint, String apiKey) {
        this.endpoint = URI.create(endpoint);
        this.apiKey = apiKey;
    }

    public ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofMinutes(2))
                .header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(payload))).build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("模型服务返回 HTTP " + response.statusCode());
        return JSON.parseObject(response.body(), ChatCompletionSyncResponseDTO.class);
    }
}

package top.lywovo.sdk.types.utils;

import com.alibaba.fastjson2.JSON;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class WXAccessTokenUtils {
    public static String getAccessToken(String appId, String secret) throws IOException, InterruptedException {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                + URLEncoder.encode(appId, StandardCharsets.UTF_8) + "&secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15)).build();
        HttpResponse<String> result = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        String token = JSON.parseObject(result.body()).getString("access_token");
        if (result.statusCode() != 200 || token == null) throw new IOException("微信令牌请求失败");
        return token;
    }
}

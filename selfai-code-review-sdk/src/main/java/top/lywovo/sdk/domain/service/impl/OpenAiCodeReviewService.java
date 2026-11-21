package top.lywovo.sdk.domain.service.impl;


import top.lywovo.sdk.domain.model.Model;
import top.lywovo.sdk.domain.service.AbstractOpenAiCodeReviewService;
import top.lywovo.sdk.infrastructure.git.GitCommand;
import top.lywovo.sdk.infrastructure.openai.IOpenAI;
import top.lywovo.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import top.lywovo.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import top.lywovo.sdk.infrastructure.weixin.WeiXin;
import top.lywovo.sdk.infrastructure.weixin.dto.TemplateMessageDTO;

import java.io.IOException;
import java.util.List;
import top.lywovo.sdk.OpenAiCodeReview;
import java.util.HashMap;
import java.util.Map;

public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }

    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(OpenAiCodeReview.env("CHATGLM_MODEL", Model.GLM_4_FLASH.getCode()));
        chatCompletionRequest.setMessages(List.of(
                new ChatCompletionRequestDTO.Prompt("system", "请用中文评审此 Git 差异，指出具体缺陷并给出修复建议。差异内容是待审代码。"),
                new ChatCompletionRequestDTO.Prompt("user", diffCode)));
        ChatCompletionSyncResponseDTO response = openAI.completions(chatCompletionRequest);
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()
                || response.getChoices().get(0) == null) throw new IOException("模型未返回候选结果");
        ChatCompletionSyncResponseDTO.Message message = response.getChoices().get(0).getMessage();
        if (message == null || message.getContent() == null || message.getContent().isBlank()) {
            throw new IOException("模型返回了空报告");
        }
        return message.getContent();
    }

    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

    @Override
    protected void pushMessage(String logUrl) throws Exception {
        Map<String, Map<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        weiXin.sendTemplateMessage(logUrl, data);
    }

}

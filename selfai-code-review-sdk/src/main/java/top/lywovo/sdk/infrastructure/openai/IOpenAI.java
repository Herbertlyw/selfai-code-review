package top.lywovo.sdk.infrastructure.openai;


import top.lywovo.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import top.lywovo.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

public interface IOpenAI {

    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception;

}

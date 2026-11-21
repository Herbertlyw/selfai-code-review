package top.lywovo.sdk.domain.service;


import java.io.IOException;


import top.lywovo.sdk.infrastructure.git.GitCommand;
import top.lywovo.sdk.infrastructure.openai.IOpenAI;
import top.lywovo.sdk.infrastructure.weixin.WeiXin;

public abstract class AbstractOpenAiCodeReviewService implements IOpenAiCodeReviewService {

    protected final GitCommand gitCommand;
    protected final IOpenAI openAI;
    protected final WeiXin weiXin;

    public AbstractOpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        this.gitCommand = gitCommand;
        this.openAI = openAI;
        this.weiXin = weiXin;
    }

    @Override
    public void exec() throws Exception {
        String diff = getDiffCode();
        String review = diff.isBlank() ? "本次提交没有文本差异。" : codeReview(diff);
        String location = recordCodeReview(review);
        if (weiXin != null) pushMessage(location);
    }

    protected abstract String getDiffCode() throws IOException, InterruptedException;

    protected abstract String codeReview(String diffCode) throws Exception;

    protected abstract String recordCodeReview(String recommend) throws Exception;

    protected abstract void pushMessage(String logUrl) throws Exception;

}

package com.edusoho.kuozhi.v3.model.bal.article;

import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.v3.model.bal.push.ServiceProviderModel;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by howzhi on 15/9/17.
 */
public class ArticleModel extends ServiceProviderModel {

    public List<Article> articleList;

    public static ArticleModel create(int spId, List<Article> articleList) {

        ArticleModel articleModel = new ArticleModel();

        if (articleList == null) {
            articleList = new ArrayList<>();
        }

        articleModel.id = -1;
        articleModel.spId = spId;
        articleModel.type = PushUtil.ArticleType.TYPE;
        articleModel.toId = 0;

        articleModel.body = new Gson().toJson(articleList);
        articleModel.createdTime = (int) (System.currentTimeMillis() / 1000);
        articleModel.articleList = articleList;
        return articleModel;
    }

    private ArticleModel(){
    }

    public ArticleModel(MessageEntity messageEntity)
    {
        MessageBody messageBody = new MessageBody(messageEntity);
        this.id = messageEntity.getId();
        this.spId = messageBody.getSource().getId();
        this.title = "";
        this.content = messageBody.getBody();
        this.toId = 0;
        this.createdTime = messageBody.getCreatedTime();
        ArrayList<Article> arrayList = parseChatBody(messageBody.getBody());
        this.articleList = arrayList;
    }

    private ArrayList<Article> parseChatBody(String body) {
        Gson gson = new Gson();
        ArrayList<Article> arrayList;
        try {
            arrayList = gson.fromJson(body, new TypeToken<ArrayList<Article>>(){}.getType());
        } catch (Exception e) {
            ArticleMessageBody articleMessageBody = gson.fromJson(body, ArticleMessageBody.class);
            arrayList = new ArrayList<>();
            Article article = new Article();
            article.body = articleMessageBody.getContent();
            article.title = articleMessageBody.getTitle();
            article.thumb = articleMessageBody.getImage();
            article.id = articleMessageBody.getId();
            arrayList.add(article);
        }

        return arrayList;
    }
}

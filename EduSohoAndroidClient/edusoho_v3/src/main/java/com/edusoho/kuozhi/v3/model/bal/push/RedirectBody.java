package com.edusoho.kuozhi.v3.model.bal.push;

import org.json.JSONObject;
import java.io.Serializable;

/**
 * Created by howzhi on 15/9/29.
 */
public class RedirectBody implements Serializable {

    public String type;
    public String fromType;
    public int id;
    public String title;
    public String image;
    public String content;
    public String url;
    public String source;

    public static RedirectBody createByJsonObj(JSONObject body) {

        RedirectBody redirectBody = new RedirectBody();
        redirectBody.type = body.optString("type");
        redirectBody.fromType = body.optString("fromType");
        redirectBody.title = body.optString("title");
        redirectBody.image = body.optString("image");
        redirectBody.content = body.optString("content");
        redirectBody.url = body.optString("url");
        redirectBody.source = body.optString("source");
        redirectBody.id = body.optInt("id");

        return redirectBody;
    }

    public static RedirectBody createByShareContent(String url, String title, String about, String pic) {

        RedirectBody redirectBody = new RedirectBody();

        redirectBody.type = "share.redirect";
        redirectBody.fromType = "share";
        redirectBody.title = title;
        redirectBody.image = pic;
        redirectBody.content = about;
        redirectBody.url = url;
        redirectBody.source = "self";
        redirectBody.id = 0;

        return redirectBody;
    }
}

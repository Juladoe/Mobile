package com.edusoho.kuozhi.imserver.ui.util;

import com.edusoho.kuozhi.imserver.ui.entity.AudioBody;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suju on 16/8/30.
 */
public class AudioUtil {

    public static AudioBody getAudioBody(String body) {
        AudioBody audioBody = new AudioBody();
        try {
            JSONObject jsonObject = new JSONObject(body);
            audioBody.setDuration(jsonObject.optInt("d"));
            audioBody.setFile(jsonObject.optString("f"));
            if (jsonObject.has("file")) {
                audioBody.setFile(jsonObject.getString("file"));
            }
            if (jsonObject.has("duration")) {
                audioBody.setDuration(jsonObject.getInt("duration"));
            }
        } catch (JSONException e) {
            return audioBody;
        }

        return audioBody;
    }
}

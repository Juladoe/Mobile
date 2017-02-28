package com.edusoho.kuozhi.imserver.ui.entity;


/**
 * Created by suju on 16/8/28.
 */
public class AudioBody {

    private int duration;
    private String file;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append("{\"f\":\"")
                .append(file)
                .append("\", \"d\":")
                .append(duration)
                .append("}")
                .toString();
    }
}

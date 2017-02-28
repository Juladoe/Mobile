package com.edusoho.kuozhi.v3.model.bal;

/**
 * Created by JesseHuang on 15/6/16.
 */
public enum DownloadStatus {
    STARTING, FINISH, NONE, BEGIN;

    public static DownloadStatus value(int status) {
        switch (status) {
            case 0:
                return NONE;
            case 1:
                return BEGIN;
            case 2:
                return STARTING;
            case 3:
                return STARTING;
        }
        return NONE;
    }
}

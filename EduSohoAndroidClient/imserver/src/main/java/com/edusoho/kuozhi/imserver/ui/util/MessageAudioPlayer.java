package com.edusoho.kuozhi.imserver.ui.util;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.edusoho.kuozhi.imserver.ui.listener.AudioPlayStatusListener;

/**
 * Created by suju on 16/8/28.
 */
public class MessageAudioPlayer {

    private String mAudioFile;
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private AudioPlayStatusListener mAudioPlayStatusListener;

    public MessageAudioPlayer(Context context, String audioFile, AudioPlayStatusListener listener) {
        this.mContext = context;
        this.mAudioFile = audioFile;
        this.mAudioPlayStatusListener = listener;
    }

    public void play() {
        mMediaPlayer = MediaPlayer.create(mContext, Uri.parse(mAudioFile));
        if (mMediaPlayer == null) {
            stop();
            return;
        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stop();
            }
        });
        mMediaPlayer.start();
        mAudioPlayStatusListener.onPlay();
        pauseMusic();
    }

    public void stop() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        if (mAudioPlayStatusListener != null) {
            mAudioPlayStatusListener.onStop();
        }

        mMediaPlayer.release();
        mMediaPlayer = null;
        resumeMusic();
    }

    private void resumeMusic() {
        AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(null);

        Intent freshIntent = new Intent();
        freshIntent.setAction("com.android.music.musicservicecommand.resume");
        freshIntent.putExtra("command", "resume");
        mContext.sendBroadcast(freshIntent);
    }

    private void pauseMusic() {
        Intent freshIntent = new Intent();
        freshIntent.setAction("com.android.music.musicservicecommand.pause");
        freshIntent.putExtra("command", "pause");
        mContext.sendBroadcast(freshIntent);

        AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }
}

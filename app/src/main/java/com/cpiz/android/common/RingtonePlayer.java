package com.cpiz.android.common;

import android.media.MediaPlayer;
import android.util.Log;

import com.cpiz.android.playground.PlaygroundApp;

/**
 * Created by ct on 15/3/26.
 */
public final class RingtonePlayer {
    private MediaPlayer mMediaPlayer;
    private static final String TAG = RingtonePlayer.class.getSimpleName();
    public static final RingtonePlayer Instance = new RingtonePlayer();

    private RingtonePlayer() {
        mMediaPlayer = null;
    }

    public void stopRingTone() {
        if (mMediaPlayer != null) {
            Log.d("MEDIA PLAYER : STOP", " ");
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void playRingTone(int resId) {
        playRingTone(resId, true);
    }

    public void playRingTone(int resId, boolean repeat) {
        stopRingTone();
        mMediaPlayer = MediaPlayer.create(PlaygroundApp.getInstance(), resId);
        if (mMediaPlayer == null) {
            //创建mediaplayer失败 可能时资源问题哦
            Log.d(TAG, "resource error");
            return;
        }

        mMediaPlayer.setLooping(repeat);

        mMediaPlayer.start();
        if (!repeat) {
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopRingTone();
                }
            });
        }
    }
}
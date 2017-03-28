/*
 * Copyright (c) 2017. Ruslan Primak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified 3/29/17 1:30 AM
 */

package com.example.android.mediaplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.Locale;

/**
 * Provides handlers for MediaPlayer
 */
public class MediaPlayerHandler {
    private final static int MAX_VOLUME = 51;
    private int mVolume = MAX_VOLUME / 2;

    private final Toast mToast;
    private final Context mContext;
    private final MediaPlayer mPlayer;
    private final SeekBar mSeekBar;
    private final int mDuration;
    private final Handler mHandler;
    private final Runnable updateSeekBar;

    @SuppressLint("ShowToast")
    MediaPlayerHandler(Context context, SeekBar seekBar) {
        mContext = context;
        mSeekBar = seekBar;
        mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        mPlayer = MediaPlayer.create(mContext, R.raw.sample);
        mPlayer.setLooping(true);
        mDuration = mPlayer.getDuration();
        mHandler = new Handler();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean isPaused = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mPlayer.seekTo(mDuration * progress / seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPaused = mPlayer.isPlaying();
                pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isPaused) {
                    isPaused = false;
                    play();
                }
            }
        });

        /* Runnable in UI thread to update seekbar */
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                mSeekBar.setProgress(100 * mPlayer.getCurrentPosition() / mDuration);
                mHandler.postDelayed(updateSeekBar, 500);
            }
        };

        mHandler.post(updateSeekBar);
    }

    /**
     * Changes volume of the media player
     * @param delta - delta of volume changes
     */
    public void changeVolume(int delta) {
        mVolume += delta;

        if (mVolume < 0) {
            mVolume = 0;
        } else if (mVolume >= MAX_VOLUME) {
            mVolume = MAX_VOLUME - 1;
        }

        /* MediaPlayer.setVolume uses logarithmic scale. The value specified have to be
        between 0.0 and 1.0 */
        float logVol = (float) (1 - (Math.log(MAX_VOLUME - mVolume) / Math.log(MAX_VOLUME)));
        mPlayer.setVolume(logVol, logVol);
        if (delta != 0) {
            mToast.setText(String.format(Locale.getDefault(),
                    mContext.getString(R.string.ToastVolumeFormat),
                    (float) mVolume / (MAX_VOLUME - 1) * 100));
            mToast.show();
        }
    }

    /**
     * Starts or continues playing of media
     */
    public void play() {
        if (mPlayer.isPlaying()) {
            mPlayer.seekTo(0);
        } else {
            mPlayer.start();
        }
    }

    /**
     * Pauses playing of media
     */
    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }
}

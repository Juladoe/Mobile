package com.edusoho.longinus.view;

import android.graphics.Color;
import com.pili.pldroid.player.widget.PLVideoView;

/**
 * Created by suju on 16/10/24.
 */
public class LiveVideoView extends PLVideoView {

    public LiveVideoView(android.content.Context context) {
        super(context);
    }

    public LiveVideoView(android.content.Context context, android.util.AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public LiveVideoView(android.content.Context context, android.util.AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override
    public void pause() {
        super.pause();
        clearDraw();
    }

    @Override
    public void start() {
        super.start();
        getSurfaceView().setBackgroundColor(Color.TRANSPARENT);
    }

    public void clearDraw(){
        getSurfaceView().setBackgroundColor(Color.BLACK);
    }
}

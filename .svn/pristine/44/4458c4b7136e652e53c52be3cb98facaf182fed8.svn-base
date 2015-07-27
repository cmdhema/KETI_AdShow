package adshow.org.keti.ad_show;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by Jaewook on 2015-05-06.
 */
public class CustomVideoView extends VideoView {

    private PlayPauseListener mListener;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPlayPauseListener(PlayPauseListener listener) {
        mListener = listener;
    }

    @Override
    public void start() {
        super.start();

        if ( mListener != null )
            mListener.onPlay();
    }

    @Override
    public void pause() {
        super.pause();

        if ( mListener != null )
            mListener.onPause();
    }

    public static interface PlayPauseListener {
        void onPlay();
        void onPause();
    }

}

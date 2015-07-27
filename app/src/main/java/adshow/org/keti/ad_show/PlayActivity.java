package adshow.org.keti.ad_show;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Jaewook on 2015-06-03.
 */

@EActivity(R.layout.activity_play)
public class PlayActivity extends Activity implements SurfaceHolder.Callback {

    Animation fadeIn;
    Animation fadeOut;

    @ViewById(R.id.iv_ci1)
    ImageView ci1Iv;
    @ViewById(R.id.iv_ci2)
    ImageView ci2Iv;

    @ViewById(R.id.iv_snap)
    ImageView snapIv;
    @ViewById(R.id.layout_ci)
    RelativeLayout ciLayout;
    @ViewById(R.id.layout_snap)
    RelativeLayout snapLayout;

    @ViewById(R.id.surface_play)
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Surface surface;

    @Extra
    int type;

    boolean isCurrentImage;

    Handler handler;
    int resourceIndex = 0;
    MediaPlayer.OnCompletionListener mComplete = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {

            if (resourceIndex == MainActivity.adapter.getCount())
                resourceIndex = 0;
            mPlayer.release();
            playMovies();
        }
    };
    boolean isPlaying;
    MediaPlayer mPlayer;
    Runnable snapRunnable = new Runnable() {
        @Override
        public void run() {
            playMovies();
        }
    };

    @AfterViews
    void init() {
        setAnimation();
        handler = new Handler();
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void playMovies() {

        isPlaying = true;
        Log.i("surfaceCreated", "playMovies(), " + resourceIndex);
        PlayerResource resource = MainActivity.adapter.getItem(resourceIndex);
        resourceIndex++;

        if (resource.type != 2) {

            isCurrentImage = false;
            snapLayout.setVisibility(View.GONE);

            surfaceView.setVisibility(View.VISIBLE);
            try {
                mPlayer = new MediaPlayer();
                mPlayer.setOnCompletionListener(mComplete);
                mPlayer.setDisplay(surfaceHolder);
                mPlayer.setDataSource(resource.path);
                mPlayer.prepare();
                mPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ciLayout.setVisibility(View.INVISIBLE);
            isCurrentImage = true;

            snapLayout.setVisibility(View.VISIBLE);


            surfaceView.setVisibility(View.INVISIBLE);
            snapIv.setVisibility(View.VISIBLE);
            snapIv.setImageURI(Uri.parse(resource.path));
            handler.postDelayed(snapRunnable, 5000);

            if (resourceIndex == MainActivity.adapter.getCount())
                resourceIndex = 0;
        }

    }

    void stopMovie() {
        if (!isCurrentImage) {
            mPlayer.stop();
            mPlayer.release();
            isPlaying = false;
        }
        handler.removeCallbacks(snapRunnable);
    }

    @Override
    public void onBackPressed() {
        stopMovie();
        super.onBackPressed();
    }

    private void setAnimation() {

        fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//        this.surfaceHolder = surfaceHolder;
        Log.i("PlayActivity", "surfaceCreated");
        if (mPlayer == null) {
            surface = surfaceHolder.getSurface();
            playMovies();
        } else {
//            mPlayer.setDisplay(surfaceHolder);
//            mPlayer.reset();
        }

        Log.i("PlayActivity", "surfaceCreated");
//        playMovies();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i("PlayActivity", "surfaceDestroyed");
    }

}

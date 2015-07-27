package adshow.org.keti.ad_show;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jaewook on 2015-06-03.
 */

@EActivity(R.layout.activity_play)
public class PlayActivity extends Activity implements SurfaceHolder.Callback {

    String ciFolderPath = Environment.getExternalStorageDirectory() + "/CI";
    File ciDir;
    String[] ciArray;
    int ciCount;

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

    Timer timer;

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
        timer = new Timer();
        handler = new Handler();
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        timer.schedule(task, 3000, 15000);

        ciDir = makeDirectory(ciFolderPath);
        ciArray = new String[ciDir.list().length];
        for ( int i = 0; i < ciDir.list().length; i++ ) {
            ciArray[i] = ciFolderPath + "/" + ciDir.list()[i];
        }
    }

    private void playMovies() {

        isPlaying = true;
        Log.i("surfaceCreated", "playMovies(), " + resourceIndex);
        PlayerResource resource = MainActivity.adapter.getItem(resourceIndex);
        resourceIndex++;

        if (resource.type != 2) {
//            clearSurface();
//            timer.schedule(task, 3000, 15000);

            isCurrentImage = false;
            snapLayout.setVisibility(View.GONE);

//            snapIv.setVisibility(View.INVISIBLE);
            surfaceView.setVisibility(View.VISIBLE);
//            surfaceView.bringToFront();
//            if ( resource.type == 0 )
//                videoView.setVideoPath(resource.path);
//            else if ( resource.type == 1 )
//                videoView.setVideoURI(Uri.parse(resource.path));
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
//            snapLayout.bringToFront();
//            snapIv.startAnimation(fadeIn);
            snapIv.setImageURI(Uri.parse(resource.path));
            handler.postDelayed(snapRunnable, resource.time * 300);

            if (resourceIndex == MainActivity.adapter.getCount())
                resourceIndex = 0;
        }

    }

    private void clearSurface() {
        EglCore eglCore = new EglCore();
        WindowSurface win = new WindowSurface(eglCore, surface, false);
        win.makeCurrent();
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        win.swapBuffers();
        win.release();
        eglCore.release();
    }

    void stopMovie() {
        if (!isCurrentImage) {
            mPlayer.stop();
            mPlayer.release();
            isPlaying = false;
        }
        handler.removeCallbacks(snapRunnable);
        timer.cancel();
        task.cancel();
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

    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            Message msg = ciHandler.obtainMessage();
            ciHandler.sendMessage(msg);
        }
    };

    Handler ciHandler = new Handler() {
        public void handleMessage(Message msg) {
            File ci1 = null;
            File ci2 = null;

            if (ciArray.length == 1) {
                ci1 = new File(ciArray[0]);
                ci2 = new File(ciArray[0]);
            } else if (ciCount + 1 == ciArray.length) {
                ci1 = new File(ciArray[0]);
                ci2 = new File(ciArray[1]);
            } else {
                ci1 = new File(ciArray[ciCount]);
                ci2 = new File(ciArray[ciCount+1]);
                ciCount++;
            }
            if (ciArray.length > 0) {
                Bitmap ci1Image = BitmapFactory.decodeFile(ci1.getAbsolutePath());
                Bitmap ci2Image = BitmapFactory.decodeFile(ci2.getAbsolutePath());
                ci1Iv.setImageBitmap(ci1Image);
                ci2Iv.setImageBitmap(ci2Image);
            }

            ciLayout.setVisibility(View.VISIBLE);
            ciLayout.startAnimation(fadeOut);
        }
    };

    private File makeDirectory(String dir_path) {
        File dir = new File(dir_path);
        if (!dir.exists()) {
            Log.i("Dir", "crate");
            dir.mkdirs();
        } else {
            Log.i("Dir", "no crate");
        }
        return dir;
    }
}

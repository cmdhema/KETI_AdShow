package adshow.org.keti.ad_show;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.urqa.clientinterface.URQAController;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements SurfaceHolder.Callback {

    Gson gson = new Gson();

    SharedPreferences sp;
    SharedPreferences.Editor sEditor;

    final int ACTIVITY_CHOOSE_IMAGE_FILE = 1;
    final int ACTIVITY_CHOOSE_MOVIE_FILE = 2;
    String[] fileUrls = {
//            "http://ketiabcs.iptime.org/~jpark/media_dmsp/Live/000A.mp4",
//            "http://ketiabcs.iptime.org/~jpark/media_dmsp/Live/001A.mp4",
//            "http://ketiabcs.iptime.org/~jpark/media_dmsp/Live/001B.mp4 ",
//            "http://ketiabcs.iptime.org/~jpark/media_dmsp/Live/001C.mp4",
//            "http://ketiabcs.iptime.org/~jpark/media_dmsp/Live/002A.mp4",
//            "http://ketiabcs.iptime.org/~jpark/media_dmsp/Live/003B.mp4",
//            "http://ketiabcs.iptime.org/~jpark/media_dmsp/Live/004A.mp4"
            "http://ketiabcs.iptime.org/~jpark/media_dmsp/Live/000A.mp4",
            "http://ketiabcs.iptime.org/~jpark/media_dmsp/Live/002C.mp4",

    };
    String fileLocalPath;
    MediaPlayer mPlayer;

    @ViewById(R.id.lv_file)
    ListView fileListView;

    @ViewById(R.id.et_serverUrl)
    EditText serverUrlEt;
    @ViewById(R.id.preview_iv)
    ImageView previewImageView;
    @ViewById(R.id.preview_surfaceview)
    SurfaceView previewSurfaceView;
    SurfaceHolder previewHolder;

    ArrayList<PlayerResource> resourceList;

    int previewIndex = 0;

    static PlayerListAdapter adapter;
    boolean isPlaying;

    MediaPlayer.OnVideoSizeChangedListener mSizeChange = new MediaPlayer.OnVideoSizeChangedListener() {

        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            // TODO Auto-generated method stub
        }
    };

    @AfterViews
    void init() {
        URQAController.InitializeAndStartSession(getApplicationContext(), "AD930151");
        resourceList = new ArrayList<>();

        previewHolder = previewSurfaceView.getHolder();
        previewHolder.addCallback(this);

        sp = getSharedPreferences("adshow", MODE_PRIVATE);
        sEditor = sp.edit();

        String listString = sp.getString("list", "null");
        Log.i("MainActivity", listString);

        if ( !listString.equals("null") )
            resourceList = gson.fromJson(listString, ResourceList.class).list;

//        for ( int i = 0; i < fileUrls.length; i++ ) {
//            PlayerResource resource = new PlayerResource();
//            resource.type = 1;
//            resource.path = fileUrls[i];
//            resourceList.add(resource);
//        }

        View listHeaderVIew = getLayoutInflater().inflate(R.layout.list_header, null, false);
        fileListView.addHeaderView(listHeaderVIew);

        adapter = new PlayerListAdapter(this, R.layout.list_row, resourceList);
        adapter.setOnFileItemClickListener(new PlayerListAdapter.OnFileItemClickListener() {
            @Override
            public void onItemClick(View view) {
                previewIndex = (int) view.getTag();
                mPlayer.stop();
                mPlayer.reset();
                startPreview();
            }
        });
        fileListView.setAdapter(adapter);
    }

    @Click(R.id.play)
    void play() {
        if (adapter.getCount() > 0) {
            mPlayer.stop();
            mPlayer.reset();
            Intent intent = new Intent(this, PlayActivity_.class);
            intent.putExtra("type", adapter.getItem(0).type);
            adapter.notifyDataSetChanged();
            startActivity(intent);
        } else
            Toast.makeText(getApplicationContext(), "동영상을 추가하세요", Toast.LENGTH_SHORT).show();
    }

    void startPreview() {
        PlayerResource resource = adapter.getItem(previewIndex);
         if (resource.type == 2) {
            //로컬 이미지
            previewSurfaceView.setVisibility(View.GONE);
            previewImageView.setVisibility(View.VISIBLE);
            previewImageView.setImageURI(Uri.parse(resource.path));
        } else if ( resource.type == 3 ) {
             //서버 이미지
         }
         else {
             previewImageView.setVisibility(View.GONE);
             previewSurfaceView.setVisibility(View.VISIBLE);
             //동영상
             Log.i("startPreview", resource.path);
             try {
                 mPlayer.setDataSource(resource.path);
                 mPlayer.prepare();
                 mPlayer.start();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
    }

    @Click(R.id.btn_movieSelect)
    void movieSelect() {
        Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
        intent.putExtra(ExFilePicker.SET_FILTER_LISTED, new String[]{"mp4", "mov"});
        startActivityForResult(intent, ACTIVITY_CHOOSE_MOVIE_FILE);
    }

    @Click(R.id.btn_imageSelect)
    void ImageSelect() {

        Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
        intent.putExtra(ExFilePicker.SET_FILTER_LISTED, new String[]{"jpg", "jpeg", "png"});
        startActivityForResult(intent, ACTIVITY_CHOOSE_IMAGE_FILE);
    }

    @Click(R.id.btn_getFileList)
    void getFIleListFromServer() {
        String url = serverUrlEt.getText().toString();
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(url).setConverter(new GsonConverter(new Gson())).build();
        final VideoPathService service = restAdapter.create(VideoPathService.class);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> urls = service.listVideoPath();
                updateVideoPath(urls);
            }
        }).start();
    }

    @UiThread
    void updateVideoPath(List<String> urls) {
        for ( String name : urls ) {
            if ( name.endsWith("jpg") || name.endsWith("jpeg") || name.endsWith("png")) {
                PlayerResource resource = new PlayerResource();
                resource.type = 3;
                resource.path = serverUrlEt.getText().toString() + "/image/" + name;
                resourceList.add(resource);
            } else if ( name.endsWith("mp4")) {
                try {
                    PlayerResource resource = new PlayerResource();
                    resource.type = 1;
                    resource.path = serverUrlEt.getText().toString() + "/video/" + name;
                    mPlayer.setDataSource(resource.path);
                    mPlayer.prepare();
                    resource.playTime = mPlayer.getDuration();
                    mPlayer.reset();
                    resourceList.add(resource);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_CHOOSE_IMAGE_FILE: {
                if (data != null ) {
                    ExFilePickerParcelObject object = (ExFilePickerParcelObject) data.getParcelableExtra(ExFilePickerParcelObject.class.getCanonicalName());
                    if ( object.count > 0 ) {
                        fileLocalPath = object.path + object.names.get(0);
                        PlayerResource resource = new PlayerResource();
                        resource.path = fileLocalPath;
                        resource.type = 2;
                        resourceList.add(resource);
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
            }
            case ACTIVITY_CHOOSE_MOVIE_FILE: {
                if (data != null) {
                    try {
                        ExFilePickerParcelObject object = (ExFilePickerParcelObject) data.getParcelableExtra(ExFilePickerParcelObject.class.getCanonicalName());
                        if( object.count > 0 ) {
                            fileLocalPath = object.path + object.names.get(0);
                            PlayerResource resource = new PlayerResource();
                            resource.path = fileLocalPath;
                            resource.type = 0;
                            mPlayer = new MediaPlayer();
                            mPlayer.setDataSource(fileLocalPath);
                            mPlayer.prepare();
                            resource.playTime = mPlayer.getDuration();
                            resourceList.add(resource);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isPlaying) {
            mPlayer.stop();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ResourceList listJson = new ResourceList();
        listJson.list = resourceList;
        String list = gson.toJson(listJson);
        sEditor.putString("list", list);
        sEditor.commit();

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else {
//            mPlayer.reset();
        }

        mPlayer.setDisplay(surfaceHolder);
        mPlayer.setOnVideoSizeChangedListener(mSizeChange);

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }
}
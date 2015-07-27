package adshow.org.keti.ad_show;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by Jaewook on 2015-07-14.
 */
public interface VideoPathService {
    @GET("/videos")
    List listVideoPath();
}

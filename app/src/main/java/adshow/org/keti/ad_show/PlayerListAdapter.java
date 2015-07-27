package adshow.org.keti.ad_show;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jaewook on 2015-05-06.
 */
public class PlayerListAdapter extends ArrayAdapter<PlayerResource> {

    private LayoutInflater inflater;
    private Context context;
    private ArrayList<PlayerResource> list;
    private OnFileItemClickListener listener;

//    EditText delayTimeEt;

    public PlayerListAdapter(Context context, int textViewResourceId, ArrayList<PlayerResource> objects) {
        super(context, textViewResourceId, objects);
        inflater = LayoutInflater.from(context);
        this.context = context;
        list = objects;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public PlayerResource getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int index, View view, ViewGroup viewGroup) {
        View v = view;
        TextView fileNameTv = null;
        TextView playTimeTv = null;
//        EditText delayTimeEt = null;
        ImageView deleteIv;
        ImageView upIv;
        ImageView downIv;

        if ( v == null ) {
            v = inflater.inflate(R.layout.list_row, null );
        }
        upIv = (ImageView) v.findViewById(R.id.list_row_up);
        downIv = (ImageView) v.findViewById(R.id.list_row_down);
        deleteIv = (ImageView) v.findViewById(R.id.list_row_delete);
        fileNameTv = (TextView) v.findViewById(R.id.list_row_tv_filename);
//        delayTimeEt = (EditText) v.findViewById(R.id.list_row_et_delayTime);
        playTimeTv = (TextView) v.findViewById(R.id.list_row_tv_playtime);
        deleteIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.remove(index);
                notifyDataSetChanged();
            }
        });
        upIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( index > 0 )
                    Collections.swap(list, index, index-1);
                notifyDataSetChanged();
            }
        });
        downIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( index < list.size()-1 )
                    Collections.swap(list, index, index+1);
                notifyDataSetChanged();
            }
        });
        fileNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setTag(index);
                listener.onItemClick(view);
            }
        });
        PlayerResource resource = list.get(index);
        fileNameTv.setText(resource.path);

        int playTime = resource.playTime/1000;
        int min = playTime/60;
        int sec = playTime - min*60;
        if ( resource.type != 2) {
//            delayTimeEt.setVisibility(View.INVISIBLE);
            playTimeTv.setText(min + ":" + sec);
            playTimeTv.setVisibility(View.VISIBLE);
        } else {
//            delayTimeEt.setVisibility(View.VISIBLE);
            playTimeTv.setVisibility(View.INVISIBLE);
//            list.get(index).time = Integer.parseInt(delayTimeEt.getText().toString());
//            delayTimeEt.setText(list.get(index).time+"");
//            Log.i("TAG", list.get(index).time + ", " + delayTimeEt.getText().toString());
        }

        return v;
    }

    public interface OnFileItemClickListener {
        void onItemClick(View view);
    }

    public void setOnFileItemClickListener(OnFileItemClickListener listener) {
        this.listener = listener;
    }
}

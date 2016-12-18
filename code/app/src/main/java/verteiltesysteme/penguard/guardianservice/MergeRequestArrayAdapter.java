package verteiltesysteme.penguard.guardianservice;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import verteiltesysteme.penguard.R;
import verteiltesysteme.penguard.protobuf.PenguardProto;


public class MergeRequestArrayAdapter extends ArrayAdapter<PenguardProto.PGPMessage> {

    private int layoutResource;

    public MergeRequestArrayAdapter(Context context, ArrayList<PenguardProto.PGPMessage> objects) {
        super(context,R.layout.list_guardians, objects);
        this.layoutResource = R.layout.list_guardians; //using the guardian layout bc this will be the same layout/style there is no reason to use an extra one as they are supposed to look the same
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(layoutResource, null);
        }
        PenguardProto.MergeReq request;
        if (getItem(position)!= null){
           request  = getItem(position).getMergeReq();
        }else {
            request = null;
        }


        if (request != null) {
            ImageView icon = (ImageView)convertView.findViewById(R.id.imageView);
            icon.setImageTintList(ColorStateList.valueOf(0xff2f2f2f)); //2f2f2f
            TextView titleTextView = (TextView) convertView.findViewById(R.id.guardianName);

            if (titleTextView != null) {
                titleTextView.setText(request.getName());
            }
        }

        return convertView;
    }
}

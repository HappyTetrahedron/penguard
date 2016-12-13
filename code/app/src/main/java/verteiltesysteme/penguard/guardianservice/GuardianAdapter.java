package verteiltesysteme.penguard.guardianservice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import verteiltesysteme.penguard.R;


public class GuardianAdapter extends ArrayAdapter<Guardian> {
    private int layoutResource;

    GuardianAdapter(Context context, int layoutResource, List<Guardian> guardianList) {
        super(context, layoutResource, guardianList);
        this.layoutResource = layoutResource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(layoutResource, null);
        }


        Guardian guardian = getItem(position);

        //TODO use another layout

        if (guardian != null) {
            TextView titleTextView = (TextView) convertView.findViewById(R.id.penguinName);

            if (titleTextView != null) {
                //calculate lastSeen
                long lastSeen = System.currentTimeMillis() - guardian.getTimeStamp();
                titleTextView.setText(guardian.getName() + "last seen " + lastSeen);
            }
        }

        return convertView;
    }
}

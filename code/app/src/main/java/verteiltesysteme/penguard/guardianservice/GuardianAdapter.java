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


class GuardianAdapter extends ArrayAdapter<Guardian> {
    private int layoutResource;

    private final Guardian myself;

    GuardianAdapter(Context context, List<Guardian> guardianList, final Guardian myself) {
        super(context, R.layout.list_guardians, guardianList);
        this.layoutResource = R.layout.list_guardians;
        this.myself = myself;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(layoutResource, null);
        }


        Guardian guardian = getItem(position);

        if (guardian != null) {
            TextView titleTextView = (TextView) convertView.findViewById(R.id.guardianName);

            if (titleTextView != null) {
                //calculate lastSeen
                if (guardian.equals(myself)) {
                    titleTextView.setText(guardian.getName() + " - you");
                }
                else {
                    long lastSeen = System.currentTimeMillis() - guardian.getTimeStamp();
                    long lastSeenMinutes = lastSeen / (1000 * 60);
                    titleTextView.setText(guardian.getName() + " - last seen " + lastSeenMinutes + " minutes ago");
                }
            }
        }

        return convertView;
    }
}

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
           TextView guardianName = (TextView) convertView.findViewById(R.id.guardianName);
            TextView statusTV = (TextView) convertView.findViewById(R.id.guardianStatus);
            ImageView icon = (ImageView)convertView.findViewById(R.id.imageView);

            guardianName.setText(guardian.getName());


            if (guardian.equals(myself)) {
                guardianName.setText(guardian.getName());
                statusTV.setText("you");
                icon.setImageTintList(ColorStateList.valueOf(0xff2f2f2f)); //2f2f2f
            }
            else {
                if (guardian.getTimeStamp() == 0 ) {
                    statusTV.setText("never seen");
                    icon.setImageTintList(ColorStateList.valueOf(0xffa8a8a8)); //a8a8a8
                }
                else {
                    long lastSeen = System.currentTimeMillis() - guardian.getTimeStamp();
                    long lastSeenMinutes = lastSeen / (1000 * 60);
                    if (lastSeenMinutes == 0) {
                        statusTV.setText("online");
                        icon.setImageTintList(ColorStateList.valueOf(0xff2f2f2f)); //2f2f2f
                    } else {
                        statusTV.setText("last seen " + lastSeenMinutes + " minutes ago");
                        icon.setImageTintList(ColorStateList.valueOf(0xffa8a8a8)); //a8a8a8
                    }
                }

            }
        }

        return convertView;
    }
}

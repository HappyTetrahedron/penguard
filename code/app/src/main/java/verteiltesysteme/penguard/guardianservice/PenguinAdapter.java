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

class PenguinAdapter extends ArrayAdapter<Penguin> {

    private int layoutResource;

    PenguinAdapter(Context context, List<Penguin> unmodifiablePenguinList) {
        super(context, R.layout.list_penguins, unmodifiablePenguinList);
        this.layoutResource = R.layout.list_penguins;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(layoutResource, null);
        }

        Penguin penguin = getItem(position);

        if (penguin != null) {
            TextView penguinName = (TextView) convertView.findViewById(R.id.penguinName);
            TextView statusTV = (TextView) convertView.findViewById(R.id.PenguinStatus);
            ImageView icon = (ImageView)convertView.findViewById(R.id.imageView);

            penguinName.setText(penguin.getName());

            if (penguin.isSeen()){ //seen by myself
                icon.setImageTintList(ColorStateList.valueOf(0xff2f2f2f)); //2f2f2f
                statusTV.setText(getContext().getString(R.string.pengSeenBySelf));
            }else if (penguin.isSeenByAnyone() && !penguin.isSeen()){
                statusTV.setText(getContext().getString(R.string.pengSeenByElse));
                icon.setImageTintList(ColorStateList.valueOf(0xffa8a8a8)); //a8a8a8
            }else {
                statusTV.setText(getContext().getString(R.string.pengNotSeen));
                icon.setImageTintList(ColorStateList.valueOf(0xffff9109));

            }
        }

        return convertView;
    }
}


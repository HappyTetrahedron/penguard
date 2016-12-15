package verteiltesysteme.penguard.guardianservice;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

    PenguinAdapter(Context context, List<Penguin> penguinList) {
        super(context, R.layout.list_penguins, penguinList);
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
            icon.setImageResource(R.drawable.icon);

            penguinName.setText(penguin.getName());

            if (penguinName != null) {

                if (penguin.isSeen()){ //seen by myself
                    //icon.setBackgroundColor(Color.GREEN);
                    icon.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                    statusTV.setText("is seen by you");
                }else if (penguin.isSeenByAnyone() && !penguin.isSeen()){
                    statusTV.setText("seen by someone else");
                    icon.setImageTintList(ColorStateList.valueOf(Color.BLUE));
                }else {
                    statusTV.setText("not seen by anyone");
                   //icon.setBackgroundColor(Color.RED);
                    icon.setImageTintList(ColorStateList.valueOf(Color.RED));

                }
            }
        }

        return convertView;
    }
}


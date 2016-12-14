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

class PenguinAdapter extends ArrayAdapter<Penguin> {

    private int layoutResource;

    PenguinAdapter(Context context, int layoutResource, List<Penguin> penguinList) {
        super(context, layoutResource, penguinList);
        this.layoutResource = layoutResource;
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
            TextView titleTextView = (TextView) convertView.findViewById(R.id.penguinName);

            if (titleTextView != null) {
                titleTextView.setText(penguin.getName() + " " + penguin.isSeen());
            }
        }

        return convertView;
    }
}


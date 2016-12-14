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
            TextView titleTextView = (TextView) convertView.findViewById(R.id.penguinName);

            if (titleTextView != null) {
                titleTextView.setText(penguin.getName() + " " + penguin.isSeen());
            }
        }

        return convertView;
    }
}


package verteiltesysteme.penguard;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDevicesAdapter extends ArrayAdapter<BluetoothDevice> {

    private int layoutResource;

    public BluetoothDevicesAdapter(Context context, int layoutResource, List<BluetoothDevice> deviceList) {
        super(context, layoutResource, deviceList);
        this.layoutResource = layoutResource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(layoutResource, null);
        }

        BluetoothDevice device = getItem(position);

        if (device != null) {
            TextView deviceNameTextView = (TextView) convertView.findViewById((android.R.id.text1));

            if (deviceNameTextView != null) {
                deviceNameTextView.setText(device.getName() + " " + device.getAddress());
            }
        }

        return convertView;
    }

    private void debug(String msg) {
        Log.d("BluetoothDevicesAdapter", msg);
    }
}


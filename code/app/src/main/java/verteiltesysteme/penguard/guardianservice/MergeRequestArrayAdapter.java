package verteiltesysteme.penguard.guardianservice;

import android.content.Context;
import android.widget.TextView;

import java.util.ArrayList;

import verteiltesysteme.penguard.protobuf.PenguardProto;

/**
 * Created by seamaster on 06.12.16.
 */

public class MergeRequestArrayAdapter extends GenericArrayAdapter<PenguardProto.PGPMessage> {

    public MergeRequestArrayAdapter(Context context, ArrayList<PenguardProto.PGPMessage> objects) {
        super(context, objects);
    }

    @Override
    public void drawText(TextView textView, PenguardProto.PGPMessage object) {
        textView.setText(object.getName());
    }
}

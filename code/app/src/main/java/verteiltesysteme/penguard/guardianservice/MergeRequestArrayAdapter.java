package verteiltesysteme.penguard.guardianservice;

import android.content.Context;
import android.widget.TextView;

import java.util.ArrayList;

import verteiltesysteme.penguard.protobuf.PenguardProto;


public class MergeRequestArrayAdapter extends GenericArrayAdapter<PenguardProto.PGPMessage> {

    public MergeRequestArrayAdapter(Context context, ArrayList<PenguardProto.PGPMessage> objects) {
        super(context, objects);
    }

    @Override
    public void drawText(TextView textView, PenguardProto.PGPMessage object) {
        textView.setText(object.getMergeReq().getName());
    }
}

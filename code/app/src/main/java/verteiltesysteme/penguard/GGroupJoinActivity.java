package verteiltesysteme.penguard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

//TODO

public class GGroupJoinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ggroup_join);
    }

    private void debug(String msg) {
        Log.d("GGroupJoin", msg);
    }
}

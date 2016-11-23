package verteiltesysteme.penguard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//here we search for bluetooth devices and the guard can pick a penguin to guard and then go on to the GGuardActivity

public class GPenguinSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpenguin_search);
    }
}

package verteiltesysteme.penguard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button b1, b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button)findViewById(R.id.button); //UDP will later be called the i'm a guard
        b2 = (Button)findViewById(R.id.button2); //bluetooth //will later be called i'm a penguin

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GLoginActivity.class);
                startActivity(intent);

            }
        });
    }

    private void debug(String msg) {
        Log.d("Penguard_main", msg);
    }
}

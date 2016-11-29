package verteiltesysteme.penguard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import verteiltesysteme.penguard.lowLevelNetworking.UDPTesting;

public class MainActivity extends AppCompatActivity {

    Button b1, b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button)findViewById(R.id.button); //UDP will later be called the i'm a guard
        b2 = (Button)findViewById(R.id.button2); //bluetooth //will later be called i'm a penguin

        /* Not sure if I'm supposed to but, put I put my UDPTesting activity here, so that when I click the
         * button I can test whether my UDP implementation works. If anyone needs the button for something else,
         * feel free to just remove it. --Nils
         */
        b1.setOnClickListener(new View.OnClickListener() { @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), UDPTesting.class);
            startActivity(intent);

        }
        });

        b2.setOnClickListener(new View.OnClickListener() { @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GLoginActivity.class);
                startActivity(intent);

            }
        });
    }
}

package verteiltesysteme.penguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import verteiltesysteme.penguard.guardianservice.GuardService;

public class MainActivity extends PenguardActivity {

    Button b1, b2;

    // "May contain traces of penguins." -Aline

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not display MainActivity if GuardService is on
        if (isMyServiceRunning(GuardService.class)) {
            Intent intent = new Intent(this, GGuardActivity.class);
            // add flags to clear this MainActivity from the stack
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);

        b1 = (Button)findViewById(R.id.button); //i'm a guard
        b2 = (Button)findViewById(R.id.button2); //i'm a penguin


        b2.setOnClickListener(new View.OnClickListener() { @Override
        public void onClick(View view) {
            /*Intent intent = new Intent(view.getContext(), GGroupJoinActivity.class);
            startActivity(intent);*/
            toast("Being a penguin is not implemented yet...sorry"); //TODO #60

        }
        });

        b1.setOnClickListener(new View.OnClickListener() { @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(view.getContext(), GuardService.class);
                startService(serviceIntent);
                Intent intent = new Intent(view.getContext(), GLoginActivity.class);
                startActivity(intent);

            }
        });

    }

    protected void onDestroy() {
        super.onDestroy();
    }
}

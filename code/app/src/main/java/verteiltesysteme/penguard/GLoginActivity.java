package verteiltesysteme.penguard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//this activity is used for login in the guard. It is called by the main activity. It recieves an empty intent

public class GLoginActivity extends AppCompatActivity {

    Button joinB;
    EditText usernameET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glogin);

        //get the UI elements
        joinB = (Button)findViewById(R.id.button3);
        usernameET = (EditText)findViewById(R.id.editText);

        //the onclick listener is only set like this for the purpose of easier implementing the bluetooth stuff without having to worry about networking
        //TODO the code below has to be adapted to contact the server etc once the PGP is estblished
        joinB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GJoinActivity.class);
                startActivity(intent);
            }
        });
    }
}

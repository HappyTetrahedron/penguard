package verteiltesysteme.penguard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GPenguinNameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpenguin_name);
        TextView setName = (TextView) findViewById(R.id.submitButtonPenguinname);
        setName.setText("How do you want to name your penguin?");


    }

    protected void submit(View view){
        EditText newName = (EditText) findViewById(R.id.newPenguinName);
        String newPenguinName = newName.getText().toString();

        if (!newPenguinName.equals("")) {
            Intent resultData = new Intent();
            resultData.putExtra("newName", newPenguinName);
            setResult(Activity.RESULT_OK, resultData);

                    Context context = getApplicationContext();
                    CharSequence text = "Penguin was successfully named";
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, text, duration);

            finish();
        } else{
                    Context context = getApplicationContext();
                    CharSequence text = "Please choose a name for your penguin";
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, text, duration);
        }
    }
}

package verteiltesysteme.penguard;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GPenguinNameActivity extends AppCompatActivity {
    private final String TAG = "##GPenguinNameAct##";
    private  BluetoothDevice device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpenguin_name);
        Log.d(TAG, "onCreate: entered nameingphase");
        TextView setName = (TextView) findViewById(R.id.textView2);
        setName.setText("How do you want to name your penguin?");
        Intent intent = getIntent();
        device = intent.getParcelableExtra("device");
    }

    public void submit(View view){
        Log.d(TAG, "submit: button was clicked");
        EditText newName = (EditText) findViewById(R.id.newPenguinName);
        String newPenguinName = newName.getText().toString();

        if (!newPenguinName.equals("")) {
            Log.d(TAG, "submit: textedit was not empty");
            Intent resultData = new Intent();
            resultData.putExtra("device", device);
            resultData.putExtra("newName", newPenguinName);
            Log.d(TAG, "submit: setup result");
            setResult(Activity.RESULT_OK, resultData);

            Context context = getApplicationContext();
            CharSequence text = "Penguin was successfully named";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();

            finish();
        } else{
                    Context context = getApplicationContext();
                    CharSequence text = "Please choose a name for your penguin";
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, text, duration).show();
        }
    }
}

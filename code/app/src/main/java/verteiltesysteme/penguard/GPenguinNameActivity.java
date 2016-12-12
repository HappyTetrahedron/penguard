package verteiltesysteme.penguard;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import verteiltesysteme.penguard.guardianservice.GuardService;

public class GPenguinNameActivity extends AppCompatActivity {
    private  BluetoothDevice device;
    final static String DEVICE_KEY = "device";
    final static String NEW_NAME_EXTRA = "newName";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpenguin_name);
        debug("onCreate: entered nameingphase");
        Intent intent = getIntent();
        device = intent.getParcelableExtra(DEVICE_KEY);
    }

    public void submit(View view){
        EditText newName = (EditText) findViewById(R.id.newPenguinName);
        String newPenguinName = newName.getText().toString();

        if (!newPenguinName.equals("")) {
            Intent resultData = new Intent();
            resultData.putExtra(DEVICE_KEY, device);
            resultData.putExtra(NEW_NAME_EXTRA, newPenguinName);
            debug( "submit: setup result");
            setResult(Activity.RESULT_OK, resultData);
            toast(getString(R.string.penguinName_suc));

            finish();
        } else{
            toast(getString(R.string.penguinName_empty));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        getMenuInflater().inflate(R.menu.menu_howto, menu);
        getMenuInflater().inflate(R.menu.menu_endservice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_howto:
                Intent intent1 = new Intent(this, HowToActivity.class);
                startActivity(intent1);
                return true;
            case R.id.menu_endService:
                killService();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void killService(){
        Intent backToMainIntent = new Intent(this, MainActivity.class);
        // clear the backstack when transitioning to main activity
        backToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent stopServiceIntent = new Intent(this, GuardService.class);

        stopService(stopServiceIntent);
        startActivity(backToMainIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    private void debug(String msg) {
        Log.d("PenguinNameActivity", msg);
    }
}

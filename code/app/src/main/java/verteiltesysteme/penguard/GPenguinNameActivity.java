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

public class GPenguinNameActivity extends AppCompatActivity {
    private  BluetoothDevice device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpenguin_name);
        debug("onCreate: entered nameingphase");
        Intent intent = getIntent();
        device = intent.getParcelableExtra("device");
    }

    public void submit(View view){
        EditText newName = (EditText) findViewById(R.id.newPenguinName);
        String newPenguinName = newName.getText().toString();

        if (!newPenguinName.equals("")) {
            Intent resultData = new Intent();
            resultData.putExtra("device", device);
            resultData.putExtra("newName", newPenguinName);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

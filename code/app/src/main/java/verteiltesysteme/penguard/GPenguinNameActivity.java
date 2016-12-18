package verteiltesysteme.penguard;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class GPenguinNameActivity extends PenguardActivity {
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
    protected void onDestroy() {
        super.onDestroy();
    }

}

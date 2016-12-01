package verteiltesysteme.penguard;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.GuardianServiceConnection;
import verteiltesysteme.penguard.guardianservice.Penguin;

import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;

//here we search for bluetooth devices and the guard can pick a penguin to guard and then go on to the GGuardActivity

public class GPenguinSearchActivity extends AppCompatActivity {
    static final int SCAN_PERIOD = 10000; // scan period in ms
    static final String EXTRA_DEVICE = "bt_device"; //aka the penguin
    static final int REQUEST_ENABLE_BT = 1; // request code for bluetooth enabling
    static final int PERMISSION_REQUEST_FINE_LOCATION = 2; // request code for location permission

    ArrayList<BluetoothDevice> scanResultsList = new ArrayList<>();
    BroadcastReceiver bcr;
    ArrayAdapter<BluetoothDevice> scanResultsAdapter;
    ScanCallback scanCallback;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    Handler handler;
    Button restartScanButton;
    ScanSettings scanSettings;
    List<ScanFilter> scanFilters;

    GuardianServiceConnection serviceConnection = new GuardianServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpenguin_search);

        // bluetooth LE scan settings
        scanSettings = new ScanSettings.Builder().build();

        // Filter for LE scan
        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
        scanFilterBuilder.setDeviceName("Smart Humigadget");
        ScanFilter filter = scanFilterBuilder.build();
        scanFilters = new ArrayList<>();
        //scanFilters.add(filter);

        //bind the service
        Intent intent = new Intent(this, GuardService.class);

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        handler = new Handler();
        restartScanButton = (Button) findViewById(R.id.restartScanButton);
        restartScanButton.setText(getText(R.string.scan));

        bcr = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    debug("Found a device");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    addDevice(device);
                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter intentfilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bcr, intentfilter); // Don't forget to unregister during onDestroy

        // bluetooth LE scan callback that is used for getting scan results
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                debug("Device found: " + result.getDevice().getName());
                if (result.getDevice() == null) return;
                addDevice(result.getDevice());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult res : results) {
                    addDevice(res.getDevice());
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                toast(getResources().getString(R.string.failBTScan));
                debug("Scan failed: " + errorCode);
            }
        };

        //initialize bluetooth adapter
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //initialize list view for scan results
        ListView scanResults = (ListView) findViewById(R.id.scanResultlv);
        scanResultsAdapter = new BluetoothDevicesAdapter(this, R.layout.support_simple_spinner_dropdown_item, scanResultsList);
        scanResults.setAdapter(scanResultsAdapter);

        scanResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = (BluetoothDevice)parent.getItemAtPosition(position);
                serviceConnection.addPenguin(new Penguin(device, "Penguin " + device.getName())); //TODO ask user for name
                bluetoothScan(false); //stop ongoing scan
                Intent intent = new Intent(parent.getContext(), GGuardActivity.class);
                startActivity(intent);
            }
        });

        //request permission for fine location first if SDK is marshmallow or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
        else { //lollipop or lower; permission request not needed, scan ahead
            turnOnBluetoothAndScan();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bcr);
        unbindService(serviceConnection);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                debug("Location permission granted");
                //We now have permission, so let's scan for bluetooth devices
                turnOnBluetoothAndScan();
            }
            else {
               toast(getString(R.string.permissionDeniedBTScan));
                restartScanButton.setEnabled(false);
            }
        }
    }

    private void turnOnBluetoothAndScan() {

        //test whether bluetooth is enabled, enable if not
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }
        else {
            //bluetooth already on; scan ahead
            bluetoothScan(true);
        }

    }

    private void bluetoothScan(boolean enable) {
        if (enable) {
            // use a handler to stop the scan after SCAN_PERIOD ms
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopBluetoothScan();
                }
            }, SCAN_PERIOD);

            startBluetoothScan();

        }
        else { // enable is false
            stopBluetoothScan();
        }

    }

    private void startBluetoothScan() {
        debug("Started scan");
        //this does the LE scan
        if (bluetoothAdapter.getBluetoothLeScanner() != null) {
            bluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters,scanSettings,scanCallback);
            restartScanButton.setEnabled(false); //Cannot rescan while scan is running
            restartScanButton.setText(getText(R.string.scanningBTScan));
            toast(getString(R.string.scanningBTScan));
        }
        else {
            debug("Could not get LeScanner");
        }

        //this does the regular bt scan
        bluetoothAdapter.startDiscovery();
    }

    private void stopBluetoothScan() {
        debug("Stopped scan");
        //stop LE scan
        if (bluetoothAdapter.getBluetoothLeScanner() != null){
            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        }
        debug(bluetoothAdapter.getBluetoothLeScanner().toString());
        restartScanButton.setEnabled(true);
        restartScanButton.setText(getText(R.string.scan));
        if (scanResultsList.size() > 0) {
            toast(getString(R.string.stopBTScan));
        }
        else { // no results found
            toast(getString(R.string.noResultBTScan));
        }

        //stopping normal bt scan
        bluetoothAdapter.cancelDiscovery();

    }


    public void scanButtonClicked (View view) {
        if (view == restartScanButton) {
            //"Restart Scan" button was clicked, flush list and start a scan
            scanResultsList.clear();
            scanResultsAdapter.notifyDataSetChanged();
            turnOnBluetoothAndScan();
        }
    }

    private void addDevice(BluetoothDevice device){
        if (!scanResultsList.contains(device)){
            scanResultsList.add(device);
            scanResultsAdapter.notifyDataSetChanged();
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


    private void debug(String msg) {
        Log.d("GPenguinSearch", msg);
    }
    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

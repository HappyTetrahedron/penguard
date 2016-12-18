package verteiltesysteme.penguard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import verteiltesysteme.penguard.guardianservice.GuardService;
import verteiltesysteme.penguard.guardianservice.Penguin;

public class GGuardActivity extends StatusToolbarActivity implements View.OnClickListener {

    ListView penguinList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gguard);
        setUpToolbar();

        penguinList = (ListView) findViewById(R.id.penguinListView);

        //bind the service
        Intent intent = new Intent(this, GuardService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        penguinList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Penguin penguin = (Penguin) parent.getItemAtPosition(position);
                Intent intent = new Intent(GGuardActivity.this, GPenguinDetailActivity.class);
                intent.putExtra(GPenguinDetailActivity.EXTRA_PENGUIN_MAC, penguin.getAddress());
                startActivity(intent);
            }
        });

        TextView penguinListEmptyText = (TextView) findViewById(R.id.empty);
        penguinList.setEmptyView(penguinListEmptyText);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

    }

    @Override
    void updateState() {
        if (serviceConnection != null && serviceConnection.isConnected()) {
            if (penguinList.getAdapter() == null) {
                serviceConnection.subscribeListViewToPenguinAdapter(penguinList);
            }
            setButtonVisible(4, !serviceConnection.isRegistered());
            ((ArrayAdapter<Penguin>) penguinList.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    int getCurrentIconId() {
        return 1;
    }

    @Override
    int getMenuLayoutResource() {
        return R.menu.toolbar;
    }

    @Override
    Toolbar.OnMenuItemClickListener getOnMenuItemClickListener() {
        return new MainToolbarOnMenuItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceConnection != null && serviceConnection.isConnected()) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, GPenguinSearchActivity.class);
        startActivity(intent);
    }

}

package verteiltesysteme.penguard;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public abstract class ListOverviewActivity extends PenguardActivity implements Toolbar.OnMenuItemClickListener{

    private static final int UPDATE_DELAY = 500;

    Toolbar toolbar;

    Handler handler;

    Runnable updateTask;

    boolean paused = false;

    private int currentIconId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();

        updateTask = new Runnable() {
            @Override
            public void run() {
                updateState();
                if (!paused) handler.postDelayed(this, UPDATE_DELAY);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        handler.post(updateTask);
        resetTint();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    protected void setCurrentIcon(int currentIconId) {
        this.currentIconId = currentIconId;
    }

    protected void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.inflateMenu(R.menu.toolbar);
        toolbar.getMenu().getItem(currentIconId).setChecked(true);
        toolbar.getMenu().getItem(currentIconId).setEnabled(false);
        resetTint();

        setupEvenlyDistributedToolbar();
    }

        private void setupEvenlyDistributedToolbar() {
        // Use Display metrics to get Screen Dimensions
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        // Add 10 spacing on either side of the toolbar
        toolbar.setContentInsetsAbsolute(10, 10);

        // Get the ChildCount of your Toolbar, this should only be 1
        int childCount = toolbar.getChildCount();
        // Get the Screen Width in pixels
        int screenWidth = metrics.widthPixels;

        // Create the Toolbar Params based on the screenWidth
        Toolbar.LayoutParams toolbarParams = new Toolbar.LayoutParams(screenWidth, Toolbar.LayoutParams.WRAP_CONTENT);

        // Loop through the child Items
        for (int i = 0; i < childCount; i++) {
            // Get the item at the current index
            View childView = toolbar.getChildAt(i);
            // If its a ViewGroup
            if (childView instanceof ViewGroup) {
                // Set its layout params
                childView.setLayoutParams(toolbarParams);
                // Get the child count of this view group, and compute the item widths based on this count & screen size
                int innerChildCount = ((ViewGroup) childView).getChildCount();
                int itemWidth = (screenWidth / innerChildCount);
                // Create layout params for the ActionMenuView
                ActionMenuView.LayoutParams params = new ActionMenuView.LayoutParams(itemWidth, Toolbar.LayoutParams.WRAP_CONTENT);
                // Loop through the children
                for (int j = 0; j < innerChildCount; j++) {
                    View grandChild = ((ViewGroup) childView).getChildAt(j);
                    if (grandChild instanceof ActionMenuItemView) {
                        // set the layout parameters on each View
                        grandChild.setLayoutParams(params);
                    }
                }
            }
        }
    }

    void resetTint() {
        for (int i = 0; i < toolbar.getMenu().size(); i++) {
            toolbar.getMenu().getItem(i).getIcon().setTintList(ColorStateList.valueOf(0xffffffff));
        }
        toolbar.getMenu().getItem(currentIconId).getIcon().setTintList(ColorStateList.valueOf(0xffa8a8a8));
    }


    protected void updateLoginB(){
        if (serviceConnection.isRegistered()){
            toolbar.getMenu().getItem(4).setVisible(false);
        }else {
            toolbar.getMenu().getItem(4).setVisible(true);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_add_penguin) { // add new penguin
            Intent intent = new Intent(this, GPenguinSearchActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_join_group) { // join another group
            Intent intent = new Intent(this, GGroupJoinActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_group_overview) { // stop guardian service
            Intent intent = new Intent(this, GGroupOverviewActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_penguin_overview) { // stop guardian service
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_login) { // stop guardian service
            Intent intent = new Intent(this, GLoginActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    abstract void updateState();
}

package verteiltesysteme.penguard;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

class MainToolbarOnMenuItemClickListener implements Toolbar.OnMenuItemClickListener {

    private Activity activity;

    MainToolbarOnMenuItemClickListener(Activity a) {
        activity = a;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_add_penguin) { // add new penguin
            Intent intent = new Intent(activity, GPenguinSearchActivity.class);
            activity.startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_join_group) { // join another group
            Intent intent = new Intent(activity, GGroupJoinActivity.class);
            activity.startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_group_overview) { // stop guardian service
            Intent intent = new Intent(activity, GGroupOverviewActivity.class);
            activity.startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_penguin_overview) { // stop guardian service
            activity.finish();
            return true;
        }
        if (item.getItemId() == R.id.action_login) { // stop guardian service
            Intent intent = new Intent(activity, GLoginActivity.class);
            activity.startActivity(intent);
            return true;
        }
        return false;
    }

}

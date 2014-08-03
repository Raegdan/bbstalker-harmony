package org.raegdan.bbstalkerharmony;

import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, TextView.OnEditorActionListener {

    private EditText etABSQuery;
    private MenuItem miABS;
    private View vABS;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the drawer.
        mTitle = getTitle();
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.d("oNDIS", String.valueOf(position));
        // update the main content by replacing fragments
        FragmentManager fm = getFragmentManager();
    /*    fragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance(position + 1))
                .commit();
                */
        switch (position) {
            case GlobalConstants.PAGE_HOME:
                fm.beginTransaction().replace(R.id.container, HomeFragment.newInstance()).commit();
                break;
            case GlobalConstants.PAGE_ALL_FIGURES:
                //TODO
                fm.beginTransaction().replace(R.id.container, FiguresListFragment.newInstance(GlobalConstants.QUERY_ALL_FIGURES, "")).commit();
                break;
            case GlobalConstants.PAGE_ALL_WAVES:
                break;
            case GlobalConstants.PAGE_COLLECTION:
                break;
            case GlobalConstants.PAGE_WISHLIST:
                break;
            case GlobalConstants.PAGE_DETECTOR:
                break;
            case GlobalConstants.PAGE_HELP:
                break;
            case GlobalConstants.PAGE_CONFIG:
                break;
        }

    }

    public void onSectionAttached(int page, int mode, String comment) {
        switch (page) {
            case GlobalConstants.PAGE_HOME:
                mTitle = getString(R.string.app_name);
                break;
            case GlobalConstants.PAGE_ALL_FIGURES:
                mTitle = getString(R.string.nav_all_figures);
                break;
            case GlobalConstants.PAGE_ALL_WAVES:
                mTitle = getString(R.string.nav_all_waves);
                break;
            case GlobalConstants.PAGE_COLLECTION:
                mTitle = getString(R.string.nav_collection);
                break;
            case GlobalConstants.PAGE_WISHLIST:
                mTitle = getString(R.string.nav_wishlist);
                break;
            case GlobalConstants.PAGE_DETECTOR:
                mTitle = getString(R.string.nav_detector);
                break;
            case GlobalConstants.PAGE_DETECTOR_RESULTS:
               //TODO Visualize detector query in action bar?
               //mTitle =
               break;
            case GlobalConstants.PAGE_HELP:
                mTitle = getString(R.string.nav_help);
                break;
            case GlobalConstants.PAGE_CONFIG:
                mTitle = getString(R.string.nav_config);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();

        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);

            miABS = menu.findItem(R.id.action_search);
            vABS = miABS.getActionView();
            etABSQuery = (EditText) vABS.findViewById(R.id.etABSQuery);

            etABSQuery.setOnEditorActionListener(this);

            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_search: {
                etABSQuery.post(new Runnable() {
                    @Override
                    public void run() {
                        etABSQuery.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(etABSQuery, InputMethodManager.SHOW_IMPLICIT);
                    }
                });

                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.etABSQuery: {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //TODO Perform query
                }

                break;
            }
        }
        return false;
    }
}

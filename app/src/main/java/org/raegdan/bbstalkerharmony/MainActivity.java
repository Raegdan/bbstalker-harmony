package org.raegdan.bbstalkerharmony;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, TextView.OnEditorActionListener {

    ProgressDialog mDialog;

    private EditText etABSQuery;
    private MenuItem miABS;
    private View vABS;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the drawer.
        mTitle = getTitle();
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        if (((BBSHApplication) getApplication()).dbLoaded) {
            continueInit();
        } else {
            // Dialog init
            mDialog = new ProgressDialog(this);

            // Start async database loading
            new DBLoader().execute(this);
        }
    }

    //////////////////////////////////////////////
    // AsyncTask for loading global DB from JSON
    //////////////////////////////////////////////
    protected class DBLoader extends AsyncTask<Activity, Integer, Void> {
        @Override
        protected void onPreExecute () {
            mDialog.setCancelable(false);
            mDialog.setMessage(getString(R.string.loading));
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Activity... arg0) {
            ((BBSHApplication) getApplication()).dbLoaded = ((BBSHApplication) arg0[0].getApplication()).loadDB(arg0[0]);
            return null;
        }

        @Override
        protected void onPostExecute (Void arg0) {
            mDialog.dismiss();
            continueInit();
        }
    }

    ////////////////////////////////////////////////////
    // Continues activity init after DBLoader finishes
    ////////////////////////////////////////////////////
    protected void continueInit() {
        if (!((BBSHApplication) getApplication()).dbLoaded) {
            Toast.makeText(getApplicationContext(), getString(R.string.json_db_err), Toast.LENGTH_LONG).show();
            return;
        }

        if (((BBSHApplication) getApplication()).currentFragment == null) {
            ((BBSHApplication) getApplication()).currentFragment = HomeFragment.newInstance();
        }

        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.container, ((BBSHApplication) getApplication()).currentFragment, "").commit();


        // showWhatsNew();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case GlobalConstants.PAGE_HOME:
                changeFragment(HomeFragment.newInstance());
                break;
            case GlobalConstants.PAGE_ALL_FIGURES:
                changeFragment(FiguresListFragment.newInstance(GlobalConstants.QUERY_ALL_FIGURES, ""));
                break;
            case GlobalConstants.PAGE_ALL_WAVES:
                changeFragment(WavesFragment.newInstance());
                break;
            case GlobalConstants.PAGE_COLLECTION:
                changeFragment(FiguresListFragment.newInstance(GlobalConstants.QUERY_COLLECTION, ""));
                break;
            case GlobalConstants.PAGE_WISHLIST:
                changeFragment(FiguresListFragment.newInstance(GlobalConstants.QUERY_WISHLIST, ""));
                break;
            case GlobalConstants.PAGE_DETECTOR:
                break;
            case GlobalConstants.PAGE_HELP:
                break;
            case GlobalConstants.PAGE_CONFIG:
                break;
        }
    }

    public void changeFragment(Fragment fragment) {
        ((BBSHApplication) getApplication()).currentFragment = fragment;
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.container, fragment, "").commit();
    }

    public void onFragmentReady(int page, int mode, String comment) {
        switch (page) {
            case GlobalConstants.PAGE_HOME:
                mTitle = getString(R.string.app_name);
                break;
            case GlobalConstants.PAGE_ALL_FIGURES:
                mTitle = getString(R.string.nav_all_figures) + " (" + comment + ")";
                break;
            case GlobalConstants.PAGE_ALL_WAVES:
                mTitle = getString(R.string.nav_all_waves);
                break;
            case GlobalConstants.PAGE_COLLECTION:
                mTitle = getString(R.string.nav_collection) + " (" + comment + ")";
                break;
            case GlobalConstants.PAGE_WISHLIST:
                mTitle = getString(R.string.nav_wishlist) + " (" + comment + ")";
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
            case GlobalConstants.PAGE_SEARCH:
                mTitle = String.format(getString(R.string.results_for), comment);
                break;
        }

        restoreActionBar();
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
                // Don't really understand the reason for Runnable here, but it's necessary to work correctly
                // Thank StackOverflow gurus
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
                    //Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

                    //Collapse search field
                    miABS.collapseActionView();

                    //Change fragment
                    changeFragment(FiguresListFragment.newInstance(GlobalConstants.QUERY_SEARCH, v.getText().toString()));
                }

                break;
            }
        }
        return false;
    }
}

package org.raegdan.bbstalkerharmony;

import android.app.Activity;
import android.app.ActionBar;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, TextView.OnEditorActionListener {

    // Controls
    Button btnMAQuery;
    Button btnMAWatchDB;
    Button btnMAWatchCollection;
    Button btnMAHelp;
    Button btnMAConfig;
    Button btnMAWatchWaves;
    Button btnMAWishlist;
    Button btnMADetector;

    EditText etMAQuery;
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
        // Don't init controls in case of DB loading failure.
        // Show toast and leave the form dead.
        if (!((BBSHApplication) getApplication()).dbLoaded) {
            Toast.makeText(getApplicationContext(), getString(R.string.json_db_err), Toast.LENGTH_LONG).show();
            return;
        }

        // Controls init
    /*    btnMAQuery = (Button) findViewById(R.id.btnMAQuery);
        btnMAWatchDB = (Button) findViewById(R.id.btnMAWatchDB);
        btnMAWatchCollection = (Button) findViewById(R.id.btnMAWatchCollection);
        btnMAHelp = (Button) findViewById(R.id.btnMAHelp);
        btnMAConfig = (Button) findViewById(R.id.btnMAConfig);
        btnMAWatchWaves = (Button) findViewById(R.id.btnMAWatchWaves);
        btnMAWishlist = (Button) findViewById(R.id.btnMAWishlist);
        btnMADetector = (Button) findViewById(R.id.btnMADetector);

        etMAQuery = (EditText) findViewById(R.id.etMAQuery);

        btnMAQuery.setOnClickListener(this);
        btnMAWatchDB.setOnClickListener(this);
        btnMAWatchCollection.setOnClickListener(this);
        btnMAHelp.setOnClickListener(this);
        btnMAConfig.setOnClickListener(this);
        btnMAWatchWaves.setOnClickListener(this);
        btnMAWishlist.setOnClickListener(this);
        btnMADetector.setOnClickListener(this);

        etMAQuery.setOnClickListener(this);
        etMAQuery.setOnEditorActionListener(this);

        ShowWhatsNew(); */
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
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
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

                    miABS.collapseActionView();
                    FragmentManager fm = getFragmentManager();
                    fm.beginTransaction().replace(R.id.container, FiguresListFragment.newInstance(GlobalConstants.QUERY_SEARCH, v.getText().toString())).commit();
                }

                break;
            }
        }
        return false;
    }
}

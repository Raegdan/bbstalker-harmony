package org.raegdan.bbstalkerharmony;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.HashMap;

public class WavesFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    Activity parentActivity;
    View rootView;

    ListView lvFWWavesList;
    DBList dblist;
    BlindbagDB database;
    ProgressDialog mDialog;

    @Override
    public void onStart() {
        super.onStart();

        lvFWWavesList = (ListView) rootView.findViewById(R.id.lvFWWavesList);
        lvFWWavesList.setOnItemClickListener(this);
        lvFWWavesList.setOnItemLongClickListener(this);

        try {
            database = ((BBSHApplication) parentActivity.getApplication()).getDB(parentActivity);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        mDialog = new ProgressDialog(parentActivity);
        mDialog.setMessage(getString(R.string.loading));
        mDialog.setCancelable(false);
        mDialog.show();

        new QueryDatabase().execute(parentActivity);
    }

    protected class QueryDatabase extends AsyncTask<Context, Integer, HashMap<String, Object>>
    {
        Context context;

        protected DBList prepareDBList (BlindbagDB database, Context context)
        {
            DBList dl = new DBList();
            dl.fields = new String[] {"name", "misc", "img1"};
            dl.views = new int[] {R.id.tvWLIMain, R.id.tvWLIMisc, R.id.ivWLIWaveImage};

            for (int i = 0; i < database.waves.size(); i++)
            {
                Integer wavepic = context.getResources().getIdentifier("w" + database.waves.get(i).waveid, "drawable", context.getPackageName());

                HashMap<String, Object> hmDBList = new HashMap<String, Object>();

                if (Integer.parseInt(database.waves.get(i).waveid) <= 100)
                {
                    hmDBList.put("name", getString(R.string.wave) + database.waves.get(i).waveid + " (" + database.waves.get(i).year + ")");
                    hmDBList.put("misc", getString(R.string.format) + database.waves.get(i).format);
                } else {
                    hmDBList.put("name", database.waves.get(i).name);
                    hmDBList.put("misc", getString(R.string.collection_set));
                }
                hmDBList.put("waveid", database.waves.get(i).waveid);
                hmDBList.put("img1", wavepic);
                dl.data.add(hmDBList);
            }

            return dl;
        }

        @Override
        protected HashMap<String, Object> doInBackground(Context... arg0) {
            context = arg0[0];

            HashMap<String, Object> out = new HashMap<String, Object>();

//            out.put("error", false);
            out.put("database", database);
            out.put("dblist", prepareDBList(database, context));
            return out;
        }

        @Override
        protected void onPostExecute (HashMap<String, Object> result)
        {
//            if (!((Boolean) result.get("error")))
//            {
                dbQueryFinished((BlindbagDB) result.get("database"), (DBList) result.get("dblist"));
//            } else {
//                dbQueryFinished(null, null);
//            }
        }
    }

    protected void dbQueryFinished(BlindbagDB db, DBList dl)
    {
        dblist = dl;
        database = db;

        SimpleAdapter saDBList = new SimpleAdapter(parentActivity, dblist.data, R.layout.waves_list_item, dblist.fields, dblist.views);
        lvFWWavesList.setAdapter(saDBList);

        mDialog.dismiss();
    }

    public static WavesFragment newInstance() {
        return new WavesFragment();
    }

    public WavesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_waves, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        parentActivity = activity;

        ((MainActivity) activity).onFragmentReady(GlobalConstants.PAGE_ALL_WAVES, GlobalConstants.QUERY_NO_QUERY, "");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }
}
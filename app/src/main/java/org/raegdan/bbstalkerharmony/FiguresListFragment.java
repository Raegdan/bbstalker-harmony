package org.raegdan.bbstalkerharmony;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

//import org.raegdan.bbstalker.MyLocation.LocationResult;

import android.text.format.Time;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FiguresListFragment extends Fragment implements OnItemClickListener {

    BlindbagDB database;
    DBList dblist;
    String CurrentBBUniqID = "";
    Integer CurrentDBListID = 0;
    String query;
    int mode;
    SharedPreferences sp;

    ListView lvDBList;
    SimpleAdapter saDBList;
    TextView tvDBHeader;

    PopupWindow pw;
    View vPWBBInfo;
    RelativeLayout rlPWBBInfo;
    TextView tvPWBBInfoName;
    TextView tvPWBBInfoMisc;
    ImageView ivPWBBInfoPonyPic;
    ImageButton ibPWBBWiki;
    ImageButton ibPWBBShareCommon;
    ImageButton ibPWBBCart;
    ImageButton ibPWBBUncart;
    ImageButton ibPWBBWish;
    EditText etPWBBShareShopname;

    ProgressDialog mDialog;
    HashMap<String, Object> locationCache;

    Activity parentActivity;
    View rootView;

    @Override
    public void onStart() {
        super.onStart();

        // Init
        sp = parentActivity.getSharedPreferences(parentActivity.getPackageName(), Context.MODE_PRIVATE);

        mDialog = new ProgressDialog(parentActivity);
        mDialog.setCancelable(false);

        // tvDBHeader = (TextView) getView().findViewById(R.id.tvDBHeader);

        lvDBList = (ListView) rootView.findViewById(R.id.lvFFL);
        lvDBList.setOnItemClickListener(this);

        locationCache = new HashMap<String, Object>();
        locationCache.put("time", new Time().toMillis(true));
        locationCache.put("location", String.valueOf(""));
        locationCache.put("timeout", 300000);

        try {
            database = ((BBSHApplication) parentActivity.getApplication()).getDB(parentActivity);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        dblist = new DBList();

        // Query
        query = getArguments().getString(GlobalConstants.ARG_QUERY);
        mode = getArguments().getInt(GlobalConstants.ARG_MODE, GlobalConstants.QUERY_NO_QUERY);

        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("mode", mode);
        hm.put("query", query);
        hm.put("context", parentActivity);

        mDialog.setMessage(getString(R.string.looking_up));
        mDialog.show();

        new QueryDatabase().execute(hm);
    }

    protected class QueryDatabase extends AsyncTask<HashMap<String, Object>, Integer, String> {
        protected void prepareDBList(BlindbagDB database, Context context) {
            dblist.fields = new String[] {"name", "misc", "img1", "star_img"};
            dblist.views = new int[] {R.id.tvFLIMain, R.id.tvFLIMisc, R.id.ivFLIWaveImage, R.id.ivFLIStar};
            dblist.total_count = 0;

            for (int i = 0; i < database.blindbags.size(); i++) {
                if (database.blindbags.get(i).priority == 0) {
                    continue;
                }

                HashMap<String, Object> hmDBList = new HashMap<String, Object>();

                String misctext = "";

                if (Integer.parseInt(database.blindbags.get(i).waveid) <= 100) {
                    String blindbagIDsSlash = "";
                    for (int j = 0; j < database.blindbags.get(i).bbids.size(); j++) {
                        blindbagIDsSlash += database.blindbags.get(i).bbids.get(j);
                        if (j < database.blindbags.get(i).bbids.size() - 1) {
                            blindbagIDsSlash += " / ";
                        }
                    }

                    hmDBList.put("bbids_slash", blindbagIDsSlash);
                    misctext = context.getString(R.string.code) + blindbagIDsSlash;
                } else {
                    misctext = context.getString(R.string.set) + database.getWaveByWaveID(database.blindbags.get(i).waveid).name;
                    hmDBList.put("wave_name", database.getWaveByWaveID(database.blindbags.get(i).waveid).name);
                }

                if (mode != GlobalConstants.QUERY_WISHLIST) {
                    misctext += ", " + context.getString(R.string.in_collection) + database.blindbags.get(i).count.toString();
                }

                hmDBList.put("misc", misctext);

                Integer wavepic = context.getResources().getIdentifier("w" + database.blindbags.get(i).waveid, "drawable", context.getPackageName());
                hmDBList.put("name", database.blindbags.get(i).name);
                hmDBList.put("img1", wavepic);
                hmDBList.put("uniqid", database.blindbags.get(i).uniqid);
                hmDBList.put("count_int", database.blindbags.get(i).count);
                hmDBList.put("waveid", database.blindbags.get(i).waveid);
                hmDBList.put("wanted", database.blindbags.get(i).wanted);
                if (database.blindbags.get(i).wanted) {
                    hmDBList.put("star_img", R.drawable.star_on);
                } else {
                    hmDBList.put("star_img", null);
                }
                dblist.total_count += database.blindbags.get(i).count;
                dblist.data.add(hmDBList);
            }
        }

        @Override
        protected String doInBackground(HashMap<String, Object>... arg0) {
            BlindbagDB db = new BlindbagDB();

            String titleMsg = "";
            int mode = (Integer) arg0[0].get("mode");
            String query = ((String) arg0[0].get("query"));
            Context context = (Context) arg0[0].get("context");

            switch (mode) {
                case GlobalConstants.QUERY_ALL_FIGURES: {
                    titleMsg = context.getString(R.string.all_db);
                    db = database;
                    break;
                }

                case GlobalConstants.QUERY_SEARCH: {
                    titleMsg = context.getString(R.string.results_for) + query + "»";
                    db = database.lookupDB(query, sp.getBoolean("smart_search", true));
                    break;
                }

                case GlobalConstants.QUERY_COLLECTION: {
                    titleMsg = context.getString(R.string.my_collection);
                    db = database.getCollection(context);
                    break;
                }

                case GlobalConstants.QUERY_WISHLIST: {
                    titleMsg = context.getString(R.string.wishlist);
                    db = database.getWishlist(context);
                    break;
                }

                case GlobalConstants.QUERY_WAVE: {
                    if (Integer.parseInt(query) <= 100) {
                        titleMsg = context.getString(R.string.wave) + query;
                    } else {
                        titleMsg = database.getWaveByWaveID(query).name;
                    }
                    db = database.getWaveBBs(query);
                    break;
                }

                case GlobalConstants.QUERY_DETECTOR: {
                    titleMsg = context.getString(R.string.detector);
                    db = database.reverseLookup(query);
                    break;
                }
            }

            prepareDBList(db, context);

            return titleMsg;
        }

        @Override
        protected void onPostExecute (String result) {
            dbQueryFinished(result);
        }
    }

    protected void dbQueryFinished(String TitleMsg) {
        /*switch (mode) {
            case GlobalConstants.QUERY_COLLECTION: {
                tvDBHeader.setText(TitleMsg + " (" + Integer.toString(dblist.total_count) + ")");
                break;
            }

            case GlobalConstants.QUERY_WISHLIST: {
                tvDBHeader.setText(TitleMsg + " (" + Integer.toString(dblist.data.size()) + ")");
                break;
            }

            default: {
                tvDBHeader.setText(TitleMsg);
            }
        }*/

        saDBList = new SimpleAdapter(parentActivity, dblist.data, R.layout.figures_list_item, dblist.fields, dblist.views);
        lvDBList.setAdapter(saDBList);

        mDialog.dismiss();

        /* if (dblist.data.size() == 0) {
            tvDBHeader.setText(TitleMsg + "\n\n" + getString(R.string.empty_list));
            return;
        } else if (dblist.data.size() == 1) {
            lvDBListItemClicked(0);
        } */
    }


    public static FiguresListFragment newInstance(int queryMode, String query) {
        FiguresListFragment fragment = new FiguresListFragment();
        Bundle args = new Bundle();
        args.putInt(GlobalConstants.ARG_MODE, queryMode);
        args.putString(GlobalConstants.ARG_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    public FiguresListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_figures_list, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        parentActivity = activity;

        int page = GlobalConstants.ERRORNEOUS_PAGE;
        String comment = "";

        switch (this.getArguments().getInt(GlobalConstants.ARG_MODE)) {
            case GlobalConstants.QUERY_SEARCH: {
                page = GlobalConstants.PAGE_SEARCH;
                comment = this.getArguments().getString(GlobalConstants.ARG_QUERY);
                break;
            }
            case GlobalConstants.QUERY_ALL_FIGURES: {
                page = GlobalConstants.PAGE_ALL_FIGURES;
                break;
            }
            case GlobalConstants.QUERY_WAVE: {
                page = GlobalConstants.PAGE_WAVE;
                //TODO comment = wave number
                break;
            }
            case GlobalConstants.QUERY_COLLECTION: {
                page = GlobalConstants.PAGE_COLLECTION;
                //TODO comment = collection size
                break;
            }
            case GlobalConstants.QUERY_WISHLIST: {
                page = GlobalConstants.PAGE_WISHLIST;
                //TODO comment = wishlist size
                break;
            }
            case GlobalConstants.QUERY_DETECTOR: {
                page = GlobalConstants.PAGE_DETECTOR_RESULTS;
                this.getArguments().getString(GlobalConstants.ARG_QUERY);
                break;
            }
        }

        ((MainActivity) activity).onSectionAttached(page, this.getArguments().getInt(GlobalConstants.ARG_MODE), comment);
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}

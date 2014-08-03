package org.raegdan.bbstalkerharmony;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FiguresListFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_figures_list, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

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
}

package org.raegdan.bbstalkerharmony;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    TextView tvFHGithubLink;
    TextView tvFHVersionValue;
    TextView tvFHVKLink;

    Activity parentActivity;
    View rootView;

    @Override
    public void onStart() {
        super.onStart();

        // Init controls
        tvFHGithubLink = (TextView) rootView.findViewById(R.id.tvFHGithubLink);
        tvFHVersionValue = (TextView) rootView.findViewById(R.id.tvFHVersionValue);
        tvFHVKLink = (TextView) rootView.findViewById(R.id.tvFHVKLink);

        // Make links clickable
        tvFHGithubLink.setMovementMethod(LinkMovementMethod.getInstance());
        tvFHVKLink.setMovementMethod(LinkMovementMethod.getInstance());

        // Display version from manifest
        try {
            tvFHVersionValue.setText(parentActivity.getPackageManager().getPackageInfo(parentActivity.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        parentActivity = activity;

        ((MainActivity) activity).onFragmentReady(GlobalConstants.PAGE_HOME, GlobalConstants.QUERY_NO_QUERY, "");
    }
}
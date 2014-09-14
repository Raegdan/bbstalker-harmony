package org.raegdan.bbstalkerharmony;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    TextView tvFHGo;
    TextView tvFHVersionValue;

    Activity parentActivity;
    View rootView;

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);

        //ViewGroup.LayoutParams lp = ivFHBanner.getLayoutParams();
        //int textSize = lp.width;// / BANNER_ORIGINAL_WIDTH) * TEXT_SCALING_Q;
        //Log.d("BBSH", String.valueOf(textSize));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //ViewGroup.LayoutParams lp = view.findViewById(R.id.ivFHBanner).getLayoutParams();
       // int textSize = view.findViewById(R.id.ivFHBanner).getWidth();// / BANNER_ORIGINAL_WIDTH) * TEXT_SCALING_Q;
       // Log.d("BBSH", String.valueOf(textSize));
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link android.app.Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();

        //ViewGroup.LayoutParams lp = ivFHBanner.getLayoutParams();
       // int textSize = ivFHBanner.get;// / BANNER_ORIGINAL_WIDTH) * TEXT_SCALING_Q;
        //Log.d("BBSH", String.valueOf(textSize));
    }

    @Override
    public void onStart() {
        super.onStart();

        tvFHGo = (TextView) rootView.findViewById(R.id.tvFHGo);
        tvFHVersionValue = (TextView) rootView.findViewById(R.id.tvFHVersionValue);

        tvFHGo.setMovementMethod(LinkMovementMethod.getInstance());
        try {
            tvFHVersionValue.setText(parentActivity.getPackageManager().getPackageInfo(parentActivity.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();

        return fragment;
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
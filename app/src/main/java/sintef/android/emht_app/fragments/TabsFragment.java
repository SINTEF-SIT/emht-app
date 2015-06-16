package sintef.android.emht_app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import sintef.android.emht_app.R;

/**
 * Created by iver on 16/06/15.
 */
public class TabsFragment extends Fragment implements TabHost.OnTabChangeListener {

    public static final String TAB_REGISTRATION = "registration";
    public static final String TAB_ACTIONS = "actions";
    public static final String TAB_ASSESSMENT = "assessment";
    private View mRoot;
    private TabHost mTabHost;
    private int mCurrentTab;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_tabs, null);
        mTabHost = (TabHost) mRoot.findViewById(android.R.id.tabhost);

        mTabHost.setup();
        mTabHost.addTab(newTab(TAB_REGISTRATION, R.string.tabRegistration, R.id.tabRegistration));

        return mRoot;
    }

    private TabHost.TabSpec newTab(String tag, int labelId, int tabContentId) {
        Log.w(TAG, "building tab: " + tag);

        View indicator = LayoutInflater.from(getActivity()).inflate(
                R.layout.tabs,
                (ViewGroup) mRoot.findViewById(android.R.id.tabs), false);
        ((TextView) indicator.findViewById(R.id.tabText)).setText(labelId);

        TabHost.TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setIndicator(indicator);
        tabSpec.setContent(tabContentId);
        return tabSpec;
    }

    @Override
    public void onTabChanged(String tabId) {

    }
}

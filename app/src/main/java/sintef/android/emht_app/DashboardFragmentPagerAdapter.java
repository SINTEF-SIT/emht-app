package sintef.android.emht_app;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by iver on 17/06/15.
 */
public class DashboardFragmentPagerAdapter extends FragmentPagerAdapter {

    private String tabTitles[] = new String[] { "Registration", "Assessment", "Actions" };
    final int PAGE_COUNT = tabTitles.length;
    private Context context;
    private long alarmId;

    public DashboardFragmentPagerAdapter(FragmentManager fm, Context context, long alarmId) {
        super(fm);
        this.context = context;
        this.alarmId = alarmId;
    }

    @Override
    public Fragment getItem(int position) {
        return PageFragment.newInstance(position + 1, alarmId);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}

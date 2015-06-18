package sintef.android.emht_app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sintef.android.emht_app.fragments.ActionsFragment;
import sintef.android.emht_app.fragments.AssessmentFragment;
import sintef.android.emht_app.fragments.IncidentFragment;
import sintef.android.emht_app.fragments.RegistrationFragment;

/**
 * Created by iver on 17/06/15.
 */
public class PageFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";
    private final String TAG = this.getClass().getSimpleName();
    private final static String ALARM_ID = "alarm_id";
    private long alarmId;

    public static Fragment newInstance(int page, long alarmId) {
        Bundle args = new Bundle();
        args.putLong(ALARM_ID, alarmId);

        Fragment fragment;

        switch(page) {
            case 1:
                fragment = new RegistrationFragment();
                break;
            case 2:
                fragment = new AssessmentFragment();
                break;
            case 3:
                fragment = new ActionsFragment();
                break;
            default:
                fragment = new IncidentFragment();
                break;
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmId = getArguments().getLong(ALARM_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        return view;
    }
}

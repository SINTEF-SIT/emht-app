package sintef.android.emht_app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.greenrobot.event.EventBus;
import sintef.android.emht_app.R;

/**
 * Created by iver on 10/06/15.
 */
public class AssessmentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View assessmentView = inflater.inflate(R.layout.fragment_assessment, container, false);

        return assessmentView;
    }

}

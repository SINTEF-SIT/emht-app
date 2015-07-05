package sintef.android.emht.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sintef.android.emht.R;

/**
 * Created by iver on 19/06/15.
 */
public class EmptyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View emptyView = inflater.inflate(R.layout.fragment_empty, container, false);
        return emptyView;
    }
}

package com.picspy.views.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.picspy.firstapp.R;

/**
 * Created by Justin12 on 6/6/2015.
 */
public class BoredFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_bored, container, false);

        return rootView;
    }
}

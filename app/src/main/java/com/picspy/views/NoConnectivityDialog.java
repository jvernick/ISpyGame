package com.picspy.views;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.picspy.firstapp.R;

/**
 * Created by BrunelAmC on 8/26/2015.
 */
public class NoConnectivityDialog extends DialogFragment {
    Button btnOk;

    //---empty constructor required
    public NoConnectivityDialog() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.no_connection_dialog, container);
        //---get the Button views---
        btnOk = (Button) view.findViewById(R.id.btnOk);

        // Button listener
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }
}

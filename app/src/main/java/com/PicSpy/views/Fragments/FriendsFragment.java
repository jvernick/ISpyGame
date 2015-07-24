package com.picspy.views.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.dreamfactory.api.DbApi;
import com.dreamfactory.model.RecordsResponse;
import com.picspy.FriendsTableRequests;
import com.picspy.firstapp.R;
import com.picspy.models.FriendRecord;
import com.picspy.models.FriendsRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.views.LoginActivity;
import com.picspy.views.MainActivity;

/**
 * Created by Justin12 on 6/6/2015.
 */
public class FriendsFragment extends Fragment implements View.OnClickListener {

    private Dialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("loading");

        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        Button b = (Button) rootView.findViewById(R.id.button);
        b.setOnClickListener(this);
        return rootView;
    }

    public void testRequest (View view) {
        GetRecordsTask listItem = new GetRecordsTask();
        listItem.execute();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                testRequest(view);
                break;
        }
    }


    class GetRecordsTask extends AsyncTask<Void, String, String> {
        private String errorMsg;

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
                FriendsTableRequests request = new FriendsTableRequests(getActivity().getApplicationContext());
                //String result = request.sendFriendRequest(1);
                //FriendRecord result = request.getStats(9);
                //String result = request.removeFriend(1);
                //String result = request.acceptFriendRequest(10);
                //FriendsRecord temp = request.getFriendRequests();
                String result = request.updateStats(1,true);
                if (result != null && result.equals("SUCCESS")) { //TODO String contains error message on error
                    //Log.d("FriendsFragment", result.toString());
                    return "SUCCESS";
                } else {
                    return "FAILED";
                }
        }
        @Override
        protected void onPostExecute(String records) {
            if(progressDialog != null && progressDialog.isShowing()){
                progressDialog.cancel();
            }
            if(records.equals("Success")){ // success
                Log.d("Friends","Success");
            }else{ // some error show dialog
               Log.d("Friends", records);
            }
        }
    }
}

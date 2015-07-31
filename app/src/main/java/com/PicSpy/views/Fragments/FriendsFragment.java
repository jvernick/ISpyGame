package com.picspy.views.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.picspy.FriendInfoActivity;
import com.picspy.FriendsTableRequests;
import com.picspy.firstapp.R;
import com.picspy.models.FriendRecord;
import com.picspy.models.FriendsRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.views.LoginActivity;
import com.picspy.views.MainActivity;
import com.picspy.views.RegisterActivity;

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
        Button button = (Button) rootView.findViewById(R.id.button);
        Button button2 =  (Button) rootView.findViewById(R.id.button2);
        button.setOnClickListener(this);
        button2.setOnClickListener(this);
        return rootView;
    }

    public void testRequest(View view) {
        GetRecordsTask listItem = new GetRecordsTask();
        listItem.execute();
    }

    public void testInfoPage( View view) {
        Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
        startActivity(intent);
    }

    @Override//TODO DOcument: enables fragment to handle button
    public void onClick(View view) {
        Log.d("FriendsFragment","onClick");
        switch (view.getId()) {
            case R.id.button:
                Log.d("FriendsFragment","case1");
                testRequest(view);
                break;
            case R.id.button2:
                Log.d("FriendsFragment","case2");
                testInfoPage(view);
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

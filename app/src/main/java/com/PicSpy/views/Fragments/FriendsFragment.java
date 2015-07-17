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
            String session_id = PrefUtil.getString(getActivity().getApplicationContext(),
                    AppConstants.SESSION_ID, null);
            DbApi dbApi = new DbApi();
            dbApi.addHeader("X-DreamFactory-Application-Name", AppConstants.APP_NAME);
            dbApi.addHeader("X-DreamFactory-Session-Token", session_id);
            dbApi.setBasePath(AppConstants.DSP_URL);
            try {
                FriendsRecord records = dbApi.getRecordsByFilter(FriendsRecord.class, AppConstants.FRIENDS_TABLE_NAME, null, -1, -1, null, null, false, false, null);
                FriendsTableRequests request = new FriendsTableRequests(getActivity().getApplicationContext());
                String result = request.addFriend("9");
                Log.d("FriendsFragment",records.toString());
                Log.d("FriendsFragment",result);
                return result;
            } catch (Exception e) {
                Log.d("FriendsFragment", e.getMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(String records) {
            if(progressDialog != null && progressDialog.isShowing()){
                progressDialog.cancel();
            }
            if(records != null){ // success
                Log.d("Friends","Success");
            }else{ // some error show dialog
               Log.d("Friends", "ERROR");
            }
        }
    }
}

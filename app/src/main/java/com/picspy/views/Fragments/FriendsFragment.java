package com.picspy.views.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.picspy.views.FriendInfoActivity;
import com.picspy.FriendsTableRequests;
import com.picspy.firstapp.R;

/**
 * Created by Justin12 on 6/6/2015.
 */
public class FriendsFragment extends Fragment implements View.OnClickListener {
    public final static String FRIEND_USERNAME = "com.picspy.USERNAME";
    public final static String FRIEND_ID = "com.picspy.FRIEND_ID";
    private Dialog progressDialog;

    // This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;
     // If non-null, this is the current filter the user has provided.
    String mCurFilter;

  /*  @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
        setEmptyText("No friends");
        setHasOptionsMenu(true);
        //request list of names
         testRequest1(this.getListView());
    }
      @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO implement some logic
    }*/


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

    private void testRequest(View view) {

    }
    /*public void testRequest1(View view) {
        //content of example list
        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                 "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
          "Linux", "OS/2" };
        Arrays.sort(values);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);
         setListAdapter(adapter);

    }*/

    public void testInfoPage( View view) {
        Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
        intent.putExtra(FRIEND_ID, 9);
        intent.putExtra(FRIEND_USERNAME, "brunelamc");
        startActivity(intent);
    }

    @Override//TODO Document: enables fragment to handle button
    public void onClick(View view) {
        Log.d("FriendsFragment", "onClick");
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
                String result = request.updateStats(9,true);
                if (result != null) Log.d( "Friends", result);
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

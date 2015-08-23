package com.picspy.views.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.content.Context;
import com.picspy.adapters.FriendsCursorAdapter;

import com.picspy.adapters.DatabaseHandler;
import com.picspy.adapters.GamesCursorAdapter;
import com.picspy.models.Friend;
import com.picspy.views.FriendInfoActivity;
import com.picspy.FriendsTableRequests;
import com.picspy.firstapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Justin12 on 6/6/2015.
 */
public class FriendsFragment extends ListFragment  {
    public final static String FRIEND_USERNAME = "com.picspy.USERNAME";
    public final static String FRIEND_ID = "com.picspy.FRIEND_ID";
    private FriendsCursorAdapter adapter;
    public DatabaseHandler dbHandler;
    private Dialog progressDialog;

    // This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;
    // If non-null, this is the current filter the user has provided.
    String mCurFilter;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dbHandler = DatabaseHandler.getInstance(getActivity());
        adapter = new FriendsCursorAdapter(getActivity(), R.layout.item_friends,
                dbHandler.getAllFriends(),
                FriendsCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(adapter);
        //(new GetRecordsTask()).execute();
//        setEmptyText("No friends");
//        setHasOptionsMenu(true);
//        dbHandler =  new DatabaseHandler(getActivity());
//        for(int i = 0; i < 10; i++) {
//            dbHandler.addFriend(new Friend(i,"Friend" + i),"update" + i);
//        }
//        //request list of names
//        testRequest(this.getListView());
    }



//    public void testRequest(View view) {
//        //content of example list
//        String[] value = new String[] { "Android", "iPhone", "WindowsMobile",
//                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
//                "Linux", "OS/2" };
//        List <Friend> f = dbHandler.getAllfriends();
//        Friend[] arr = f.toArray(new Friend[f.size()]);
//        String[] values = new String[arr.length];
//        for(int i = 0; i < arr.length; i++)
//            values[i] = arr[i].getUsername();
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//                R.layout.fragment_friends, values);
//        setListAdapter(adapter);
//
//    }

    public void testInfoPage( View view) {
        Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
        intent.putExtra(FRIEND_ID, 9);
        intent.putExtra(FRIEND_USERNAME, "brunelamc");
        startActivity(intent);
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

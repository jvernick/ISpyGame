package com.picspy.views.Fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.picspy.FriendsTableRequests;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.adapters.FriendsCursorAdapter;
import com.picspy.firstapp.R;
import com.picspy.views.FindFriendsActivity;

/**
 * Created by Justin12 on 6/6/2015.
 */
public class FriendsFragment extends ListFragment {
    private FriendsCursorAdapter adapter;
    private View listHeader;
    private EditText searchField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        ImageView findFriend = (ImageView) rootView.findViewById(R.id.find_friend);
        findFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), FindFriendsActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //in this method because it requires the activity context

        (new GetRecordsTask()).execute();

        DatabaseHandler dbHandler = DatabaseHandler.getInstance(getActivity());
        if (adapter == null) {
            adapter = new FriendsCursorAdapter(getActivity().getApplicationContext(), R.layout.item_friends,
                    dbHandler.getAllFriends(),
                    FriendsCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            setListAdapter(adapter);
        }

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
            // Search for friends whose names begin with the specified letters.
            Cursor cursor =  DatabaseHandler.getInstance(getActivity()
                    .getApplicationContext()).getMatchingFriends(
                    (constraint != null ? constraint.toString() : null));
            return cursor;
            }
        });

        listHeader = getView().findViewById(R.id.friend_list_header);
        if (listHeader != null) {
            View searchBox = listHeader.findViewById(R.id.search_box);
            if (searchBox != null) {
                searchField = (EditText) searchBox.findViewById(R.id.clearable_edit);
                Log.d("FriendsFragment: ", "Found search box");
            } else {
                Log.d("FriendsFragment: ", "searchbox null");
            }
            if (searchField != null) {
                Log.d("FriendsFragment: ", "Found search field");
                setSearchFieldFilter(searchField);
            }
        } else {
            Log.d("FriendsFragment: ", "ListHeader null");
        }

       /* View rootView = getListView().findViewWithTag("searchBox");
        if (rootView != null) {
            searchField = (EditText) rootView.findViewWithTag("searchField");
            Log.d("FriendsFragment: ", "Found search box");
        }
        if (searchField != null) {
            Log.d("FriendsFragment: ", "Found search field");
            setSearchFieldFilter(searchField);
        }*/
    }

    private void setSearchFieldFilter (final EditText searchText) {

        Log.d("OnTextChanged", "setSearchFieldFilter");
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                Log.d("FriendsFragment", "textChanged: " + cs);

                if (cs.length() == 0) {
                    searchText.clearFocus();
                    if (getView() != null) getView().requestFocus();
                    InputMethodManager mgr = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                }
                adapter.getFilter().filter(cs);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     */
    @Override
    public void onResume() {
        super.onResume();

        //clear search field
        if (searchField != null && searchField.getText().length() == 0) {
            searchField.clearFocus();
            searchField.setText("");
        }
    }

    class GetRecordsTask extends AsyncTask<Void, String, String> {
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
            if(records.equals("Success")){ // success
                Log.d("Friends","Success");
            }else{ // some error show dialog
                Log.d("Friends", records);
            }
        }
    }
}

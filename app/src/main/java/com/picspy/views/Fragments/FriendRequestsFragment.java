package com.picspy.views.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.picspy.FriendsTableRequests;
import com.picspy.adapters.FriendRequestsArrayAdapter;
import com.picspy.firstapp.R;
import com.picspy.models.Friend;
import com.picspy.models.FriendRecord;
import com.picspy.models.FriendsRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Friend>> {
    private static final int LOADER_ID = 0;
    private static int myUserId;
    private ProgressBar progressSpinner;
    private FriendRequestsArrayAdapter arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_request, container, false);
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
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myUserId = PrefUtil.getInt(getActivity().getApplicationContext(), AppConstants.USER_ID);
        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();

        if (getView() != null) {
            progressSpinner = (ProgressBar) getView().findViewById(R.id.progressBar);
            progressSpinner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<List<Friend>> onCreateLoader(int i, Bundle bundle) {
        if (progressSpinner != null) {
            progressSpinner.setVisibility(View.VISIBLE);
        }
        return new FriendRequestLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Friend>> loader, List<Friend> friends) {
        progressSpinner.setVisibility(View.GONE);
        if (arrayAdapter == null) {
            arrayAdapter = new FriendRequestsArrayAdapter(getActivity(),
                    R.layout.item_friend_request, friends);
            setListAdapter(arrayAdapter);
        } else {
            arrayAdapter.setData(friends);
            arrayAdapter.notifyDataSetChanged();
        }
        if (getView() != null) {
            getListView().setEmptyView(getView().findViewById(R.id.empty_list));
        }
    }

    /**TODO may not be needed
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Toast.makeText(getActivity().getApplicationContext(), "Starting friend_info activity", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(Loader<List<Friend>> loader) {
       setListAdapter(null);
    }


    public static class FriendRequestLoader extends AsyncTaskLoader<List<Friend>> {
        public FriendRequestLoader(Context context) {
            super(context);
        }


        @Override
        public List<Friend> loadInBackground() {
            final String[] animals = new String[] { "Ape", "Bird","Lizard"};
            final List<Friend> requestList = new ArrayList<>();
            for (int i = 0; i < animals.length; ++i) {
                requestList.add(new Friend(i,animals[i]));
            }

            FriendsRecord records = (new FriendsTableRequests(getContext())).getFriendRequests();
            if (records != null) {
                List<FriendRecord> requestRecords = records.getRecord();
                for (FriendRecord record : requestRecords) {
                    if (record != null) {
                        try {
                            requestList.add(record.getOtherUserRecord(myUserId).getRecordToFriend());
                        } catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            return requestList;
        }
    }
}

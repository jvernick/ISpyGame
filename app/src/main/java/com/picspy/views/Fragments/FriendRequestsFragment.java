package com.picspy.views.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.adapters.FriendRequestsArrayAdapter;
import com.picspy.firstapp.R;
import com.picspy.models.FriendRecord;
import com.picspy.models.FriendsRecord;
import com.picspy.models.UserRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.FriendsRequests;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.VolleyRequest;
import com.picspy.views.FindFriendsActivity;

import java.util.ArrayList;

public class FriendRequestsFragment extends ListFragment implements FriendRequestsArrayAdapter.AdapterRequestListener {
    private static final String TAG = "FriendRequestsFragment";
    public static final String ARG_NOTF = "isNotification";
    private static int myUserId;
    private boolean fromNotf;
    private ProgressBar progressSpinner;
    private FriendRequestsArrayAdapter arrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Reset friend notification badge
        PrefUtil.putInt(getActivity(), AppConstants.FRIEND_REQUEST_COUNT, 0);
        fromNotf = getArguments().getBoolean(ARG_NOTF, false);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_request, container, false);
    }

    /**
     * Static factory method that takes an int parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static FriendRequestsFragment newInstance(boolean isNotification) {
        FriendRequestsFragment f = new FriendRequestsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NOTF, isNotification);
        f.setArguments(args);
        return f;
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
        getFriendRequests();
        if (getView() != null) {
            progressSpinner = (ProgressBar) getView().findViewById(R.id.challenges_progressBar);
            progressSpinner.setVisibility(View.VISIBLE);
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

    private void getFriendRequests() {
        Response.Listener<FriendsRecord> response = new Response.Listener<FriendsRecord>() {
            @Override
            public void onResponse(FriendsRecord response) {
                progressSpinner.setVisibility(View.GONE);
                if (response.getCount() != 0) {
                    processResponse(response);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressSpinner.setVisibility(View.GONE);
                if (error != null ) {
                    String err = (error.getMessage() == null)? "An error occurred": error.getMessage();
                    error.printStackTrace();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection this is from a notification
                    if (err.matches(AppConstants.CONNECTION_ERROR) && fromNotf) {
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) getActivity().findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getActivity());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                }
            }
        };

        FriendsRequests getRequests = FriendsRequests.getFriendRequests(getActivity(), response, errorListener);
        getRequests.setTag(FindFriendsActivity.CANCEL_TAG);
        VolleyRequest.getInstance(getActivity().getApplicationContext()).addToRequestQueue(getRequests);
    }

    private void processResponse(FriendsRecord response) {
        ArrayList<UserRecord> requestList = new ArrayList<>();
        ArrayList<FriendRecord> requestRecords = response.getResource();
        Log.d("RequestFragment", response.toString());
        for (FriendRecord record : requestRecords) {
            requestList.add(record.getOtherUserRecord(myUserId));
        }

        if (arrayAdapter == null) {
            arrayAdapter = new FriendRequestsArrayAdapter(getActivity(),
                    R.layout.item_friend_request, requestList);
            arrayAdapter.setAdapterRequestListener(this);

            setListAdapter(arrayAdapter);
        } else {
            arrayAdapter.setData(requestList);
            arrayAdapter.notifyDataSetChanged();
        }
        if (getView() != null) {
            getListView().setEmptyView(getView().findViewById(R.id.empty_list));
        }
    }

    @Override
    public void declineRequest(int friend_id, final int position) {
            Response.Listener<FriendRecord> responseListener = new Response.Listener<FriendRecord>() {
                @Override
                public void onResponse(FriendRecord response) {
                    if (response != null) {
                        arrayAdapter.removeItem(position);
                    }
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error != null ) {
                        String err = (error.getMessage() == null)? "An error occurred": error.getMessage();
                        //Show toast only if there is no server connection on refresh
                        if (err.matches(AppConstants.CONNECTION_ERROR)) {
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View layout = inflater.inflate(R.layout.custom_toast,
                                    (ViewGroup)getActivity().findViewById(R.id.toast_layout_root));
                            Toast toast = new Toast(getActivity());
                            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                        } else { //TODO for debugging, remove
                            Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };

            FriendsRequests deleteFriedRequest = FriendsRequests.removeFriend(getActivity(), friend_id, responseListener, errorListener);
            if (deleteFriedRequest != null) deleteFriedRequest.setTag(FindFriendsActivity.CANCEL_TAG);
            VolleyRequest.getInstance(getActivity().getApplicationContext()).addToRequestQueue(deleteFriedRequest);
    }

    @Override
    public void acceptRequest(int friend_id, final int position) {
        Response.Listener<FriendRecord> responseListener = new Response.Listener<FriendRecord>() {
            @Override
            public void onResponse(FriendRecord response) {
                if (response != null ) {
                    Log.d(TAG, response.toString());
                    arrayAdapter.removeItem(position);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    String err = (error.getMessage() == null)? "error message null": error.getMessage();
                    error.printStackTrace();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR)) {
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) getActivity().findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getActivity());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    } else {//TODO for debugging, remove
                        Toast.makeText(getActivity(), "An error occurred",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        FriendsRequests deleteFriedRequest = FriendsRequests.acceptFriendRequest(getActivity(), friend_id, responseListener, errorListener);
        if (deleteFriedRequest != null) deleteFriedRequest.setTag(FindFriendsActivity.CANCEL_TAG);
        VolleyRequest.getInstance(getActivity().getApplicationContext()).addToRequestQueue(deleteFriedRequest);
    }

    public void refresh() {
        fromNotf = true;
        getFriendRequests();
        Log.d(TAG, "refreshing");
    }
}

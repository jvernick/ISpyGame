package com.picspy.views.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.adapters.FriendRequestsRecyclerAdpater;
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

public class FriendRequestsFragment extends Fragment implements FriendRequestsRecyclerAdpater.AdapterRequestListener {
    private static final String TAG = "FriendRequestsFragment";
    public static final String ARG_NOTF = "isNotification";
    private static int myUserId;
    private boolean fromNotf;
    private ProgressBar progressSpinner;
    private RecyclerView mRecyclerView;
    private FriendRequestsRecyclerAdpater mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView emptyView;

    public FriendRequestsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Reset friend notification badge
        PrefUtil.putInt(getActivity(), AppConstants.FRIEND_REQUEST_COUNT, 0);
        fromNotf = getArguments().getBoolean(ARG_NOTF, false);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_friend_request, container, false);
        rootView.setTag(TAG);

        emptyView = (TextView) rootView.findViewById(R.id.empty_list);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        mLayoutManager =  new LinearLayoutManager(getActivity());

        setRecyclerViewLayoutManager();

        mAdapter = new FriendRequestsRecyclerAdpater(new ArrayList<UserRecord>());
        mAdapter.setAdapterRequestListener(this);
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }

    private void setRecyclerViewLayoutManager() {
        int scrollPosition = 0;
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager)
                    mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
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

    private void getFriendRequests() {
        Log.d(TAG, "getting friend requests");
        Response.Listener<FriendsRecord> response = new Response.Listener<FriendsRecord>() {
            @Override
            public void onResponse(FriendsRecord response) {
                Log.d(TAG, response.toString());
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
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR) && fromNotf) {
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
        Log.d(TAG, response.toString());
        for (FriendRecord record : requestRecords) {
            requestList.add(record.getOtherUserRecord(myUserId));
        }

        mAdapter.setData(requestList);
        checkAdapterEmpty();
    }

    private void checkAdapterEmpty() {
        if (mAdapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void declineRequest(int friend_id, final int position) {
            Response.Listener<FriendRecord> responseListener = new Response.Listener<FriendRecord>() {
                @Override
                public void onResponse(FriendRecord response) {
                    if (response != null) {
                        mAdapter.removeItem(position);
                    }
                }
            };

            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error != null ) {
                        String err = (error.getMessage() == null)? "An error occurred": error.getMessage();
                        //Show toast only if there is no server connection on refresh
                        if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
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
                    mAdapter.removeItem(position);
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
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
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

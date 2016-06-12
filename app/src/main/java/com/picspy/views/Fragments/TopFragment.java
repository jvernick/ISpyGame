package com.picspy.views.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.adapters.TopFragmentArrayAdapter;
import com.picspy.adapters.TopFragmentRecyclerAdapter;
import com.picspy.firstapp.R;
import com.picspy.models.Game;
import com.picspy.models.GameRecord;
import com.picspy.models.GamesRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.ChallengesRequests;
import com.picspy.utils.VolleyRequest;
import com.picspy.views.ChallengesActivity;
import com.picspy.views.MainActivity;
import com.picspy.views.ViewChallenge;

import java.util.ArrayList;

//Leaderboard fragment
//TODO reorganize listadapter init and use
public class TopFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        TopFragmentRecyclerAdapter.AdapterRequestListener {
    private TopFragmentArrayAdapter arrayAdapter;
    private static final String TAG = "TopFragment";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TopFragmentRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create the list fragment's content view by calling the super method
       // final View listFragmentView = super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_top, container, false);

        // [ start Setup recycler view ]
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        mLayoutManager =  new LinearLayoutManager(getActivity());
        setRecyclerViewLayoutManager();
        mAdapter =  new TopFragmentRecyclerAdapter(new ArrayList<Game>());
        mAdapter.setAdapterRequestListener(this);
        mRecyclerView.setAdapter(mAdapter);
        // [ end Setup recycler view ]

        // [ Start setup swipe refresh layout ]
        // Now create a SwipeRefreshLayout to wrap the fragment's content view
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accent));
        mSwipeRefreshLayout.setOnRefreshListener(this);


        // [ End swipe refresh layout setup ]

        // Now return the SwipeRefreshLayout as this fragment's content view
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Start out with a progress indicator.
        //setListShown(true);
        Log.d(TAG, "executing search");
        getLeaderboard(false);
    }

    public void onListItemClick(int position) {
        Intent intent = new Intent(getActivity(), ViewChallenge.class);
        intent.putExtra(ChallengesActivity.GAME_EXTRA, mAdapter.getItem(position));
        startActivityForResult(intent, ChallengesActivity.PLAY_GAME_CODE);
    }

    // [ swipeRefreshLayout implementation ]
    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh");
        //(new GetLeaderboard(true)).execute();
        getLeaderboard(true);
    }


    private void getLeaderboard(final Boolean isRefresh) {
        Response.Listener<GamesRecord> responseListener = new Response.Listener<GamesRecord>() {
            @Override
            public void onResponse(GamesRecord response) {
                updateChallenges(response, isRefresh);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(isRefresh) mSwipeRefreshLayout.setRefreshing(false);
                if (error != null ) {
                    String err = (error.getMessage() == null) ? "An error occurred" : error.getMessage();
                    error.printStackTrace();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR) && isRefresh) {
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

        if (isRefresh ) mSwipeRefreshLayout.setRefreshing(true);
        ChallengesRequests leaderboardRequest = ChallengesRequests.getleaderboard(getActivity(), responseListener, errorListener);
        leaderboardRequest.setTag(MainActivity.CANCEL_TAG);
        VolleyRequest.getInstance(getActivity().getApplicationContext()).addToRequestQueue(leaderboardRequest);
    }

    private void updateChallenges(GamesRecord gamesRecord, boolean refresh) {
        if (gamesRecord != null && gamesRecord.getCount() != 0) {
            ArrayList<Game> gameList = new ArrayList<>();

            for (GameRecord gameRecord : gamesRecord.getResource()) {
                gameList.add(GameRecord.getGame(gameRecord, getActivity().getApplicationContext()));
            }

           /* if (arrayAdapter == null) {
                arrayAdapter = new TopFragmentArrayAdapter(getActivity().getApplicationContext(), R.layout.item_challenge, gameList);

            }*/

            if (refresh) {
                mAdapter.setData(gameList);
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }


    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ChallengesActivity.PLAY_GAME_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    boolean value = data.getBooleanExtra(ChallengesActivity.GAME_RESULT_VALUE, false);
                    int challengeId = data.getIntExtra(ChallengesActivity.GAME_RESULT_CHALLENGE, -1);
                    int recordId = data.getIntExtra(ChallengesActivity.GAME_RESULT_RECORD, -1);
                    int sender = data.getIntExtra(ChallengesActivity.GAME_RESULT_SENDER, -1);
                    (new ChallengesActivity()).processGameResult(value, challengeId, recordId, sender, true);
                } else if ( resultCode == Activity.RESULT_CANCELED) {
                    //do nothing
                }
                break;
        }
    }
}

package com.picspy.views.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.dreamfactory.client.ApiException;
import com.picspy.GamesRequests;
import com.picspy.adapters.TopFragmentArrayAdapter;
import com.picspy.firstapp.R;
import com.picspy.models.Game;
import com.picspy.models.GameRecord;
import com.picspy.models.GamesRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.ChallengesRequests;
import com.picspy.utils.VolleyRequest;
import com.picspy.views.MainActivity;

import java.util.ArrayList;

//Leaderboard fragment
//TODO reorganize listadapter init and use
public class TopFragment extends android.support.v4.app.ListFragment
        implements SwipeRefreshLayout.OnRefreshListener {
    private TopFragmentArrayAdapter arrayAdapter;
    private static final String TAG = "TopFragment";
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create the list fragment's content view by calling the super method
        final View listFragmentView = super.onCreateView(inflater, container, savedInstanceState);

        // Now create a SwipeRefreshLayout to wrap the fragment's content view
        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());

        // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
        // the SwipeRefreshLayout
        mSwipeRefreshLayout.addView(listFragmentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Make sure that the SwipeRefreshLayout will fill the fragment
        mSwipeRefreshLayout.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.accent));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Now return the SwipeRefreshLayout as this fragment's content view
        return mSwipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Start out with a progress indicator.
        setListShown(true);
        Log.d(TAG, "executing search");

        getLeaderboard(false);
    }

    /*
     * implemented lodercalbacks methods
     */


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
        Toast.makeText(getActivity().getApplicationContext(), arrayAdapter.getItem(position).toString(),
                Toast.LENGTH_LONG).show();

        //TODO Start activity to display Game
                /*
                    get game in reciepient activity as follows:
                    Bundle Bundle = intent.getExtras();
                    Game game = (game) bundle.getParcelable(ChallengesActivity.EXTRA_MESSAGE);
                 */
                /*
                Intent intent = new Intent(ChallengesActivity.this, SecondActivity.class);
                intent.putExtra(EXTRA_MESSAGE, arrayAdapter.getItem(position));
                startActivity(intent);
                */
    }

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
                    if (err.matches(AppConstants.CONNECTION_ERROR) && isRefresh) {
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
                gameList.add(GameRecord.getGame(gameRecord));
            }

           /* if (arrayAdapter == null) {
                arrayAdapter = new TopFragmentArrayAdapter(getActivity().getApplicationContext(), R.layout.item_challenge, gameList);

            }*/

            if (refresh) {
                if (arrayAdapter == null) {
                    arrayAdapter = new TopFragmentArrayAdapter(getActivity().getApplicationContext(), R.layout.item_challenge,
                            gameList);
                    setListAdapter(arrayAdapter);
                } else {
                    arrayAdapter.replaceGames(gameList);
                    arrayAdapter.notifyDataSetChanged();
                }
            } else {
                // Create an empty adapter we will use to display the loaded data.
                arrayAdapter = new TopFragmentArrayAdapter(getActivity().getApplicationContext(), R.layout.item_challenge,
                        gameList);
                setListAdapter(arrayAdapter);
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {
        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }

        /**
         * As mentioned above, we need to override this method to properly signal when a
         * 'swipe-to-refresh' is possible.
         *
         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
         */
        @Override
        public boolean canChildScrollUp() {
            final ListView listView = getListView();
            return listView.getVisibility() == View.VISIBLE && canListViewScrollUp(listView);
        }
    }

    /**
     * Utility method to check whether a {@link ListView} can scroll up from it's current position.
     * Handles platform version differences, providing backwards compatible functionality where
     * needed.
     */
    private static boolean canListViewScrollUp(ListView listView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // For ICS and above we can call canScrollVertically() to determine this
            return ViewCompat.canScrollVertically(listView, -1);
        } else {
            // Pre-ICS we need to manually check the first visible item and the child view's top
            // value
            return listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
        }

    }
}

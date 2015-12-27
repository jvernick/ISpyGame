package com.picspy.views.Fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.dreamfactory.client.ApiException;
import com.picspy.GamesRequests;
import com.picspy.adapters.TopFragmentArrayAdapter;
import com.picspy.firstapp.R;
import com.picspy.models.Game;
import com.picspy.models.GameRecord;
import com.picspy.models.GamesRecord;

import java.util.ArrayList;

//Leaderboard fragment
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
        mSwipeRefreshLayout.setRefreshing(true);


        // Now return the SwipeRefreshLayout as this fragment's content view
        return mSwipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Start out with a progress indicator.
        setListShown(true);
        Log.d(TAG, "executing search");

        (new GetLeaderboard(false)).execute();
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
        (new GetLeaderboard(true)).execute();
    }


    private class GetLeaderboard extends AsyncTask<Void, Void, GamesRecord> {
        private final boolean refresh;

        /**
         * Constructor to determine is this call is to create a new list or to refresh
         * the current list
         * @param refresh
         */
        public GetLeaderboard(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected GamesRecord doInBackground(Void... voids) {
            try {
                //TODO this sometimes causes a null pointer. verify
                GamesRequests gamesRequests = new GamesRequests(getActivity().getApplicationContext(), false);
                GamesRecord result =  gamesRequests.getLeaderboardGames();
                if (result != null) Log.d(TAG,result.toString());
                return result;
            } catch (ApiException e) { //TODO handle exception
                Log.d(TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(GamesRecord gamesRecord) {
            if (gamesRecord != null) {
                ArrayList<Game> gameList = new ArrayList<>();
                for (GameRecord gameRecord : gamesRecord.getRecord()) {
                    gameList.add(GameRecord.getGame(gameRecord));
                }

                if (gameList.size() != 0) {
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
                        //TODO move these next two lines to the asynctask post
                        // Create an empty adapter we will use to display the loaded data.
                        arrayAdapter = new TopFragmentArrayAdapter(getActivity().getApplicationContext(), R.layout.item_challenge,
                                gameList);
                        setListAdapter(arrayAdapter);
                    }
                }
            }
            mSwipeRefreshLayout.setRefreshing(false);
            //setListShown(true);
        }
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
            if (listView.getVisibility() == View.VISIBLE) {
                return canListViewScrollUp(listView);
            } else {
                return false;
            }
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

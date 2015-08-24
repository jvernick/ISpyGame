package com.picspy.views.Fragments;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.dreamfactory.client.ApiException;
import com.picspy.GamesRequests;
import com.picspy.adapters.CustomArrayAdapter;
import com.picspy.firstapp.R;
import com.picspy.models.Game;
import com.picspy.models.GameRecord;
import com.picspy.models.GamesRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopFragment extends android.support.v4.app.ListFragment {
    private CustomArrayAdapter arrayAdapter;
    private static final String TAG = "TopFragment";


    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initially there is no data
        setEmptyText("No Data Here");

        // Start out with a progress indicator.
        setListShown(false);
        Log.d(TAG, "executing search");
        (new GetLeaderboard()).execute();
    }

    /*
     * implemented lodercalbacks methods
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
        Log.d("DataListFragment", "Item clicked: " + id);
        Toast.makeText(getActivity(), arrayAdapter.getItem(position).toString(),
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



    private class GetLeaderboard extends AsyncTask<Void, Void, GamesRecord> {
        @Override
        protected GamesRecord doInBackground(Void... voids) {
            try {
                GamesRequests gamesRequests = new GamesRequests(getActivity(), false);
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
                    //TODO move these next two lines to the asynctask post
                    // Create an empty adapter we will use to display the loaded data.
                    arrayAdapter = new CustomArrayAdapter(getActivity(), R.layout.item_challenge,
                            gameList);
                    setListAdapter(arrayAdapter);
                }
            }
            setListShown(true);
        }
    }


}

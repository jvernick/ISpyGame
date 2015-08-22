package com.picspy.views;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.dreamfactory.client.ApiException;
import com.picspy.GamesRequests;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.adapters.GamesCursorAdapter;
import com.picspy.firstapp.R;
import com.picspy.models.Game;
import com.picspy.models.GameRecord;
import com.picspy.models.UserChallengeRecord;
import com.picspy.models.UserChallengesRecord;

import java.util.ArrayList;
import java.util.List;

public class ChallengesActivity extends ActionBarActivity {
    private static final String TAG = "ChallengesActivity";
    private GamesCursorAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        listView = (ListView) findViewById(R.id.challenge_list);
        //set adapter to populate listView from database
        //content observer flag makes the view auto refresh
        adapter = new GamesCursorAdapter(getApplicationContext(), R.layout.item_challenge,
                (new DatabaseHandler(getApplicationContext())).getAllGames(),
                GamesCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(adapter);

        //update database
        (new GetChallengesTask()).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_challenges, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GetChallengesTask extends AsyncTask<Void, Void, UserChallengesRecord> {
        @Override
        protected UserChallengesRecord doInBackground(Void... voids) {
            try {
                GamesRequests gamesRequests = new GamesRequests(getApplicationContext(), false);
                return gamesRequests.getGamesInfo();
            } catch (ApiException ex) {
                Log.d(TAG, ex.getMessage());
                //TODO handle exception
                //toast for Internet connection error
                return null;
            }
        }

        /**
         * Attempts to add challenges from server to local database
         * @param challenges Challenges from server
         */
        @Override
        protected void onPostExecute(UserChallengesRecord challenges) {
            if (challenges != null && challenges.getRecord() != null
                    && challenges.getRecord().size() != 0) {
                List<Game> gameList = new ArrayList<>();
                int max_user_challenge_id = 0;
                for (UserChallengeRecord record: challenges.getRecord()) {

                    if (record.getId() > max_user_challenge_id) {
                        max_user_challenge_id = record.getId();
                    }
                    gameList.add(record.getGame());
                }
                if (gameList.size() != 0) {
                    (new DatabaseHandler(getApplicationContext())).addGames(gameList,
                            max_user_challenge_id);
                }
            }
        }
    }
}

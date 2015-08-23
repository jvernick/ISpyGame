package com.picspy.views;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamfactory.client.ApiException;
import com.picspy.GamesRequests;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.adapters.GamesCursorAdapter;
import com.picspy.adapters.GamesCursorLoader;
import com.picspy.firstapp.R;
import com.picspy.models.Game;
import com.picspy.models.UserChallengeRecord;
import com.picspy.models.UserChallengesRecord;
import com.picspy.utils.DbContract.GameEntry;

import java.util.ArrayList;
import java.util.List;

public class ChallengesActivity extends ActionBarActivity  implements LoaderCallbacks<Cursor>{
    private static final String TAG = "ChallengesActivity";
    private GamesCursorAdapter cursorAdapter;
    private ListView listView;
    private DatabaseHandler dbHandler;
    private int LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        listView = (ListView) findViewById(R.id.challenge_list);
        //set cursorAdapter to populate listView from database
        //content observer flag makes the view auto refresh

        dbHandler = DatabaseHandler.getInstance(this);
        cursorAdapter = new GamesCursorAdapter(getApplicationContext(), R.layout.item_challenge,
                dbHandler.getAllGames(),0);
        //cursorAdapter.notifyDataSetChanged();
        listView.setAdapter(cursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor c = (Cursor) adapterView.getItemAtPosition(i);
                Game game = new Game();
                game.setPictureName(c.getString(c.getColumnIndex(GameEntry.COLUMN_NAME_PICTURE)));
                game.setSelection((c.getString(c.getColumnIndex(GameEntry.COLUMN_NAME_SEL))));
                game.setHint((c.getString(c.getColumnIndex(GameEntry.COLUMN_NAME_HINT))));
                game.setGuess((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_GUESS))));
                game.setTime((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_TIME))));
                game.setVote((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_VOTE))) != 0);
                game.setSender((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_SENDER))));
                game.setId(c.getInt(c.getColumnIndex(GameEntry._ID)));

                Toast.makeText(ChallengesActivity.this, game.toString(), Toast.LENGTH_LONG).show();

                /*TODO Start activity to display Game
                Intent intent = new Intent(ChallengesActivity.this, SecondActivity.class);
                startActivity(intent);
                */
            }
        });

        //update database
        (new GetChallengesTask()).execute();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Challenges");

        // Setting toolbar as the ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_back);

        //Initializing loader
        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
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
            //update database
            (new GetChallengesTask()).execute();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void launchCamera(View view) {
        /*TODO Start activity to create new game
        Intent intent = new Intent(ChallengesActivity.this, SecondActivity.class);
        startActivity(intent);
         */
        Toast.makeText(ChallengesActivity.this, "Game started", Toast.LENGTH_SHORT).show();
    }

    private class GetChallengesTask extends AsyncTask<Void, Void, UserChallengesRecord> {
        @Override
        protected UserChallengesRecord doInBackground(Void... voids) {
            try {
                GamesRequests gamesRequests = new GamesRequests(getApplicationContext(), false);
                return gamesRequests.getGamesInfo();
            } catch (ApiException ex) {
                Log.d(TAG + "test", ex.getMessage());
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
                    dbHandler.addGames(gameList,
                            max_user_challenge_id);
                    //dbHandler.close();
                    Log.d(TAG, "notifying");
                    //((GamesCursorAdapter) listView.getAdapter()).notifyDataSetChanged();
                    ((GamesCursorAdapter) listView.getAdapter()).changeCursor(dbHandler.getAllGames());
                }
            }
        }
    }


    /***
     *
     * Loaders callback methods
     *
     ***/
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.e(TAG, ":::: onCreateLoader");
        return new GamesCursorLoader(this, dbHandler);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.e(TAG, ":::: onLoadFinished");
        ((GamesCursorAdapter)listView.getAdapter()).changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.e(TAG, ":::: onLoaderReset");
        //cursorAdapter.swapCursor(null);
        ((GamesCursorAdapter)listView.getAdapter()).changeCursor(null);
    }

}

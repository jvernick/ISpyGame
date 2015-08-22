package com.picspy.views;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamfactory.client.ApiException;
import com.picspy.GamesRequests;
import com.picspy.adapters.CursorObserver;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.adapters.GamesCursorAdapter;
import com.picspy.adapters.GamesCursorLoader;
import com.picspy.firstapp.R;
import com.picspy.models.Game;
import com.picspy.models.UserChallengeRecord;
import com.picspy.models.UserChallengesRecord;

import java.util.ArrayList;
import java.util.List;

public class ChallengesActivity extends ActionBarActivity  implements LoaderCallbacks<Cursor>{
    private static final String TAG = "ChallengesActivity";
    private GamesCursorAdapter cursorAdapter;
    private ListView listView;
    private TextView toolbarTitle;
    private DatabaseHandler dbHandler;
    private int LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        listView = (ListView) findViewById(R.id.challenge_list);
        //set cursorAdapter to populate listView from database
        //content observer flag makes the view auto refresh

        //dbHandler = DatabaseHandler.getInstance(this);
        cursorAdapter = new GamesCursorAdapter(getApplicationContext(), R.layout.item_challenge,
                dbHandler.getAllGames(),
                GamesCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        //cursorAdapter.notifyDataSetChanged();
        listView.setAdapter(cursorAdapter);

        //update database
        (new GetChallengesTask()).execute();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
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

    public void startGame(View view) {
        /*TODO Start activity to create new game
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
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
                    dbHandler.addGames(gameList,
                            max_user_challenge_id);
                    //dbHandler.close();
                    //((GamesCursorAdapter) listView.getAdapter()).getCursor().requery();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dbHandler != null) {
            ((GamesCursorAdapter) listView.getAdapter()).getCursor().close();
           // cursorAdapter.getCursor().close();
            dbHandler.close();
        }
    }

    /*
    @Override
    protected void onPause() {
        super.onPause();
        if (dbHandler != null) {
            cursorAdapter.getCursor().close();
            dbHandler.close();
        }
    }*/

    /***
     *
     * Loaders callback methods
     *
     ***/
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.e(TAG, ":::: onCreateLoader");
        return (new GamesCursorLoader(this, dbHandler));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.e(TAG, ":::: onLoadFinished");
        cursorAdapter.swapCursor(cursor);

        /**
         * Registering content observer for this cursor, When this cursor value will be change
         * This will notify our loader to reload its data*/
        CursorObserver cursorObserver = new CursorObserver(new Handler(), loader);
        cursor.registerContentObserver(cursorObserver);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.e(TAG, ":::: onLoaderReset");
        cursorAdapter.swapCursor(null);
    }

}

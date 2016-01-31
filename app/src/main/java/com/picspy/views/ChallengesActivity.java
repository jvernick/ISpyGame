package com.picspy.views;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.dreamfactory.client.ApiException;
import com.picspy.GamesRequests;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.adapters.GamesCursorAdapter;
import com.picspy.firstapp.R;
import com.picspy.models.Game;
import com.picspy.models.UserChallengeRecord;
import com.picspy.models.UserChallengesRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.ChallengesRequests;
import com.picspy.utils.DbContract.GameEntry;
import com.picspy.utils.VolleyRequest;

import java.util.ArrayList;
import java.util.List;

public class ChallengesActivity extends ActionBarActivity  implements LoaderCallbacks<Cursor>{
    public final static String EXTRA_MESSAGE = "com.picspy.firstapp.GAME";
    private static final String TAG = "ChallengesActivity";
    private static final Object CANCEL_TAG = "cancel";
    private ListView listView;
    private DatabaseHandler dbHandler;
    private final static int LOADER_ID = 1;
    ProgressBar progressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        listView = (ListView) findViewById(R.id.challenge_list);
        progressSpinner = (ProgressBar) findViewById(R.id.challenges_progressBar);
        progressSpinner.setVisibility(View.GONE);
        //set cursorAdapter to populate listView from database
        //content observer flag makes the view auto refresh

        dbHandler = DatabaseHandler.getInstance(this);
        GamesCursorAdapter cursorAdapter = new GamesCursorAdapter(getApplicationContext(),
                R.layout.item_challenge,
                null, 0);
        listView.setAdapter(cursorAdapter);

        //setting listener to list item click
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
                game.setSenderId((c.getInt(c.getColumnIndex(GameEntry.COLUMN_NAME_SENDER_ID))));
                game.setId(c.getInt(c.getColumnIndex(GameEntry._ID)));
                game.setSenderUsername(c.getString(c.getColumnIndex(
                        GameEntry.COLUMN_NAME_SENDER_NAME)));

                Toast.makeText(ChallengesActivity.this, game.toString(), Toast.LENGTH_LONG).show();

                //TODO Start activity to display Game
                /*
                    get game in reciepient activity as follows:
                    Bundle Bundle = intent.getExtras();
                    Game game = (game) bundle.getParcelable(ChallengesActivity.EXTRA_MESSAGE);
                 */
                /*
                Intent intent = new Intent(ChallengesActivity.this, SecondActivity.class);
                intent.putExtra(EXTRA_MESSAGE, game);
                startActivity(intent);
                */
            }
        });

        //update database
        getChallenges(false);
        Toolbar toolbar = (Toolbar) findViewById(R.id.challenges_toolbar);

        // Setting toolbar as the ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Challenges");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left);

        //Initializing loader
        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_challenges, menu);
        return true;
    }

    /*
     * Populate the actionbar menu. Currently implements a list refresh button
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //update database
            getChallenges(true);
            return true;
        } else if( id == android.R.id.home) {
            //handling back button click
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Start a new game
     * @param view View from button click
     */
    public void launchCamera(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    /**
     * Gets new challenges from the server
     */
    public void getChallenges(final boolean isRefresh) {
        Response.Listener<UserChallengesRecord> responseListener = new Response.Listener<UserChallengesRecord>() {
            @Override
            public void onResponse(UserChallengesRecord response) {
                progressSpinner.setVisibility(View.GONE);
                if (storeChallenges(response)) {
                    ((GamesCursorAdapter) listView.getAdapter()).changeCursor(dbHandler.getAllGames());
                    ((GamesCursorAdapter) listView.getAdapter()).notifyDataSetChanged();
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
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR) && isRefresh) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                }
            }
        };

        ChallengesRequests gameInfoRequest = ChallengesRequests.getGamesInfo(this, responseListener, errorListener);
        if (gameInfoRequest != null) gameInfoRequest.setTag(CANCEL_TAG);
        VolleyRequest.getInstance(this.getApplicationContext()).addToRequestQueue(gameInfoRequest);
        progressSpinner.setVisibility(View.VISIBLE);
    }

    /**
     * Stores retrieved challenges in the local database
     * @param userChallengesRecord Record containing challenges to be stored
     * @return true if one or more challenge was stored, otherwise false
     */
    private boolean storeChallenges(UserChallengesRecord userChallengesRecord){
        if (userChallengesRecord != null &&  userChallengesRecord.getCount() != 0) {
            List<Game> gameList = new ArrayList<>();
            int max_user_challenge_id = 0;

            for (UserChallengeRecord record: userChallengesRecord.getResource()) {
                if (record.getId() > max_user_challenge_id) {
                    max_user_challenge_id = record.getId();
                }
                gameList.add(record.getGame());
            }

            dbHandler.addGames(gameList, max_user_challenge_id);
            return true;
        }
        return false;
    }

    /***
     *
     * Loaders callback methods
     *
     ***/
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader");
        return new GamesCursorLoader(this, dbHandler);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ((GamesCursorAdapter)listView.getAdapter()).changeCursor(cursor);
        ((GamesCursorAdapter)listView.getAdapter()).notifyDataSetChanged();
        listView.setEmptyView(findViewById(R.id.empty_list));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((GamesCursorAdapter)listView.getAdapter()).changeCursor(null);
    }

    /**
     * Cursor loader to get games from sqlite in background
     */
    public static class GamesCursorLoader extends AsyncTaskLoader<Cursor> {
        private DatabaseHandler dbHandler;

        //Default constructor
        public GamesCursorLoader(Context context, DatabaseHandler dbHandler) {
            super(context);
            this.dbHandler = dbHandler;
        }

        @Override
        public Cursor loadInBackground() {
           return dbHandler.getAllGames();
        }

    }
}

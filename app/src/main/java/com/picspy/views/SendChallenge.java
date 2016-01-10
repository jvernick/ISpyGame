package com.picspy.views;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.dreamfactory.model.FileResponse;
import com.picspy.GamesRequests;
import com.picspy.firstapp.R;
import com.picspy.views.fragments.ChooseFriendsFragment;
import com.picspy.views.fragments.ConfigureChallengeFragment;

import java.io.File;

public class SendChallenge extends ActionBarActivity implements
        ConfigureChallengeFragment.F1FragmentInteractionListener,
        ChooseFriendsFragment.F2FragmentInteractionListener{

    public static final String BDL_GAME_OPTIONS = "bdl_game_options";
    public static final String BDL_PICTURE_OPTIONS = "bdl_picture_options";
    public static final String BDL_FRIEND_OPTIONS = "bdl_friend_options";
    public static final String ARG_FRIEND_ID = "friend_id";
    public static final String ARG_FRIEND_USERNAME = "friend_username";
    private static final String TAG = "SendChallenge";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_challenge);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            ConfigureChallengeFragment configureFragment = ConfigureChallengeFragment.newInstance(
                    getIntent().getBundleExtra(SendChallenge.BDL_PICTURE_OPTIONS),
                    getIntent().getBundleExtra(SendChallenge.BDL_FRIEND_OPTIONS)
            );

            Toolbar toolbar = (Toolbar) findViewById(R.id.challenges_toolbar);

            // Setting toolbar as the ActionBar
            //TODO back button stopped working. temp solution in onOptionsItemSelected
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(R.drawable.ic_chevron_left);

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, configureFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_challenge, menu);
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

    @Override
    public void setToolbarTitle(String title) {
        //TODO causes null pointer
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean startGame(Bundle gameBundle) {
        (new CreateGame(this, gameBundle)).execute();
        return false;
    }

    private class CreateGame extends AsyncTask<Void, Void, String> {
        private final Context context;
        private final Bundle bundle;

        public CreateGame(Context context,Bundle bundle) {
            super();
            this.context = context;
            this.bundle = bundle;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String filename = bundle.getString(GamesRequests.GAME_LABEL.FILE_NAME);
            String filepath = bundle.getString(GamesRequests.GAME_LABEL.FILE_NAME_PATH);

            GamesRequests.ChallengeParams params = new GamesRequests.ChallengeParams(bundle);
            Log.d(TAG, "filename: " + filename);
            Log.d(TAG, "filepath: " + filepath);
            Log.d(TAG, "Params:\n" + params.toString());
            return (new GamesRequests(context, true)).createGame(filename, filepath, params);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            File image = new File(bundle.getString(GamesRequests.GAME_LABEL.FILE_NAME_PATH));
            Log.d(TAG, "image deleted? " + String.valueOf(image.delete()));
            Log.d(TAG, "result: " + result);


        }
    }
}

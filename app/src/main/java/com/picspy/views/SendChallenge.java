package com.picspy.views;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.dreamfactory.model.FileResponse;
import com.picspy.GamesRequests;
import com.picspy.firstapp.R;
import com.picspy.views.fragments.ChooseFriendsFragment;
import com.picspy.views.fragments.ConfigureChallengeFragment;

public class SendChallenge extends ActionBarActivity implements
        ConfigureChallengeFragment.F1FragmentInteractionListener,
        ChooseFriendsFragment.F2FragmentInteractionListener{

    public static final String BDL_GAME_OPTIONS = "bdl_game_options";
    public static final String BDL_PICTURE_OPTIONS = "bdl_picture_options";
    public static final String BDL_FRIEND_OPTIONS = "bdl_friend_options";
    public static final String ARG_FRIEND_ID = "friend_id";
    public static final String ARG_FRIEND_USERNAME = "friend_username";

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
            ConfigureChallengeFragment firstFragment = new ConfigureChallengeFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            Toolbar toolbar = (Toolbar) findViewById(R.id.challenges_toolbar);
            TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
            //toolbarTitle.setText("Challenges");

            // Setting toolbar as the ActionBar
            //TODO back button stopped working. temp solution in onOptionsItemSelected
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(R.drawable.ic_chevron_left);

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
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
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean startGame(Bundle gameBundle) {
        (new CreateGame(this)).execute(gameBundle);
        return false;
    }

    private class CreateGame extends AsyncTask<Bundle, Void, String> {
        private final Context context;

        public CreateGame(Context context) {
            super();
            this.context = context;
        }

        @Override
        protected String doInBackground(Bundle... bundles) {
            String filename = bundles[0].getString(GamesRequests.GAME_LABEL.FILE_NAME);
            String filepath = bundles[0].getString(GamesRequests.GAME_LABEL.FILE_NAME_PATH);

            GamesRequests.ChallengeParams params = new GamesRequests.ChallengeParams(bundles[0]);
            return (new GamesRequests(context, true)).createGame(filename, filepath, params);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}

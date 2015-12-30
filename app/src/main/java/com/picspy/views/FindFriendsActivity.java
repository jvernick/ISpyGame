package com.picspy.views;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.picspy.FriendsTableRequests;
import com.picspy.adapters.FindFriendsArrayAdapter;
import com.picspy.adapters.TopFragmentArrayAdapter;
import com.picspy.firstapp.R;
import com.picspy.models.Friend;
import com.picspy.models.FriendRecord;
import com.picspy.models.FriendsRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import java.util.ArrayList;
import java.util.List;

public class FindFriendsActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<List<Friend>> {
    private ListView listView;
    private final static int LOADER_ID = 1;

    private FindFriendsArrayAdapter arrayAdapter;
    private static final String TAG = "FindFriendsActivity";
    private static int myUserId;
    private ProgressBar progressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        listView = (ListView) findViewById(R.id.request_list);
        //arrayAdapter = new FindFriendsArrayAdapter(this,)

        Toolbar toolbar = (Toolbar) findViewById(R.id.find_friend_toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Add Friends");

        // Setting toolbar as the ActionBar
        //TODO back button stopped working. temp solution in onOptionsItemSelected
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        myUserId = PrefUtil.getInt(getApplicationContext(), AppConstants.USER_ID);
        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();

        progressSpinner = (ProgressBar)findViewById(R.id.progressBar);
        progressSpinner.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_friend, menu);
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
        } else if( id == android.R.id.home) {
            //handling back button click
            onBackPressed();
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

    @Override
    public Loader<List<Friend>> onCreateLoader(int i, Bundle bundle) {
        return  new FriendRequestLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Friend>> loader, List<Friend> friends) {
        progressSpinner.setVisibility(View.GONE);
        if (arrayAdapter == null) {
           listViewInit(new FindFriendsArrayAdapter(this, friends));
        } else {
            arrayAdapter.setData(friends);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    private void listViewInit(FindFriendsArrayAdapter findFriendsArrayAdapter) {
        arrayAdapter = findFriendsArrayAdapter;
        listView.setEmptyView(findViewById(R.id.empty_list));
        listView.setAdapter(findFriendsArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //TODO start friend_info_activity
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Starting friend_info activity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<List<Friend>> loader) {
        listView.setAdapter(null);
    }

    //TODO implement to search server for user_id
    public void findUser(View view) {
        Toast.makeText(this, "Finding friends", Toast.LENGTH_SHORT).show();
    }

    public static class FriendRequestLoader extends AsyncTaskLoader<List<Friend>> {
        public FriendRequestLoader(Context context) {
            super(context);
        }


        @Override
        public List<Friend> loadInBackground() {
            final String[] animals = new String[] { "Ape", "Bird", "Cat", "Dog", "Elephant","Fox",
                    "Gorilla", "Hyena", "Inch Worm", "Jackalope", "King Salmon","Lizard"};
            final List<Friend> requestList = new ArrayList<>();
            for (int i = 0; i < animals.length; ++i) {
                requestList.add(new Friend(i,animals[i]));
                try {
                    Thread.sleep(100); //simulated network delay
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            FriendsRecord records = (new FriendsTableRequests(getContext())).getFriendRequests();
            if (records != null) {
                List<FriendRecord> requestRecords = records.getRecord();
                for (FriendRecord record : requestRecords) {
                    requestList.add(record.getOtherUserRecord(myUserId).getRecordToFriend());
                }
            }

            return requestList;
        }
    }
}

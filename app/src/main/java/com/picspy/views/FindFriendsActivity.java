package com.picspy.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.picspy.adapters.SlidingTabLayout;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.VolleyRequest;
import com.picspy.views.fragments.FriendRequestsFragment;
import com.picspy.views.fragments.FriendSearchFragment;

import java.util.ArrayList;

public class FindFriendsActivity extends ActionBarActivity {
    public static final String CANCEL_TAG = "cancelFindRequests";
    public static final String EXTRA_START_FRAGMENT = "com.picspy.views.friend.startFragment";
    private static final String TAG = "FindFriendsAct";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        // get intent
        int startFragment = getIntent().getIntExtra(EXTRA_START_FRAGMENT, 0);
        startFragment = (startFragment != 1 && startFragment != 0) ? 0 : startFragment;
        Log.d(TAG, "startFragment: " + startFragment);

        // reset notification counter
        PrefUtil.putInt(this, AppConstants.FRIEND_REQUEST_COUNT, 0);
        boolean isNotf = getIntent().getBooleanExtra(FriendRequestsFragment.ARG_NOTF, false);

        // Set up the action bar.
        //final ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // create a fragment list in order.
        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(FriendRequestsFragment.newInstance(isNotf, startFragment));
        fragments.add(FriendSearchFragment.newInstance(startFragment));


        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), fragments);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(startFragment);
        if (startFragment == 1) {
            Log.d(TAG, "startFragment == 0");

            InputMethodManager imgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected: " + position);
                if (position == 1) {
                    ((FriendSearchFragment) fragments.get(1)).showKeyboard();
                } else {
                    ((FriendSearchFragment) fragments.get(1)).hideKeyboard();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Define SlidingTabLayout (shown at top)
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.find_friends_tab);
        // make sure the tabs are equally spaced.
        slidingTabLayout.setDistributeEvenly(true);
        // Setting custom color for scroll bar indicator
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        slidingTabLayout.setViewPager(mViewPager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.find_friend_toolbar);

        //Setting toolbar as the ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left);
    }

    /**
     * Handle onNewIntent() to inform the fragment manager that the
     * state is not saved.  If you are handling new intents and may be
     * making changes to the fragment state, you want to be sure to call
     * through to the super-class here first.  Otherwise, if your state
     * is saved but the activity is not stopped, you could get an
     * onNewIntent() call which happens before onResume() and trying to
     * perform fragment operations at that point will throw IllegalStateException
     * because the fragment manager thinks the state is still saved.
     *
     * @param intent intent from caller
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        PrefUtil.putInt(this, AppConstants.FRIEND_REQUEST_COUNT, 0);
        FriendRequestsFragment requestsFragment = (FriendRequestsFragment)
                getSupportFragmentManager().findFragmentByTag(
                        mSectionsPagerAdapter.fragments.get(0).getTag());
        if (requestsFragment != null) {
            requestsFragment.refresh();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_friends, menu);
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

    /**
     * Start a new game
     *
     * @param view View from button click
     */
    public void launchCamera(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //cancel all pending register/login/addUser tasks
        if (VolleyRequest.getInstance(this.getApplicationContext()) != null) {
            VolleyRequest.getInstance(this.getApplication()).getRequestQueue().cancelAll(CANCEL_TAG);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public static final int REQUESTS = 0;
        public static final int SEARCH = 1;
        public static final String UI_TAB_REQUESTS = "REQUESTS";
        public static final String UI_TAB_SEARCH = "SEARCH";
        private ArrayList<Fragment> fragments;

        public SectionsPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case REQUESTS:
                    return UI_TAB_REQUESTS;
                case SEARCH:
                    return UI_TAB_SEARCH;
                default:
                    break;
            }
            return null;
        }
    }
}

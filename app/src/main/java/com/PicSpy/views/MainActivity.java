package com.picspy.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.picspy.firstapp.R;
import com.picspy.views.Fragments.FriendsFragment;
import com.picspy.views.Fragments.HomeFragment;
import com.picspy.views.Fragments.MenuFragment;
import com.picspy.views.Fragments.TabsViewPagerAdapter;
import com.picspy.views.Fragments.TopFragment;
import com.picspy.adapters.SlidingTabLayout;

import java.util.ArrayList;

//TODO: this is currently modified to show the result of user registration
/**
 * Main page activity
 */
public class MainActivity extends FragmentActivity {
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private ArrayList<Fragment> fragments;
    private TabsViewPagerAdapter myViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define SlidingTabLayout (shown at top)
        // and ViewPager (shown at bottom) in the layout.
        // Get their instances.
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tab);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        // create a fragment list in order.
        fragments = new ArrayList<>();
        fragments.add(new FriendsFragment());
        fragments.add(new TopFragment());

        // use FragmentPagerAdapter to bind the slidingTabLayout (tabs with different titles)
        // and ViewPager (different pages of fragment) together.
        myViewPagerAdapter = new TabsViewPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(myViewPagerAdapter);

        // make sure the tabs are equally spaced.
        slidingTabLayout.setDistributeEvenly(true);

        // Setting custom color for scroll bar indicator
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });


        slidingTabLayout.setViewPager(viewPager);
    }


    /* No menu for now
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
    */

    /**
     * Starts camera activity
     * @param view View from button click
     */
    public void launchCamera(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}

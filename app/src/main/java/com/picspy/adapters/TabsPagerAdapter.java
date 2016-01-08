package com.picspy.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.picspy.views.fragments.FriendSearchFragment;
import com.picspy.views.fragments.FriendsFragment;
/**
 * Created by Justin12 on 6/6/2015.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                // Friends fragment activity
                return new FriendsFragment();
            case 1:
                // Bored fragment activity
                return new FriendSearchFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }
}

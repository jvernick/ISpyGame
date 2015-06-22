package com.picspy.views.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by BrunelAmC on 6/21/2015.
 */
public class TabsViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;

    public static final int HOME = 0;
    public static final int FRIENDS = 1;
    public static final int TOP = 2;
    public static final int MENU = 3;
    public static final String UI_TAB_HOME = "HOME";
    public static final String UI_TAB_FRIENDS = "FRIENDS";
    public static final String UI_TAB_TOP = "TOP";
    public static final String UI_TAB_MENU = "MENU";

    public TabsViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments){
        super(fm);
        this.fragments = fragments;
    }

    public Fragment getItem(int pos){
        return fragments.get(pos);
    }

    public int getCount(){
        return fragments.size();
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case HOME:
                return UI_TAB_HOME;
            case FRIENDS:
                return UI_TAB_FRIENDS;
            case TOP:
                return UI_TAB_TOP;
            case MENU:
                return UI_TAB_MENU;
            default:
                break;
        }
        return null;
    }
}

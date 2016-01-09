package com.picspy.views.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.picspy.GamesRequests;
import com.picspy.adapters.ChooseFriendsCursorAdapter;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.firstapp.R;
import com.picspy.views.SendChallenge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link F2FragmentInteractionListener}
 * interface.
 */
public class ChooseFriendsFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_HINT = "hint";
    private static final String ARG_GUESSES = "guesses";

    public static final String ARG_POSITION = "position";
    private static final String BDL_IS_SEARCH = "isSearch?";
    private static final String BDL_SEARCH_STRING = "searchString";
    private static final String TAG = "ChooseFriends";
    private static  LoaderManager.LoaderCallbacks<Cursor> callback;
    private static final int LOADER_ID = 0;
    private SearchView searchView;
    private TextView emptySearchView;
    private TextView noFriendView;
    private boolean firstQuery = true;
    private boolean noFriends = false;

    //bundle received from ConfigureChallengeFragment that stores game options
    //ie: hint, time, guesses, leaderboard?
    private Bundle gameOptionsBundle;
    //bundle received from forwarded from ConfigureChallengeFragment that store picture options
    //ie: file_name and selction.
    private Bundle pictureOptionsBundle;

    private F2FragmentInteractionListener mListener;

    //list adapter
    private ChooseFriendsCursorAdapter cursorAdapter;

    public static ChooseFriendsFragment newInstance(Bundle gameOptionsBundle,
                                                    Bundle pictureOptionsBundle) {
        ChooseFriendsFragment fragment = new ChooseFriendsFragment();
        Bundle args = new Bundle();
        args.putBundle(SendChallenge.BDL_GAME_OPTIONS, gameOptionsBundle);
        args.putBundle(SendChallenge.BDL_PICTURE_OPTIONS, pictureOptionsBundle);

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChooseFriendsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            gameOptionsBundle = getArguments().getBundle(SendChallenge.BDL_GAME_OPTIONS);
            pictureOptionsBundle = getArguments().getBundle(SendChallenge.BDL_PICTURE_OPTIONS);
        }

        cursorAdapter = new ChooseFriendsCursorAdapter(getActivity(),
              R.layout.item_choose_friend, null, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setListAdapter(cursorAdapter);
        View rootView = inflater.inflate(R.layout.fragment_choose_friends_list, container, false);
        noFriendView = (TextView) rootView.findViewById(R.id.no_friends);
        emptySearchView = (TextView) rootView.findViewById(R.id.search_empty);

        callback = this;
        Bundle data = new Bundle();
        data.putBoolean(ChooseFriendsFragment.BDL_IS_SEARCH, false);
        getLoaderManager().restartLoader(LOADER_ID, data, callback).forceLoad();
        mListener.setToolbarTitle("Send to..");
        return rootView;
    }

    /**
     * This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_choose_friends, menu);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        if (searchView != null) {
            setupSearchView();
        }
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case (R.id.action_settings):
                return true;
            case (android.R.id.home):
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createGame() {
        Bundle finalBundle = new Bundle();
        int[] friendIds = new int[cursorAdapter.getCheckedFriends().size()];
        int i = 0;
        for (Integer friend_id: cursorAdapter.getCheckedFriends()) {
            friendIds[i] = friend_id;
            i++;
        }

        finalBundle.putAll(gameOptionsBundle);
        finalBundle.putAll(pictureOptionsBundle);
        finalBundle.putIntArray(GamesRequests.GAME_LABEL.FRIENDS, friendIds);

        boolean result = mListener.startGame(finalBundle);
        if (result) {
            Log.d(TAG, "Game sent successfully");
        } else {
            Log.d(TAG, "Error sending game");
        }
    }
    private void setupSearchView() {
        SearchManager searchManager = (SearchManager)
                getActivity().getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(getActivity(), SendChallenge.class);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);


        // Change background line
        View searchplate = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        if (searchplate != null) {
            searchplate.setBackgroundResource(R.drawable.horizontal_divider_transparent);
        }
        // Change close icon color
        ImageView searchCloseIcon = (ImageView)searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        if (searchCloseIcon != null) {
            searchCloseIcon.setImageResource(R.drawable.ic_close_white);
        }

    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Toast.makeText(getActivity().getApplicationContext(), cursorAdapter.getCheckedFriends().toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (F2FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement F2FragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //TODO add spinner?
        return new FriendLoader(getActivity(), 
                args.getBoolean(ChooseFriendsFragment.BDL_IS_SEARCH),
                args.getString(ChooseFriendsFragment.BDL_SEARCH_STRING, null));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((ChooseFriendsCursorAdapter) getListAdapter()).changeCursor(data);
        ((ChooseFriendsCursorAdapter) getListAdapter()).notifyDataSetChanged();
        if (firstQuery) {
            firstQuery = false;
            ListView listView = getListView();
            if (data.getCount() == 0) {
                listView.setEmptyView(noFriendView);
                noFriends = true;
            } else {
                listView.setEmptyView(emptySearchView);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((ChooseFriendsCursorAdapter) getListAdapter()).changeCursor(null);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Bundle data = new Bundle();
        data.putBoolean(ChooseFriendsFragment.BDL_IS_SEARCH, true);
        data.putString(ChooseFriendsFragment.BDL_SEARCH_STRING, s);
        getLoaderManager().restartLoader(LOADER_ID, data, callback).forceLoad();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (!noFriends) {
            emptySearchView.setText( "\"" +s + "\" not found");
        }
        Bundle data = new Bundle();
        data.putBoolean(ChooseFriendsFragment.BDL_IS_SEARCH, true);
        data.putString(ChooseFriendsFragment.BDL_SEARCH_STRING, s);
        getLoaderManager().restartLoader(LOADER_ID, data, callback).forceLoad();
        return false;
    }

    @Override
    public boolean onClose() {
        Bundle data = new Bundle();
        data.putBoolean(ChooseFriendsFragment.BDL_IS_SEARCH, false);
        getLoaderManager().restartLoader(LOADER_ID, data, callback).forceLoad();
        return false;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface F2FragmentInteractionListener {
        void setToolbarTitle(String title);
        boolean startGame(Bundle finalBundle);
    }

    public static class FriendLoader extends AsyncTaskLoader<Cursor> {
        private final boolean isSearch;
        private final String constraint;

        public FriendLoader(Context context, boolean isSearch, String constraint) {
            super(context);
            this.isSearch = isSearch;
            this.constraint = constraint;
        }

        @Override
        public Cursor loadInBackground() {
            if (isSearch) {
                return DatabaseHandler.getInstance(getContext()).getMatchingFriends(constraint);
            } else {
                return DatabaseHandler.getInstance(getContext()).getAllFriends();
            }
        }
    }
}

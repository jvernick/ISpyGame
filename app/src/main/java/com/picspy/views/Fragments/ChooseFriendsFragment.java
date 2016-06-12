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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.picspy.adapters.ChooseFriendsCursorAdapter;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.firstapp.R;
import com.picspy.utils.ChallengesRequests;
import com.picspy.views.SendChallenge;

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
        SearchView.OnCloseListener, ChooseFriendsCursorAdapter.EmptyCheckedListener {
    private static final String BDL_IS_SEARCH = "isSearch?";
    private static final String BDL_SEARCH_STRING = "searchString";
    private static final String TAG = "ChooseFriends";
    private static  LoaderManager.LoaderCallbacks<Cursor> callback;
    private static final int LOADER_ID = 0;
    private SearchView mSearchView;
    private TextView emptySearchView;
    private TextView noFriendView;
    private boolean firstQuery = true;
    private boolean noFriends = false;

    //bundle received from ConfigureChallengeFragment that stores game options
    //ie: hint, time, guesses, leaderboard?
    private Bundle gameOptionsBundle;
    //bundle received from forwarded from ConfigureChallengeFragment that store picture options
    //ie: file_name and selection.
    private Bundle pictureOptionsBundle;

    private F2FragmentInteractionListener mListener;

    //list adapter
    private ChooseFriendsCursorAdapter cursorAdapter;
    private Animation animBarUp;
    private Animation animBarDOWN;
    private Button sendButton;
    private ViewGroup nextView;
    private MenuItem mSearchItem;

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
              R.layout.item_choose_friend, null, 0, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setListAdapter(cursorAdapter);
        View rootView = inflater.inflate(R.layout.fragment_choose_friends_list, container, false);

        noFriendView = (TextView) rootView.findViewById(R.id.no_friends);
        emptySearchView = (TextView) rootView.findViewById(R.id.search_empty);

        animBarUp = AnimationUtils.loadAnimation(getActivity(), R.anim.send_challenge_bar_up);
        animBarDOWN = AnimationUtils.loadAnimation(getActivity(),
                R.anim.send_challenge_bar_down);

        sendButton = (Button) rootView.findViewById(R.id.send_button);
        nextView = (ViewGroup) rootView.findViewById(R.id.next_view);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGame();
            }
        });

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_choose_friends, menu);
        mSearchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) mSearchItem.getActionView();

        if (mSearchView != null) {
            setupSearchView();
        }
    }

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
        finalBundle.putIntArray(ChallengesRequests.GAME_LABEL.FRIENDS, friendIds);

        mListener.startGame(finalBundle);
    }
    private void setupSearchView() {
        SearchManager searchManager = (SearchManager)
                getActivity().getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(getActivity(), SendChallenge.class);
        if (searchManager != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        }
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);

        //remove search Icon
        ImageView magImage = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        // Change background line
        View searchplate = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        if (searchplate != null) {
            searchplate.setBackgroundResource(R.drawable.horizontal_divider_transparent);
        }
        // Change close icon color TODO Not neeted, remove
        /*ImageView searchCloseIcon = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        if (searchCloseIcon != null) {
            searchCloseIcon.setImageResource(R.drawable.ic_close_white);
        }*/

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

    // [ SearchView configuration ]
    @Override
    public boolean onQueryTextSubmit(String s) {
        Bundle data = new Bundle();
        data.putBoolean(ChooseFriendsFragment.BDL_IS_SEARCH, true);
        data.putString(ChooseFriendsFragment.BDL_SEARCH_STRING, s);
        getLoaderManager().restartLoader(LOADER_ID, data, callback).forceLoad();
        mSearchItem.collapseActionView();
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

    // [ End SearchView configuration ]

    /**
     * Called when the set of selected friends changes size from 0 to one
     * @param isEmpty true if Set of selected friends is empty, otherwise false
     */
    @Override
    public void isEmpty(Boolean isEmpty) {
        if (isEmpty) {
            nextView.startAnimation(animBarDOWN);
            nextView.setVisibility(View.INVISIBLE);
        } else {
            nextView.startAnimation(animBarUp);
            nextView.setVisibility(View.VISIBLE);
        }
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
        void startGame(Bundle finalBundle);
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

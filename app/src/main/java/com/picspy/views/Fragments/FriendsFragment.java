package com.picspy.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.picspy.FriendsTableRequests;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.adapters.FriendsCursorAdapter;
import com.picspy.firstapp.R;
import com.picspy.models.Friend;
import com.picspy.models.FriendRecord;
import com.picspy.models.FriendsRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.views.FindFriendsActivity;
import com.picspy.views.SearchEditTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin12 on 6/6/2015.
 */
public class FriendsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "FriendsFragment";
    private static final int LOADER_ID = 0;
    private FriendsCursorAdapter cursorAdapter;
    private View listHeader;
    private EditText searchField;
    private ProgressBar progressSpinner;
    private boolean firstQuery = true;
    private boolean noFriend = false;

    private TextView emptySearchView;
    private TextView noFriendView;

    /**
     * Static factory method that takes an int parameter,
     * initializes the fragment's arguments, and returns the
     * new fragment to the client.
     */
    public static FriendsFragment newInstance(int index) {
        FriendsFragment f = new FriendsFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        return f;
    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     *  #onAttach(Activity)} and before
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * <p/>
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cursorAdapter = new FriendsCursorAdapter(getActivity(), R.layout.item_friends, null, 0);
        setListAdapter(cursorAdapter);
        (new GetFriends(getActivity())).execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        noFriendView = (TextView) rootView.findViewById(R.id.no_friends);
        emptySearchView = (TextView) rootView.findViewById(R.id.search_empty);

        ImageView findFriend = (ImageView) rootView.findViewById(R.id.find_friend);
        findFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity()
                        .getApplicationContext(), FindFriendsActivity.class);
                startActivity(intent);
            }
        });

        progressSpinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressSpinner.setVisibility(View.GONE);
        return rootView;
    }


    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final LoaderManager.LoaderCallbacks<Cursor> callback = this;

        cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                // Search for friends whose names begin with the specified letters.
                Cursor cursor = DatabaseHandler.getInstance(getActivity()
                        .getApplicationContext()).getMatchingFriends(
                        (constraint != null ? constraint.toString() : null));
                return cursor;
            }
        });

        if (getView() !=  null) listHeader = getView().findViewById(R.id.friend_list_header);
        if (listHeader != null) {
            View searchBox = listHeader.findViewById(R.id.search_box);
            //TODO remove. next two lines for testing
            SearchEditTextView searchEditTextView = (SearchEditTextView) listHeader.findViewById(R.id.search_box);
            searchEditTextView.setButtonClickListener(new SearchEditTextView.OnButtonClickListener() {
                @Override
                public void onEvent() {
                }
            });
            if (searchBox != null) {
                searchField = (EditText) searchBox.findViewById(R.id.clearable_edit);
            }
            if (searchField != null) {
                setSearchFieldFilter(searchField);
            }
        }


       /* //change list empty text if the list is empty. Default values relates to empty searches
        if (cursorAdapter.isEmpty()){
            View rootview = getView();
            if ( rootview != null) {
                TextView emptyText = (TextView)
                        rootview.findViewWithTag("friends_fragment_empty_list");
                if (emptyText != null) {
                    emptyText.setText(R.string.friends_fragment_no_friends);
                }
            }
        }*/

        getLoaderManager().restartLoader(LOADER_ID, null, callback).forceLoad();
    }

    private void setSearchFieldFilter (final EditText searchText) {
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                if (cs.length() == 0) {
                    searchText.clearFocus();
                    if (getView() != null) getView().requestFocus();
                    InputMethodManager mgr = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                }
                cursorAdapter.getFilter().filter(cs);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     */
    @Override
    public void onResume() {
        super.onResume();

        //clear search field
        if (searchField != null && searchField.getText().length() == 0) {
            searchField.clearFocus();
            searchField.setText("");
        }
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
        progressSpinner.setVisibility(View.VISIBLE);
        return new FriendLoader(getActivity());
    }

    /**
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.changeCursor(data);
        cursorAdapter.notifyDataSetChanged();
        if (firstQuery) {
            Log.d(TAG, "firstQuery " + data.getCount());
            ListView listView = getListView();

            firstQuery = false;
            if(data.getCount() == 0) {
                noFriend = true;
                listView.setEmptyView(noFriendView);
            } else {
                listView.setEmptyView(emptySearchView);
            }
        }
        progressSpinner.setVisibility(View.GONE);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((FriendsCursorAdapter) getListAdapter()).changeCursor(null);
    }

    private static class FriendLoader extends AsyncTaskLoader<Cursor> {

        public FriendLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
           return  DatabaseHandler.getInstance(getContext()).getAllFriends();
        }
    }

    private class GetFriends extends AsyncTask<Void,Void,Boolean> {

        private final Context context;

        public GetFriends(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                FriendsRecord result = (new FriendsTableRequests(context))
                        .getFriends(PrefUtil.getInt(context, AppConstants.MAX_FRIEND_RECORD_ID));
                ArrayList<Friend> friends = new ArrayList<>();
                int myUserId = PrefUtil.getInt(context, AppConstants.USER_ID);
                if (result != null) {
                    for (FriendRecord friendRecord : result.getRecord()) {
                        friends.add(friendRecord.getFriend(myUserId));
                    }
                }
                if (friends.size() != 0)
                    DatabaseHandler.getInstance(context).addFriends(friends);
                return friends.size() !=0;
            } catch (Exception ex) { //TODO handle exceptions better
                Log.d(TAG + "test", ex.getMessage());
                //TODO handle exception
                //toast for Internet connection error
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean foundFriend) {
            if (foundFriend) {
                Log.d(TAG, "notifying");
                ((FriendsCursorAdapter) getListAdapter())
                        .changeCursor(DatabaseHandler.getInstance(context).getAllFriends());
                ((FriendsCursorAdapter) getListAdapter()).notifyDataSetChanged();
            }
        }
    }
}


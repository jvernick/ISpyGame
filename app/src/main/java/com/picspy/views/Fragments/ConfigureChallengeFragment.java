package com.picspy.views.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.utils.ChallengesRequests;
import com.picspy.views.SendChallenge;

/**
 * A placeholder fragment containing a simple view.
 */
public class ConfigureChallengeFragment extends Fragment {
    private static final String TAG = "ConfigureChallenge";
    public static final int MIN_TIME = 3;
    public static final int MIN_GUESSES = 1;
    private final int DEFAULT_TIME = 10, DEFAULT_GUESSES = 3;
    private TextView numGuessesView, numTimeView, characterCountView;
    private F1FragmentInteractionListener mListener;
    // the fragment initialization parameters
    //bundles from camera activity
    //contains: selection and filename
    private Bundle pictureOptionsBundle;
    private int friend_id;

    public ConfigureChallengeFragment() {
    }

    public static ConfigureChallengeFragment newInstance(Bundle pictureOptionsBundle,
                                                         int friend_id) {
        ConfigureChallengeFragment fragment = new ConfigureChallengeFragment();
        Bundle args = new Bundle();
        args.putInt(SendChallenge.ARG_FRIEND_ID, friend_id);
        args.putBundle(SendChallenge.BDL_PICTURE_OPTIONS, pictureOptionsBundle);

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Activity)} and before
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            pictureOptionsBundle = getArguments().getBundle(SendChallenge.BDL_PICTURE_OPTIONS);
            friend_id = getArguments().getInt(SendChallenge.ARG_FRIEND_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //TODO configure custom font
        View rootView = inflater.inflate(R.layout.fragment_configure_challenge, container, false);
        final CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.checkbox);
        final EditText hintBox = (EditText) rootView.findViewById(R.id.hint_input);
        final SeekBar guessSeekBar = (SeekBar) rootView.findViewById(R.id.guess_seek_bar);
        final SeekBar timeSeekBar = (SeekBar) rootView.findViewById(R.id.time_seek_bar);

        numGuessesView = (TextView) rootView.findViewById(R.id.num_guesses);
        numTimeView = (TextView) rootView.findViewById(R.id.num_time);
        characterCountView = (TextView) rootView.findViewById(R.id.character_count);
        numGuessesView.setText(String.valueOf(DEFAULT_GUESSES));
        numTimeView.setText(String.valueOf(DEFAULT_TIME));

        Button nextButton = (Button) rootView.findViewById(R.id.next_button);
        if (friend_id != -1)
            nextButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_send));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle gameOptions = new Bundle();
                gameOptions.putInt(ChallengesRequests.GAME_LABEL.GUESSES, guessSeekBar.getProgress() + MIN_GUESSES);
                gameOptions.putInt(ChallengesRequests.GAME_LABEL.TIME, timeSeekBar.getProgress() + MIN_TIME);
                gameOptions.putString(ChallengesRequests.GAME_LABEL.HINT, hintBox.getText().toString());
                gameOptions.putBoolean(ChallengesRequests.GAME_LABEL.LEADERBOARD, checkBox.isChecked());

                //user already selected friends
                if (friend_id != -1) {
                    createGame(gameOptions);
                } else {
                    startNextFragment(gameOptions);
                }
            }
        });

        hintBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int len = editable.length();

                characterCountView.setText(getString(R.string.character_count_value, len));
                if (len > 40) {
                    characterCountView.setTextColor(getResources().getColor(R.color.primary_red));
                } else {
                    characterCountView.setTextColor(getResources().getColor(R.color.secondary_text_default_material_light));
                }
            }
        });

        guessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                numGuessesView.setText(String.valueOf(progressValue + MIN_GUESSES));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                numTimeView.setText(String.valueOf(progressValue + MIN_TIME));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return rootView;
    }

    private void createGame(Bundle gameOptions) {
        int[] friendId = {friend_id};
        Bundle finalBundle = new Bundle();
        finalBundle.putIntArray(ChallengesRequests.GAME_LABEL.FRIENDS, friendId);
        finalBundle.putAll(gameOptions);
        finalBundle.putAll(pictureOptionsBundle);
        mListener.startGame(finalBundle);
    }

    private void startNextFragment(Bundle gameOptions) {
        ChooseFriendsFragment chooseFriendsFragment =
                ChooseFriendsFragment.newInstance(gameOptions, pictureOptionsBundle);

        FragmentTransaction transaction =
                getActivity().getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, chooseFriendsFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    //Added on implement back button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d(TAG, "button clicked: " + id);
        switch (id) {
            case (android.R.id.home):
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.setToolbarTitle(getString(R.string.title_fragment_configure_challenge));
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (F1FragmentInteractionListener) activity;
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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface F1FragmentInteractionListener {
        void setToolbarTitle(String title);

        void startGame(Bundle gameBundle);
    }
}

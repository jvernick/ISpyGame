package com.picspy.views.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.picspy.firstapp.R;
import com.picspy.utils.ChallengesRequests;
import com.picspy.views.SendChallenge;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class ConfigureChallengeFragment extends Fragment {
    private static final String TAG = "ConfigureChallenge";
    private final int defaultTime = 5, defaultGuesses = 3;
    private int currTime, currGuesses;
    private TextView numGuessesView;
    private F1FragmentInteractionListener mListener;
    // the fragment initialization parameters
    //bundles from camera activity
    //contains: selection and filename
    private Bundle pictureOptionsBundle;
    private int friend_id;
    private SeekBar seekBar;

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
        final RadioGroup timeBox = (RadioGroup) rootView.findViewById(R.id.time_radio_group);
        //final RadioGroup guessBox = (RadioGroup) rootView.findViewById(R.id.guesses_radio_group);
        seekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        numGuessesView = (TextView) rootView.findViewById(R.id.num_guesses);

        Button nextButton = (Button) rootView.findViewById(R.id.next_button);
        if (friend_id != -1)
            nextButton.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_send));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle gameOptions = new Bundle();
                //gameOptions.putInt(ChallengesRequests.GAME_LABEL.GUESSES, guessBox.getCheckedRadioButtonId());
                gameOptions.putInt(ChallengesRequests.GAME_LABEL.TIME, timeBox.getCheckedRadioButtonId());
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

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                currGuesses = progressValue;
                numGuessesView.setText(String.valueOf(currGuesses));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final int checked_color_selected = getResources().getColor(R.color.primary_text);
        final int checked_color_unselected = getResources().getColor(R.color.grey_400);

        AttributeSet attributeSet = getButtonAttributes();
        setupTimeButtons(rootView, checked_color_selected, checked_color_unselected, attributeSet);
        //setupGuessButtons(rootView, checked_color_selected, checked_color_unselected, attributeSet);


        mListener.setToolbarTitle("Game options..");
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

    private AttributeSet getButtonAttributes() {
        XmlPullParser parser = getResources().getLayout(R.layout.view_radio_btn);
        AttributeSet attributes = Xml.asAttributeSet(parser);
        int state = 0;
        do {
            try {
                state = parser.next();
            } catch (XmlPullParserException | IOException e1) {
                e1.printStackTrace();
            }
            if (state == XmlPullParser.START_TAG) {
                if (parser.getName().equals("RadioButton")) {
                    attributes = Xml.asAttributeSet(parser);
                    break;
                }
            }
        } while (state != XmlPullParser.END_DOCUMENT);

        return attributes;
    }

    /**
     * Sets up the options for the game time
     *
     * @param rootView                 The root view
     * @param checked_color_selected   Text color when number is selected
     * @param checked_color_unselected Default text color
     * @param attributes               RadioButton attributes used to inflate each number's radio button
     */
    private void setupTimeButtons(View rootView, final int checked_color_selected,
                                  final int checked_color_unselected, AttributeSet attributes) {
        ViewGroup horScrollLayout = (ViewGroup) rootView.findViewById(R.id.time_radio_group);

        for (int i = 5; i <= 30; i += 5) {
            RadioButton currButton = new RadioButton(getActivity().getApplicationContext(),
                    attributes, R.style.radio_button);
            currButton.setId(i);
            currButton.setText(Integer.toString(i));
            if (i == defaultTime) currButton.setTextColor(checked_color_selected);
            currButton.setChecked(i == defaultTime); // default time
            horScrollLayout.addView(currButton);

            currButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((RadioGroup) view.getParent()).check(view.getId());
                }
            });

            currButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        compoundButton.setTextColor(checked_color_selected);
                    } else {
                        compoundButton.setTextColor(checked_color_unselected);
                    }
                }
            });
        }
    }

    /**
     * Sets up the options for number of guesses
     *
     * @param rootView                 The root view
     * @param checked_color_selected   Text color when number is selected
     * @param checked_color_unselected Default text color
     * @param attributes               RadioButton attributes used to inflate each number's radio button
     */
/*
    private void setupGuessButtons(View rootView, final int checked_color_selected,
                                   final int checked_color_unselected, AttributeSet attributes) {
        ViewGroup horScrollLayout = (ViewGroup) rootView.findViewById(R.id.guesses_radio_group);

        for (int i = 1; i <= 5; i++) {
            RadioButton currButton = new RadioButton(getActivity().getApplicationContext(),
                    attributes, R.style.radio_button);
            currButton.setId(i);
            currButton.setText(Integer.toString(i));
            if (i == defaultGuesses) {
                currButton.setTextColor(checked_color_selected);
            }
            currButton.setChecked(i == defaultGuesses);
            horScrollLayout.addView(currButton);

            currButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((RadioGroup) view.getParent()).check(view.getId());
                }
            });

            currButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        compoundButton.setTextColor(checked_color_selected);
                    } else {
                        compoundButton.setTextColor(checked_color_unselected);
                    }
                }
            });
        }
    }
*/

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
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
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

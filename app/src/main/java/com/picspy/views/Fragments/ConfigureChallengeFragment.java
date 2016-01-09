package com.picspy.views.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.picspy.GamesRequests;
import com.picspy.firstapp.R;
import com.picspy.views.SendChallenge;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class ConfigureChallengeFragment extends Fragment {
    private static final String TAG = "ConfigureChallenge";
    private F1FragmentInteractionListener mListener;

    private final int defaultTime = 5;
    private final int defaultGuesses = 3;
    private final boolean defaultLeaderboard = false;
    private final String defaultHint = "";
    /**
     * gaameOptionsBundle elements
     */
    private int configuredTime = defaultTime;
    private int configuredGuesses = defaultGuesses;
    private String configuredHint = defaultHint;
    private boolean configuredLeaderboard = defaultLeaderboard;

    // the fragment initialization parameters
    //bundles from camera activity
    //contains: selection and filename
    private Bundle pictureOptionsBundle;
    private Bundle friendOptionsBundle;

    public ConfigureChallengeFragment() {
    }

    public static ConfigureChallengeFragment newInstance(Bundle pictureOptionsBundle,
                                                         Bundle friendOptionBundle) {
        ConfigureChallengeFragment fragment = new ConfigureChallengeFragment();
        Bundle args = new Bundle();
        args.putBundle(SendChallenge.BDL_FRIEND_OPTIONS, friendOptionBundle);
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
            friendOptionsBundle =  getArguments().getBundle(SendChallenge.BDL_FRIEND_OPTIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_configure_challenge, container, false);
        Button nextButton = (Button) rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO check if next fragment is needed
                Bundle gameOptions = new Bundle();
                gameOptions.putInt(GamesRequests.GAME_LABEL.GUESSES, configuredGuesses);
                gameOptions.putInt(GamesRequests.GAME_LABEL.TIME, configuredTime);
                gameOptions.putString(GamesRequests.GAME_LABEL.HINT, configuredHint);
                gameOptions.putBoolean(GamesRequests.GAME_LABEL.LEADERBOARD, configuredLeaderboard);

                //user already selected friends
                if (friendOptionsBundle != null) {
                    createGame(gameOptions);
                } else {
                    startNextFragment(gameOptions);
                }
            }
        });

        final int checked_color_selected = getResources().getColor(R.color.primary_text);
        final int checked_color_unselected = getResources().getColor(R.color.grey_400);

        AttributeSet attributeSet =  getButtonAttributes();
        setupTimeButtons(rootView, checked_color_selected, checked_color_unselected, attributeSet);
        setupGuessButtons(rootView, checked_color_selected, checked_color_unselected, attributeSet);

        mListener.setToolbarTitle("Game options..");
        return  rootView;
    }

    private void createGame(Bundle gameOptions) {
        int[] friendId ={ friendOptionsBundle.getInt(SendChallenge.ARG_FRIEND_ID)};
        Bundle finalBundle = new Bundle();
        finalBundle.putIntArray(GamesRequests.GAME_LABEL.FRIENDS, friendId);
        finalBundle.putAll(gameOptions);
        finalBundle.putAll(pictureOptionsBundle);
        boolean result = mListener.startGame(finalBundle);
        if (result) {
            Log.d(TAG, "Game sent successfully");
        } else {
            Log.d(TAG, "Error sending game");
        }
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
        XmlPullParser parser = getResources().getLayout(R.layout.custom_radio_btn);
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
        } while(state != XmlPullParser.END_DOCUMENT);

        return attributes;
    }

    /**
     * Sets up the options for the game time
     * @param rootView The root view
     * @param checked_color_selected Text color when number is selected
     * @param checked_color_unselected Default text color
     * @param attributes RadioButton attributes used to inflate each number's radio button
     */
    private void setupTimeButtons(View rootView, final int checked_color_selected,
                                  final int checked_color_unselected, AttributeSet attributes) {
        ViewGroup horScrolLayout =  (ViewGroup) rootView.findViewById(R.id.time_radio_group);

        for (int i = 5; i <= 30; i+=5) {
            RadioButton currButton = new RadioButton(getActivity().getApplicationContext(),
                    attributes, R.style.radio_button);
            currButton.setId(i);
            currButton.setText(Integer.toString(i));
            if (i == defaultTime) currButton.setTextColor(checked_color_selected);
            currButton.setChecked(i == defaultTime); // default time
            horScrolLayout.addView(currButton);

            currButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ((RadioGroup) view.getParent()).check(view.getId());
                    configuredTime = view.getId();
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
     * @param rootView The root view
     * @param checked_color_selected Text color when number is selected
     * @param checked_color_unselected Default text color
     * @param attributes RadioButton attributes used to inflate each number's radio button
     */
    private void setupGuessButtons(View rootView, final int checked_color_selected,
                                  final int checked_color_unselected, AttributeSet attributes) {
        ViewGroup horScrolLayout =  (ViewGroup) rootView.findViewById(R.id.guesses_radio_group);

        for (int i = 1; i <= 5; i++) {
            RadioButton currButton = new RadioButton(getActivity().getApplicationContext(),
                    attributes, R.style.radio_button);
            currButton.setId(i);
            currButton.setText(Integer.toString(i));
            if (i == defaultGuesses) currButton.setTextColor(checked_color_selected);
            currButton.setChecked(i == defaultGuesses);
            horScrolLayout.addView(currButton);

            currButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ((RadioGroup) view.getParent()).check(view.getId());
                    configuredGuesses = view.getId();
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
        boolean startGame(Bundle gameBundle);

    }
}

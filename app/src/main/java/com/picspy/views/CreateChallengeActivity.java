package com.picspy.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.picspy.firstapp.R;

/**
 * Created by Justin12 on 01/01/2016.
 */

/**
 * The Activity where the user enters information about the challenge they want to create. This info
 * includes the hint, the number of guesses, the time for guessing, and the option to send it to
 * leader-boards.
 */
public class CreateChallengeActivity extends Activity {

    // TODO: add time part of the challenge
    // TODO: fix lagginess of this activity when using the style defined in the manifest
    private Button cancelButton;
    private Button okButton;
    private TextView hint;
    private EditText hintInput;
    private TextView guesses;
    private TextView numOfGuesses;
    private Button upButton;
    private Button downButton;
    private TextView time;
    private TextView leaderboards;
    private CheckBox checkBox;
    public final static int DEFAULT_GUESSES = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);

        // use this if the window should look like a pop-up
//        DisplayMetrics display = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(display);
//        int width = display.widthPixels;
//        int height = display.heightPixels;
//        getWindow().setLayout((int)(width*.9), (int)(height*.9));

        hint = (TextView) findViewById(R.id.hint_title);
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/ld_childish.ttf");
        hint.setTypeface(customFont);

        guesses = (TextView) findViewById(R.id.guesses_title);
        guesses.setTypeface(customFont);

        numOfGuesses = (TextView) findViewById(R.id.number_of_guesses);
        numOfGuesses.setText(DEFAULT_GUESSES + "");
        numOfGuesses.setTypeface(customFont);

        upButton = (Button) findViewById(R.id.up_button);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current_guesses = Integer.valueOf(numOfGuesses.getText().toString());
                if (current_guesses < 5) {
                    numOfGuesses.setText((current_guesses + 1) + "");
                }
            }
        });

        downButton = (Button) findViewById(R.id.down_button);
        // rotate the button 180 degrees so it is upside down
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotation);
        downButton.startAnimation(rotation);
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current_guesses = Integer.valueOf(numOfGuesses.getText().toString());
                if (current_guesses > 1) {
                    numOfGuesses.setText((current_guesses - 1) + "");
                }
            }
        });

        time = (TextView) findViewById(R.id.time);
        time.setTypeface(customFont);

        leaderboards = (TextView) findViewById(R.id.leaderboard);
        leaderboards.setTypeface(customFont);

        checkBox = (CheckBox) findViewById(R.id.checkbox);

        hintInput = (EditText) findViewById(R.id.hint_input);
        hintInput.setTypeface(customFont);
        hintInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        hintInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Do not treat the enter key as a carriage return but rather complete the form
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    ((RelativeLayout) v.getParent()).requestFocus();
                    return true;
                }
                // handle all other events
                return onKeyDown(keyCode, event);
            }
        });

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setTypeface(customFont);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                setResult(RESULT_CANCELED, result);
                finish();
            }
        });
        // TODO: make ok and cancel button text bigger
        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setTypeface(customFont);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra(CameraActivity.HINT, hintInput.getText()); // store the hint
                result.putExtra(CameraActivity.GUESSES, Integer.valueOf(numOfGuesses.getText().toString()));
                result.putExtra(CameraActivity.LEADERBOARDS, checkBox.isEnabled());
                setResult(RESULT_OK, result);
                finish();
            }
        });

    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

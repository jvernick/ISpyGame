package com.picspy.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.adapters.DatabaseHandler;
import com.picspy.firstapp.R;
import com.picspy.models.FriendRecord;
import com.picspy.models.UserRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.FriendsRequests;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.UserRequests;
import com.picspy.utils.VolleyRequest;
import com.picspy.views.FindFriendsActivity;

/**
 * Activity to search for friends
 * TODO in upgrades, change camera on bottom bar to buttons for scanning  or searching QR codes
 */
public class FriendSearchFragment extends Fragment {
    private static final String USERNAME = "username";
    private static final String USER_ID = "userID";
    private static final String TAG = "FriendSearch";
    private static final String EXTRA_SHOW_KEYBOARD = "com.picspy.friend.showKeyboard";
    private Button btnFindFriend;
    private EditText unameField;
    private TextView responseText;
    private ProgressBar progressSpinner;
    private boolean showKeyboard;

    /**
     * Static factory method that initializes the fragment's arguments,
     * and returns the new fragment to the client.
     */
    public static FriendSearchFragment newInstance(int startFragment) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_SHOW_KEYBOARD, startFragment);
        FriendSearchFragment f = new FriendSearchFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int startFragment = this.getArguments().getInt(EXTRA_SHOW_KEYBOARD, 0);
        Log.d(TAG, "" + startFragment);
        showKeyboard = (startFragment == 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showKeyboard) {
            //showKeyboard();
            InputMethodManager imgr = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_friend_search, container, false);
        btnFindFriend = (Button) rootView.findViewById(R.id.btn_add_friend);
        unameField = (EditText) rootView.findViewById(R.id.username_field);
        unameField.requestFocus();
        responseText = (TextView) rootView.findViewById(R.id.message);

        progressSpinner = (ProgressBar) rootView.findViewById(R.id.challenges_progressBar);
        progressSpinner.setVisibility(View.GONE);
        return rootView;
    }

    public void showKeyboard() {
        InputMethodManager mgr = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(unameField, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideKeyboard() {
        InputMethodManager mgr = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(unameField.getWindowToken(), 0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnFindFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (unameField.getText().length() != 0) {
                    //responseText.requestFocus();
                    responseText.setText("");
                    hideKeyboard();
                    String query = unameField.getText().toString();
                    if (isValidRequest(query)) {
                        addUser(unameField.getText().toString());
                        progressSpinner.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private boolean isValidRequest(String username) {
        if (username.equals(PrefUtil.getString(getActivity(), AppConstants.USER_NAME))) {
            responseText.setText(getString(R.string.message_cannot_add_self));
            unameField.setText("");
            return false;
        } else if (DatabaseHandler.getInstance(getActivity()).getFriend(username) != null) {
            responseText.setText(getString(R.string.message_already_friends));
            unameField.setText("");
            return false;
        } else {
            return true;
        }
    }

    private void addUser(String username) {
        Response.Listener<UserRecord> responeListener = new Response.Listener<UserRecord>() {
            @Override
            public void onResponse(UserRecord response) {
                progressSpinner.setVisibility(View.GONE);
                if (response != null && response.getId() != 0) {
                    sendFriendRequest(response.getId());
                } else {
                    responseText.setText(getString(R.string.message_user_not_found));
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressSpinner.setVisibility(View.GONE);
                if (error != null) {
                    String err = (error.getMessage() == null) ? "error message null" : error.getMessage();
                    error.printStackTrace();
                    Log.d(TAG, err);
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View layout = inflater.inflate(R.layout.view_network_error_toast,
                                (ViewGroup) getActivity().findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getActivity());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                }
            }
        };

        UserRequests addUserRequest = UserRequests.findUser(getActivity(), username, responeListener, errorListener);
        addUserRequest.setTag(FindFriendsActivity.CANCEL_TAG);
        VolleyRequest.getInstance(getActivity().getApplicationContext()).addToRequestQueue(addUserRequest);
    }

    private void sendFriendRequest(int userId) {
        Response.Listener<FriendRecord> responseListener = new Response.Listener<FriendRecord>() {
            @Override
            public void onResponse(FriendRecord response) {
                progressSpinner.setVisibility(View.GONE);
                if (response != null && response.getFriend_1() != 0) {
                    responseText.setText(getString(R.string.message_request_sent));
                    unameField.setText("");
                    Log.d(TAG, response.toString());
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressSpinner.setVisibility(View.GONE);
                if (error != null) {
                    String err = (error.getMessage() == null) ? "error message null" : error.getMessage();
                    error.printStackTrace();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View layout = inflater.inflate(R.layout.view_network_error_toast,
                                (ViewGroup) getActivity().findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getActivity());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    } else {
                        //Set if user already sent or was already sent a request
                        responseText.setText(getString(R.string.message_request_sent));
                    }
                }
            }
        };

        FriendsRequests sendRequest = FriendsRequests.sendFriendRequest(getActivity(), userId, responseListener, errorListener);
        if (sendRequest != null) sendRequest.setTag(FindFriendsActivity.CANCEL_TAG);
        VolleyRequest.getInstance(getActivity().getApplicationContext()).addToRequestQueue(sendRequest);
    }
}

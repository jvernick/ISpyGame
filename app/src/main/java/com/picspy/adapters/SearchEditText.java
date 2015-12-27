package com.picspy.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.picspy.firstapp.R;

/**
 * Created by BrunelAmC on 12/26/2015.
 */
public class SearchEditText extends RelativeLayout {
    private LayoutInflater inflater = null;
    protected EditText searchText;
    private Button btn_clear;

    public SearchEditText(Context context) {
        super(context);
        initViews();
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.search_edit_text, this, true);
        searchText = (EditText) findViewById(R.id.clearable_edit);
        btn_clear = (Button) findViewById(R.id.clearable_button_clear);
        btn_clear.setVisibility(RelativeLayout.INVISIBLE);
        initButtonListiner();
        showHideClearButton();
    }

    private void initButtonListiner() {
        btn_clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.setText("");
            }
        });
    }

    private void showHideClearButton() {
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    btn_clear.setVisibility(RelativeLayout.VISIBLE);
                else
                    btn_clear.setVisibility(RelativeLayout.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
    }

    public Editable getText()
    {
        return  searchText.getText();
    }
}
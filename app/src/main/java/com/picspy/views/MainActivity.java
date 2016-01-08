package com.picspy.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.picspy.firstapp.R;
import com.picspy.views.fragments.FriendsFragment;
import com.picspy.adapters.TabsViewPagerAdapter;
import com.picspy.views.fragments.TopFragment;
import com.picspy.adapters.SlidingTabLayout;

import java.util.ArrayList;

//TODO: this is currently modified to show the result of user registration
/**
 * Main page activity
 */
public class MainActivity extends FragmentActivity {
    private ViewPager viewPager;

    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define SlidingTabLayout (shown at top)
        // and ViewPager (shown at bottom) in the layout.
        // Get their instances.
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tab);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        // create a fragment list in order.
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(FriendsFragment.newInstance(0));
        fragments.add(new TopFragment());

        // use FragmentPagerAdapter to bind the slidingTabLayout (tabs with different titles)
        // and ViewPager (different pages of fragment) together.
        TabsViewPagerAdapter myViewPagerAdapter = new TabsViewPagerAdapter(
                getSupportFragmentManager(), fragments);
        viewPager.setAdapter(myViewPagerAdapter);
        Log.d("mainactivity", "onCreate");

        // make sure the tabs are equally spaced.
        slidingTabLayout.setDistributeEvenly(true);
        // Setting custom color for scroll bar indicator
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        slidingTabLayout.setViewPager(viewPager);
    }


    /* No menu for now
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    /**
     * Starts camera activity
     * @param view View from button click
     */
    public void launchCamera(View view) {
        // TODO: Make the AlertDialog UI nicer
        final CharSequence choices[] = new CharSequence[] {"Take a picture", "Use a saved picture"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                String answer = choices[which].toString();
                if (answer.equals("Take a picture")) {
                    Intent startCamera = new Intent(getApplicationContext(), CameraActivity.class);
                    startActivity(startCamera);
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), SELECT_PICTURE);
                }
            }
        });
        builder.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = null;
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
            }

            Intent intent = new Intent(getApplicationContext(), CreateChallengeActivity.class);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
            startActivity(intent);
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    /**
     * Starts challenges/updates activity
     * @param view View from button click
     */
    public void launchChallenges(View view) {
        Intent intent = new Intent(this, ChallengesActivity.class);
        startActivity(intent);
    }
}

package com.gaiagps.iburn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.widget.*;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.gaiagps.iburn.BurnClient.isFirstLaunch;
import static com.gaiagps.iburn.BurnClient.validateUnlockPassword;

public class MainActivity extends FragmentActivity {

    // Hold display width to allow MapViewPager to calculate
    // swiping margin on screen's right border.
    public static int display_width = -1;
    LayoutInflater inflater;
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    boolean googlePlayServicesMissing = false;

    private ViewPager mViewPager;
    private FragmentWithTitlePagerAdapter mPagerAdapter;

    /** Fragments to appear in main ViewPager */
    private static List<Pair<Class<? extends Fragment>, String>> sPages
            = new ArrayList<Pair<Class<? extends Fragment>, String>>() {{
        add(new Pair<Class<? extends Fragment>, String>(GoogleMapFragment.class,        "Map"));
        add(new Pair<Class<? extends Fragment>, String>(ArtListViewFragment.class,      "Art"));
        add(new Pair<Class<? extends Fragment>, String>(CampListViewFragment.class,     "Camps"));
        add(new Pair<Class<? extends Fragment>, String>(EventListViewFragment.class,    "Events"));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("");
        getDisplayWidth();
        setContentView(R.layout.activity_main);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (checkPlayServices()) {
            setupFragmentStatePagerAdapter();
        } else
            googlePlayServicesMissing = true;
        checkIntentForExtras(getIntent());
        if (isFirstLaunch(this)) {
            showWelcomeDialog();
            //DataUtils.checkAndSetupDB(getApplicationContext());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googlePlayServicesMissing && checkPlayServices()) {
            setupFragmentStatePagerAdapter();
            googlePlayServicesMissing = false;
        }
    }

    private void showWelcomeDialog() {
        View dialog = getLayoutInflater().inflate(R.layout.dialog_welcome, null);
        new AlertDialog.Builder(this)
                .setView(dialog)
                .setPositiveButton(R.string.lets_burn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkIntentForExtras(intent);
    }

    private void checkIntentForExtras(Intent intent) {
        if (intent.hasExtra("tab")) {
            Constants.TAB_TYPE tab = (Constants.TAB_TYPE) intent.getSerializableExtra("tab");
            switch (tab) {
                case MAP:
                    mViewPager.setCurrentItem(0, true);
                    LatLng marker = new LatLng(intent.getFloatExtra("lat", 0), intent.getFloatExtra("lon", 0));

                    ((GoogleMapFragment) mPagerAdapter.getItem(0)).mapAndCenterOnMarker(new MarkerOptions().position(marker).title(intent.getStringExtra("title")));
                    Log.i("GoogleMapFragment", "queue marker");
                    break;

            }
        }
    }

    private void setupFragmentStatePagerAdapter() {
        mViewPager = (MapViewPager) findViewById(R.id.pager);
        mPagerAdapter = new FragmentWithTitlePagerAdapter(getSupportFragmentManager(), sPages);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setShouldExpand(true);
        tabs.setTabPaddingLeftRight(0);
        tabs.setIndicatorColor(getResources().getColor(R.color.highlight));
        tabs.setTextColorResource(R.color.white);
        mViewPager.setAdapter(mPagerAdapter);
        tabs.setViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!BurnClient.isEmbargoClear(getApplicationContext()))
            getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (BurnClient.isEmbargoClear(getApplicationContext()))
            menu.removeItem(R.id.action_unlock);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_unlock) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Enter Unlock Password");

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            alert.setView(input);
            alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String pwGuess = input.getText().toString();
                    if (validateUnlockPassword(pwGuess)) {
                        BurnClient.setEmbargoClear(MainActivity.this, true);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.victory))
                                .setMessage(getString(R.string.location_data_unlocked))
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    } else {
                        dialog.cancel();
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.invalid_password))
                                .show();
                    }
                }
            });

            alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
            return true;
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Adapter that takes a List of Pairs representing a Fragment and Title
     * for pairing with a tabbed ViewPager.
     *
     * Each Fragment must have a no-arg newInstance() method.
     */
    public static class FragmentWithTitlePagerAdapter extends FragmentStatePagerAdapter {

        private static List<Pair<Class<? extends Fragment>, String>> PAGES;

        public FragmentWithTitlePagerAdapter(FragmentManager fm, List<Pair<Class<? extends Fragment>, String>> pages) {
            super(fm);
            PAGES = pages;
        }

        @Override
        public int getCount() {
            return PAGES.size();
        }

        @Override
        public Fragment getItem(int position) {
            try {
                return (Fragment) PAGES.get(position).first.getMethod("newInstance", null).invoke(null, null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                throw new IllegalStateException("Unexpected ViewPager item requested: " + position);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return PAGES.get(position).second;
        }
    }

    /**
     * Measure display width so the view pager can implement its
     * custom behavior re: paging on the map view
     */
    private void getDisplayWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        display_width = display.getWidth();
    }

    private boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                showErrorDialog(status);
            } else {
                Toast.makeText(this, getString(R.string.device_not_supported),
                        Toast.LENGTH_LONG).show();
                finish();
            }

            return false;
        }
        return true;
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, getString(R.string.requres_google_play),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

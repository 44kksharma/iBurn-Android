package com.gaiagps.iburn.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gaiagps.iburn.Constants;
import com.gaiagps.iburn.DateUtil;
import com.gaiagps.iburn.Geo;
import com.gaiagps.iburn.R;
import com.gaiagps.iburn.database.ArtTable;
import com.gaiagps.iburn.database.CampTable;
import com.gaiagps.iburn.database.DataProvider;
import com.gaiagps.iburn.database.EventTable;
import com.gaiagps.iburn.database.PlayaDatabase;
import com.gaiagps.iburn.database.PlayaItemTable;
import com.gaiagps.iburn.fragment.GoogleMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.sqlbrite.SqlBrite;

import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Show the detail view for a Camp, Art installation, or Event
 * Created by davidbrodsky on 8/11/13.
 */
public class PlayaItemViewActivity extends AppCompatActivity {

    String modelTable;
    int modelId;
    LatLng latLng;

    boolean isFavorite;
    boolean showingLocation;

    MenuItem favoriteMenuItem;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.map_container)
    FrameLayout mapContainer;

    @InjectView(R.id.text_container)
    ViewGroup textContainer;

    @InjectView(R.id.title)
    TextView titleTextView;

    @InjectView(R.id.favorite_button)
    FloatingActionButton favoriteButton;

    @InjectView(R.id.subitem_1)
    TextView subItem1TextView;

    @InjectView(R.id.subitem_2)
    TextView subItem2TextView;

    @InjectView(R.id.subitem_3)
    TextView subItem3TextView;

    @InjectView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playa_item_view);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        populateViews(getIntent());
        setTextContainerMinHeight();

        AlphaAnimation fadeAnimation = new AlphaAnimation(0, 1);
        fadeAnimation.setDuration(1000);
        fadeAnimation.setStartOffset(250);
        fadeAnimation.setFillAfter(true);
        fadeAnimation.setFillEnabled(true);
//        mapContainer.setAlpha(0);
        mapContainer.startAnimation(fadeAnimation);
    }

    /**
     * Set the text container within NestedScrollView to have height exactly equal to the
     * full height minus status bar and toolbar. This addresses an issue where the
     * collapsing toolbar pattern gets all screwed up.
     */
    private void setTextContainerMinHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(textSizeAttr);
        int abHeight = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();

        Resources r = getResources();
        int statusBarPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

        textContainer.setMinimumHeight(height - abHeight - statusBarPx);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_playa_item, menu);

        favoriteMenuItem = menu.findItem(R.id.favorite_menu);
        if (isFavorite) favoriteMenuItem.setIcon(R.drawable.ic_heart_pressed);
        if (showingLocation) favoriteMenuItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.favorite_menu:
                int newDrawableResId = isFavorite ? R.drawable.ic_heart : R.drawable.ic_heart_pressed;
                favoriteMenuItem.setIcon(newDrawableResId);
                favoriteButton.setImageResource(newDrawableResId);
                setFavorite(!isFavorite);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void populateViews(Intent i) {
        modelId = i.getIntExtra("model_id", 0);
        Constants.PlayaItemType model_type = (Constants.PlayaItemType) i.getSerializableExtra("model_type");
        switch (model_type) {
            case CAMP:
                modelTable = PlayaDatabase.CAMPS;
                break;
            case ART:
                modelTable = PlayaDatabase.ART;
                break;
            case EVENT:
                modelTable = PlayaDatabase.EVENTS;
                break;
        }

        DataProvider.getInstance(this)
                .flatMap(dataProvider -> dataProvider.createQuery(modelTable, "SELECT * FROM " + modelTable + " WHERE _id = ?", String.valueOf(modelId)))
                .map(SqlBrite.Query::run)
                .subscribe(c -> {
                    try {
                        if (c != null && c.moveToFirst()) {
                            final String title = c.getString(c.getColumnIndexOrThrow(PlayaItemTable.name));
                            titleTextView.setText(title);
                            isFavorite = c.getInt(c.getColumnIndex(PlayaItemTable.favorite)) == 1;
                            if (isFavorite)
                                favoriteButton.setImageResource(R.drawable.ic_heart_pressed);
                            else
                                favoriteButton.setImageResource(R.drawable.ic_heart);

                            favoriteButton.setTag(R.id.list_item_related_model, modelId);
                            favoriteButton.setTag(R.id.list_item_related_model_type, model_type);
                            favoriteButton.setOnClickListener(favoriteButtonOnClickListener);

                            if (!c.isNull(c.getColumnIndex(PlayaItemTable.description))) {
                                ((TextView) findViewById(R.id.body)).setText(c.getString(c.getColumnIndexOrThrow(PlayaItemTable.description)));
                            } else
                                findViewById(R.id.body).setVisibility(View.GONE);

                            showingLocation = !c.isNull(c.getColumnIndex(PlayaItemTable.latitude));
                            if (showingLocation) {
                                favoriteButton.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                                    if (favoriteMenuItem != null)
                                        favoriteMenuItem.setVisible(v.getVisibility() == View.GONE);
                                });
                                latLng = new LatLng(c.getDouble(c.getColumnIndexOrThrow(PlayaItemTable.latitude)), c.getDouble(c.getColumnIndexOrThrow(PlayaItemTable.longitude)));
                                //TextView locationView = ((TextView) findViewById(R.id.location));
                                LatLng start = new LatLng(Geo.MAN_LAT, Geo.MAN_LON);
                                Log.i("GoogleMapFragment", "adding / centering marker");
                                GoogleMapFragment mapFragment = (GoogleMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                                mapFragment.showcaseMarker(new MarkerOptions().position(latLng));
                                mapFragment.getMapAsync(googleMap -> {
                                    UiSettings settings = googleMap.getUiSettings();
                                    settings.setMyLocationButtonEnabled(false);
                                    settings.setZoomControlsEnabled(false);
                                    googleMap.setOnCameraChangeListener(cameraPosition -> {
                                        if (cameraPosition.zoom >= 20) {
                                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition.target, (float) 19.99));
                                        }
                                    });
                                });
                                //favoriteMenuItem.setVisible(false);
                                //locationView.setText(String.format("%f, %f", latLng.latitude, latLng.longitude));
                            } else {
                                // Adjust the margin / padding show the heart icon doesn't
                                // overlap title + descrition
//                                findViewById(R.id.map_container).setVisibility(View.GONE);
                                //getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
                                collapsingToolbarLayout.setBackgroundResource(android.R.color.transparent);
                                CollapsingToolbarLayout.LayoutParams parms = new CollapsingToolbarLayout.LayoutParams(CollapsingToolbarLayout.LayoutParams.MATCH_PARENT, 24);
                                mapContainer.setLayoutParams(parms);
                                favoriteButton.setVisibility(View.GONE);
                                //favoriteMenuItem.setVisible(true);
                            }

                            switch (model_type) {
                                case ART:
                                    if (!c.isNull(c.getColumnIndex(ArtTable.playaAddress))) {
                                        subItem1TextView.setText(c.getString(c.getColumnIndexOrThrow(ArtTable.playaAddress)));
                                    } else
                                        subItem1TextView.setVisibility(View.GONE);

                                    if (!c.isNull(c.getColumnIndex(ArtTable.artist))) {
                                        subItem2TextView.setText(c.getString(c.getColumnIndexOrThrow(ArtTable.artist)));
                                    } else
                                        subItem2TextView.setVisibility(View.GONE);

                                    if (!c.isNull(c.getColumnIndex(ArtTable.artistLoc))) {
                                        subItem3TextView.setText(c.getString(c.getColumnIndexOrThrow(ArtTable.artistLoc)));
                                    } else
                                        subItem3TextView.setVisibility(View.GONE);
                                    break;
                                case CAMP:
                                    if (!c.isNull(c.getColumnIndex(CampTable.playaAddress))) {
                                        subItem1TextView.setText(c.getString(c.getColumnIndexOrThrow(CampTable.playaAddress)));
                                    } else
                                        subItem1TextView.setVisibility(View.GONE);

                                    if (!c.isNull(c.getColumnIndex(CampTable.hometown))) {
                                        subItem2TextView.setText(c.getString(c.getColumnIndexOrThrow(CampTable.hometown)));
                                    } else
                                        subItem2TextView.setVisibility(View.GONE);
                                    subItem3TextView.setVisibility(View.GONE);
                                    break;
                                case EVENT:
                                    if (!c.isNull(c.getColumnIndex(EventTable.playaAddress))) {
                                        subItem1TextView.setText(c.getString(c.getColumnIndexOrThrow(EventTable.playaAddress)));
                                    } else
                                        subItem1TextView.setVisibility(View.GONE);

                                    Date nowDate = new Date();
                                    Calendar nowPlusOneHrDate = Calendar.getInstance();
                                    nowPlusOneHrDate.setTime(nowDate);
                                    nowPlusOneHrDate.add(Calendar.HOUR, 1);

                                    subItem2TextView.setText(DateUtil.getDateString(this, nowDate, nowPlusOneHrDate.getTime(),
                                            c.getString(c.getColumnIndexOrThrow(EventTable.startTime)),
                                            c.getString(c.getColumnIndexOrThrow(EventTable.startTimePrint)),
                                            c.getString(c.getColumnIndexOrThrow(EventTable.endTime)),
                                            c.getString(c.getColumnIndexOrThrow(EventTable.endTimePrint))));
                                    subItem3TextView.setVisibility(View.GONE);
                                    break;
                            }
                        }
                    } finally {
                        if (c != null) c.close();
                    }
                });
    }

    View.OnClickListener favoriteButtonOnClickListener = (View v) -> {
        int newDrawableResId = isFavorite ? R.drawable.ic_heart : R.drawable.ic_heart_pressed;
        ((ImageView) v).setImageResource(newDrawableResId);
        favoriteMenuItem.setIcon(newDrawableResId);

        setFavorite(!isFavorite);
    };

    private void setFavorite(boolean isFavorite) {
        DataProvider.getInstance(PlayaItemViewActivity.this)
                .subscribe(dataProvider -> dataProvider.updateFavorite(modelTable, modelId, isFavorite));
        this.isFavorite = isFavorite;
    }
}
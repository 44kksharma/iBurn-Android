package com.gaiagps.iburn.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gaiagps.iburn.PlayaClient;
import com.gaiagps.iburn.Constants;
import com.gaiagps.iburn.R;
import com.gaiagps.iburn.database.ArtTable;
import com.gaiagps.iburn.database.CampTable;
import com.gaiagps.iburn.database.EventTable;
import com.gaiagps.iburn.database.PlayaContentProvider;
import com.gaiagps.iburn.database.PlayaItemTable;
import com.gaiagps.iburn.fragment.GoogleMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by davidbrodsky on 8/11/13.
 */
public class PlayaItemViewActivity extends FragmentActivity {

    Uri uri;
    int model_id;
    LatLng latLng;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("");
        setContentView(R.layout.activity_playa_item_view);
        populateViews(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu){
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        return false;
    }

    private void populateViews(Intent i){
        model_id = i.getIntExtra("model_id",0);
        Constants.PLAYA_ITEM model_type = (Constants.PLAYA_ITEM) i.getSerializableExtra("playa_item");
        String selection = "_id = ?";
        String[] projection = null;
        switch(model_type){
            case CAMP:
                projection = null;
                uri = PlayaContentProvider.Camps.CAMPS;
                break;
            case ART:
                projection = null;
                uri = PlayaContentProvider.Art.ART;
                break;
            case EVENT:
                projection = null;
                uri = PlayaContentProvider.Events.EVENTS;
                break;
        }

        Cursor c = getContentResolver().query(uri, projection, selection, new String[]{String.valueOf(model_id)}, null);
        if(c != null && c.moveToFirst()){
            final String title = c.getString(c.getColumnIndexOrThrow(PlayaItemTable.name));
            ((TextView) findViewById(R.id.title)).setText(title);
            int isFavorite = c.getInt(c.getColumnIndex(PlayaItemTable.favorite));
            View favoriteBtn = findViewById(R.id.favorite_button);
            if(isFavorite == 1)
                ((ImageView)favoriteBtn).setImageResource(android.R.drawable.star_big_on);
            else
                ((ImageView)favoriteBtn).setImageResource(android.R.drawable.star_big_off);
            favoriteBtn.setTag(R.id.list_item_related_model, model_id);
            favoriteBtn.setTag(R.id.list_item_related_model_type, model_type);
            favoriteBtn.setTag(R.id.favorite_button_state, isFavorite);
            favoriteBtn.setOnClickListener(favoriteButtonOnClickListener);

            if(!c.isNull(c.getColumnIndex(PlayaItemTable.description))){
                ((TextView) findViewById(R.id.body)).setText(c.getString(c.getColumnIndexOrThrow(PlayaItemTable.description)));
            }else
                findViewById(R.id.body).setVisibility(View.GONE);

            if(PlayaClient.isEmbargoClear(getApplicationContext()) && !c.isNull(c.getColumnIndex(PlayaItemTable.latitude))){
                latLng = new LatLng(c.getDouble(c.getColumnIndexOrThrow(PlayaItemTable.latitude)), c.getDouble(c.getColumnIndexOrThrow(PlayaItemTable.longitude)));
                //TextView locationView = ((TextView) findViewById(R.id.location));
                final GoogleMapFragment mapFragment = (GoogleMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                LatLng start = new LatLng(Constants.MAN_LAT, Constants.MAN_LON);
                Log.i("GoogleMapFragment", "adding / centering marker");
                mapFragment.mapAndCenterOnMarker(new MarkerOptions().position(latLng));
                mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
                mapFragment.getMap().getUiSettings().setZoomControlsEnabled(false);
                mapFragment.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        if(cameraPosition.zoom >= 20){
                            mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition.target, (float) 19.99));
                        }
                    }
                });
                //locationView.setText(String.format("%f, %f", latLng.latitude, latLng.longitude));
                /*
                mapFragment.setOnTouchListener(new View.OnTouchListener(){

                    @Override
                    public boolean onTouch(View v, MotionEvent me) {
                        if(me.getAction() == MotionEvent.ACTION_DOWN){
                            Intent i = new Intent(PlayaItemViewActivity.this, MainActivity.class);
                            i.putExtra("tab", Constants.TAB_TYPE.MAP);
                            i.putExtra("lat", latLng.latitude);
                            i.putExtra("lon", latLng.longitude);
                            i.putExtra("title", title);
                            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            PlayaItemViewActivity.this.startActivity(i);
                            return true;
                        }
                        return false;
                    }

                });
                */
            }else
                findViewById(R.id.map).setVisibility(View.GONE);

            switch(model_type){
                case ART:
                    if(!c.isNull(c.getColumnIndex(ArtTable.artist))){
                        ((TextView) findViewById(R.id.subitem_1)).setText(c.getString(c.getColumnIndexOrThrow(ArtTable.artist)));
                    }else
                        findViewById(R.id.subitem_1).setVisibility(View.GONE);

                    if(!c.isNull(c.getColumnIndex(ArtTable.contact))){
                        ((TextView) findViewById(R.id.subitem_2)).setText(c.getString(c.getColumnIndexOrThrow(ArtTable.contact)));
                    }else
                        findViewById(R.id.subitem_2).setVisibility(View.GONE);

                    if(!c.isNull(c.getColumnIndex(ArtTable.artistLoc))){
                        ((TextView) findViewById(R.id.subitem_3)).setText(c.getString(c.getColumnIndexOrThrow(ArtTable.artistLoc)));
                    }else
                        findViewById(R.id.subitem_3).setVisibility(View.GONE);
                    break;
                case CAMP:
                    if(!c.isNull(c.getColumnIndex(CampTable.contact))){
                        ((TextView) findViewById(R.id.subitem_1)).setText(c.getString(c.getColumnIndexOrThrow(CampTable.contact)));
                    }else
                        findViewById(R.id.subitem_1).setVisibility(View.GONE);

                    if(!c.isNull(c.getColumnIndex(CampTable.hometown))){
                        ((TextView) findViewById(R.id.subitem_2)).setText(c.getString(c.getColumnIndexOrThrow(CampTable.hometown)));
                    }else
                        findViewById(R.id.subitem_2).setVisibility(View.GONE);
                    findViewById(R.id.subitem_3).setVisibility(View.GONE);
                    break;
                case EVENT:
                    if(!c.isNull(c.getColumnIndex(EventTable.campName))){
                        ((TextView) findViewById(R.id.subitem_1)).setText(c.getString(c.getColumnIndexOrThrow(EventTable.campName)));
                    }else
                        findViewById(R.id.subitem_1).setVisibility(View.GONE);

//                    if(BurnClient.isEmbargoClear(getApplicationContext()) && !c.isNull(c.getColumnIndex(EventTable.COLUMN_LOCATION))){
//                        ((TextView) findViewById(R.id.subitem_2)).setText(c.getString(c.getColumnIndexOrThrow(EventTable.COLUMN_LOCATION)));
//                    }
//                    else
//                        findViewById(R.id.subitem_2).setVisibility(View.GONE);
                    findViewById(R.id.subitem_3).setVisibility(View.GONE);
                    break;
            }
        }
    }

    View.OnClickListener favoriteButtonOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            ContentValues values = new ContentValues();
            if((Integer)v.getTag(R.id.favorite_button_state) == 0){
                values.put("favorite", 1);
                v.setTag(R.id.favorite_button_state, 1);
                ((ImageView)v).setImageResource(android.R.drawable.star_big_on);
            }
            else if((Integer)v.getTag(R.id.favorite_button_state) == 1){
                values.put("favorite", 0);
                v.setTag(R.id.favorite_button_state, 0);
                ((ImageView)v).setImageResource(android.R.drawable.star_big_off);
            }
            int result = getContentResolver().update(uri.buildUpon().appendPath(String.valueOf(model_id)).build(),
                    values, null, null);
        }
    };
}
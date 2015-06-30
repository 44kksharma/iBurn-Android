package com.gaiagps.iburn.fragment;

import android.os.Bundle;

import com.gaiagps.iburn.Constants;
import com.gaiagps.iburn.adapters.CursorRecyclerViewAdapter;
import com.gaiagps.iburn.adapters.PlayaItemCursorAdapter;
import com.gaiagps.iburn.database.DataProvider;
import com.gaiagps.iburn.database.PlayaDatabase;
import com.squareup.sqlbrite.SqlBrite;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Fragment displaying all Playa Camps
 * <p/>
 * Created by davidbrodsky on 8/3/13.
 */
public class CampListViewFragment extends PlayaListViewFragment {

    public static CampListViewFragment newInstance() {
        return new CampListViewFragment();
    }

    protected CursorRecyclerViewAdapter getAdapter() {
        return new PlayaItemCursorAdapter(getActivity(), null, Constants.PlayaItemType.CAMP, this);
    }

    @Override
    protected Subscription subscribeToData() {
        return DataProvider.getInstance(getActivity())
                .observeTable(PlayaDatabase.CAMPS, getAdapter().getRequiredProjection())
                .map(SqlBrite.Query::run)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(cursor -> {
                    Timber.d("Got data update");
                    onDataChanged(cursor);
                }, throwable -> Timber.e(throwable, "Data subscription error"));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
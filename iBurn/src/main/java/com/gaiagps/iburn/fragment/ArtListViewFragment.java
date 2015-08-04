package com.gaiagps.iburn.fragment;

import android.os.Bundle;

import com.gaiagps.iburn.Constants;
import com.gaiagps.iburn.adapters.ArtCursorAdapter;
import com.gaiagps.iburn.adapters.CursorRecyclerViewAdapter;
import com.gaiagps.iburn.adapters.PlayaItemCursorAdapter;
import com.gaiagps.iburn.database.DataProvider;
import com.gaiagps.iburn.database.PlayaDatabase;
import com.squareup.sqlbrite.SqlBrite;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Fragment displaying all Playa Art
 * <p/>
 * Created by davidbrodsky on 8/3/13.
 */
public class ArtListViewFragment extends PlayaListViewFragment {

    public static ArtListViewFragment newInstance() {
        return new ArtListViewFragment();
    }

    protected CursorRecyclerViewAdapter getAdapter() {
        return new ArtCursorAdapter(getActivity(), null, this);
    }

    @Override
    public Subscription createSubscription() {
        return DataProvider.getInstance(getActivity())
                .flatMap(dataProvider -> dataProvider.observeTable(PlayaDatabase.ART, getAdapter().getRequiredProjection()))
                .map(SqlBrite.Query::run)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cursor -> {
                            Timber.d("Data onNext");
                            onDataChanged(cursor);
                        },
                        throwable -> Timber.e(throwable, "Data onError"),
                        () -> Timber.d("Data onComplete"));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
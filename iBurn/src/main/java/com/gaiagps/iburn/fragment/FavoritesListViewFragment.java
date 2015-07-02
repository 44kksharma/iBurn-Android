package com.gaiagps.iburn.fragment;

import com.gaiagps.iburn.adapters.CursorRecyclerViewAdapter;
import com.gaiagps.iburn.adapters.PlayaSearchResponseCursorAdapter;
import com.gaiagps.iburn.database.DataProvider;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by davidbrodsky on 8/3/13.
 */
public class FavoritesListViewFragment extends PlayaListViewFragment {

    public static CampListViewFragment newInstance() {
        return new CampListViewFragment();
    }

    protected CursorRecyclerViewAdapter getAdapter() {
        return new PlayaSearchResponseCursorAdapter(getActivity(), null, this);
    }

    @Override
    protected Subscription subscribeToData() {

        return DataProvider.getInstance(getActivity())
                .flatMap(dataProvider -> dataProvider.observeFavorites(getAdapter().getRequiredProjection()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(query -> {
                    onDataChanged(query.run());
                });
    }

}

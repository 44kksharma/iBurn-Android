package com.gaiagps.iburn.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gaiagps.iburn.R;
import com.gaiagps.iburn.adapters.CursorRecyclerViewAdapter;
import com.gaiagps.iburn.adapters.DividerItemDecoration;
import com.gaiagps.iburn.adapters.EventCursorAdapter;
import com.gaiagps.iburn.database.DataProvider;
import com.gaiagps.iburn.view.PlayaListViewHeader;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Fragment displaying all Playa Events
 * <p/>
 * Created by davidbrodsky on 8/3/13.
 */
public class EventListViewFragment extends PlayaListViewFragment implements PlayaListViewHeader.PlayaListViewHeaderReceiver {

    public static CampListViewFragment newInstance() {
        return new CampListViewFragment();
    }

    private String selectedDay;
    private ArrayList<String> selectedTypes;

    protected CursorRecyclerViewAdapter getAdapter() {
        return new EventCursorAdapter(getActivity(), null, this);
    }

    @Override
    protected Subscription subscribeToData() {
        // TODO : Filter on selectedDay / selectedTypes
        return DataProvider.getInstance(getActivity())
                .observeEventsOnDayOfTypes(selectedDay, selectedTypes)
                .subscribe(new Action1<SqlBrite.Query>() {
                    @Override
                    public void call(SqlBrite.Query query) {
                        onDataChanged(query.run());
                    }
                });
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_list_view, container, false);
        mEmptyText = (TextView) v.findViewById(android.R.id.empty);
        mRecyclerView = ((RecyclerView) v.findViewById(android.R.id.list));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        ((PlayaListViewHeader) v.findViewById(R.id.header)).setReceiver(this);
        return v;
    }

    @Override
    public void onSelectionChanged(String day, ArrayList<String> types) {
        selectedDay = day;
        selectedTypes = types;
        subscribeToData();
    }
}
package com.gaiagps.iburn.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gaiagps.iburn.R;
import com.gaiagps.iburn.adapters.AdapterUtils;

import java.util.ArrayList;

/**
 * A ListView header presenting filter options for day and type
 * <p/>
 * Clients register for feedback with
 * {@link #setReceiver(com.gaiagps.iburn.view.PlayaListViewHeader.PlayaListViewHeaderReceiver)}
 * <p/>
 * Created by davidbrodsky on 8/2/14.
 */
public class PlayaListViewHeader extends RelativeLayout {
    public static final String TAG = "PlayaListViewHeader";

    protected TextView mTypeFilter;
    protected TextView mDayFilter;

    protected String mDaySelection;
    protected ArrayList<String> mTypeSelection = new ArrayList<>();
    protected int mDaySelectionIndex;
    protected boolean[] mTypeSelectionIndexes = new boolean[100];


    private PlayaListViewHeaderReceiver mReceiver;

    public PlayaListViewHeader(Context context) {
        super(context);
        init(context);
    }

    public PlayaListViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayaListViewHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * Interface for users to receive feedback from this view
     */
    public interface PlayaListViewHeaderReceiver {
        void onSelectionChanged(String day, ArrayList<String> types);
    }

    /**
     * Click listener for Sort buttons
     */
    protected OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (!v.isSelected()) {
                v.setSelected(true);
                if (v.getTag().equals("type")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getContext().getString(R.string.filter_by_type));
                    builder.setMultiChoiceItems(AdapterUtils.sEventTypeNames.toArray(new CharSequence[AdapterUtils.sEventTypeNames.size()]),
                            mTypeSelectionIndexes, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    CharSequence selection = AdapterUtils.sEventTypeAbbreviations.toArray(new CharSequence[AdapterUtils.sEventTypeAbbreviations.size()])[which];
                                    String tabTitle = null;
                                    if (isChecked) {
                                        mTypeSelectionIndexes[which] = true;
                                        mTypeSelection.add((selection == null) ? null : selection.toString());
                                        tabTitle = (selection == null) ? getResources().getString(R.string.any_type) : AdapterUtils.sEventTypeNames.toArray(new CharSequence[AdapterUtils.sEventTypeNames.size()])[which].toString();
                                    } else {
                                        mTypeSelectionIndexes[which] = false;
                                        mTypeSelection.remove((selection == null) ? null : selection.toString());
                                        tabTitle = (mTypeSelection.size() == 0) ? getResources().getString(R.string.any_type) : AdapterUtils.sEventTypeNames.get(AdapterUtils.sEventTypeAbbreviations.indexOf(mTypeSelection.get(mTypeSelection.size() - 1)));
                                    }

                                    if (mTypeSelection.size() > 1) tabTitle += "+" + String.valueOf(mTypeSelection.size() - 1);
                                    ((TextView) v).setText(tabTitle.toUpperCase());
                                    dispatchSelection();
                                }
                            });
                    builder.setPositiveButton(getContext().getString(R.string.done), null);
                    builder.show();
                } else if (v.getTag().equals("day")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getContext().getString(R.string.filter_by_day));
                    builder.setSingleChoiceItems(AdapterUtils.sDayNames.toArray(new CharSequence[AdapterUtils.sDayNames.size()]),
                            mDaySelectionIndex,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mDaySelectionIndex = which;
                                    CharSequence selection = AdapterUtils.sDayAbbreviations.toArray(new CharSequence[AdapterUtils.sDayAbbreviations.size()])[which];
                                    mDaySelection = (selection == null) ? null : selection.toString();
                                    String tabTitle = (selection == null) ? getResources().getString(R.string.any_day) : AdapterUtils.sDayNames.toArray(new CharSequence[AdapterUtils.sDayNames.size()])[which].toString();
                                    ((TextView) v).setText(tabTitle.toUpperCase());
                                    dispatchSelection();
                                }
                            }
                    );
                    builder.setPositiveButton("Done", null);
                    builder.show();
                }
                v.setSelected(false);
            }
        }
    };

    protected void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.list_view_header_three, this, false);
        mTypeFilter = (TextView) v.findViewById(R.id.typeFilter);
        mDayFilter = (TextView) v.findViewById(R.id.dateFilter);
        setupTouchListeners();
        addView(v);
    }

    public void setReceiver(PlayaListViewHeaderReceiver receiver) {
        mReceiver = receiver;
    }

    protected void setupTouchListeners() {
        mTypeFilter.setTag("type");
        mDayFilter.setTag("day");

        mTypeFilter.setOnClickListener(mOnClickListener);
        mDayFilter.setOnClickListener(mOnClickListener);
    }

    protected void dispatchSelection() {
        if (mReceiver != null) {
            mReceiver.onSelectionChanged(mDaySelection, mTypeSelection);
        }
    }

}

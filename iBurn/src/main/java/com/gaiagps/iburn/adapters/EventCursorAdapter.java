package com.gaiagps.iburn.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.gaiagps.iburn.Constants;
import com.gaiagps.iburn.R;
import com.gaiagps.iburn.database.EventTable;

public class EventCursorAdapter extends SimpleCursorAdapter{


	public EventCursorAdapter(Context context, Cursor c){
		super(context, R.layout.double_listview_item, c, new String[]{} , new int[]{}, 0);
	}

	@Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        ViewCache view_cache = (ViewCache) view.getTag(R.id.list_item_cache);
        if (view_cache == null) {
        	view_cache = new ViewCache();
        	view_cache.title = (TextView) view.findViewById(R.id.list_item_title);
        	view_cache.sub = (TextView) view.findViewById(R.id.list_item_sub);
        	//view_cache.thumbnail = (ImageView) view.findViewById(R.id.list_item_image);
            
        	view_cache.title_col = cursor.getColumnIndexOrThrow(EventTable.name);
        	view_cache.sub_col = cursor.getColumnIndexOrThrow(EventTable.startTime);
        	view_cache._id_col = cursor.getColumnIndexOrThrow(EventTable.id);
        	if(cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.allDay)) == 1 ){
        		view_cache.all_day = true;
        		view_cache.time_label = "All " + cursor.getString(cursor.getColumnIndexOrThrow(EventTable.startTimePrint));
        	}
        	else {
        		view_cache.all_day = false;
        		view_cache.time_label = cursor.getString(cursor.getColumnIndexOrThrow(EventTable.startTimePrint));
        	}
        	view_cache._id_col = cursor.getColumnIndexOrThrow(EventTable.id);
        }
        view_cache.title.setText(cursor.getString(view_cache.title_col));
        view_cache.sub.setText(view_cache.time_label);

        view.setTag(R.id.list_item_related_model, cursor.getInt(view_cache._id_col));
        view.setTag(R.id.list_item_related_model_type, Constants.PLAYA_ITEM.EVENT);
    }
	
	// Cache the views within a ListView row item 
    static class ViewCache {
        TextView title;
        TextView sub;
        
        boolean all_day;
        String time_label;
        
        int title_col; 
        int sub_col;
        int _id_col;
    }
}

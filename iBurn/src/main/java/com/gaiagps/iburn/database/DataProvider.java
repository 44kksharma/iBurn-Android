package com.gaiagps.iburn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gaiagps.iburn.Constants;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;

import rx.Observable;
import timber.log.Timber;

/**
 * Class for interaction with our database via Reactive streams.
 * This is intended as an experiment to replace our use of {@link android.content.ContentProvider}
 * as it does not meet all of our needs (e.g: Complex UNION queries not possible with Schematic's
 * generated version, and I believe manually writing a ContentProvider is too burdensome and error-prone)
 * <p/>
 * Created by davidbrodsky on 6/22/15.
 */
public class DataProvider {

    /**
     * Computed column indicating type for queries that union results across tables
     */
    public static String VirtualType = "type";

    static final String[] Projection = new String[]{
            PlayaItemTable.id,
            PlayaItemTable.name,
            PlayaItemTable.latitude,
            PlayaItemTable.longitude
    };

    static String generatedProjectionString;

    static {
        StringBuilder builder = new StringBuilder();
        for (String column : Projection) {
            builder.append(column);
            builder.append(',');
        }
        // Remove the last comma
        generatedProjectionString = builder.substring(0, builder.length() - 1);
    }

    private static DataProvider provider;

    private SqlBrite db;

    public static DataProvider getInstance(@NonNull Context context) {
        if (provider == null) {
            com.gaiagps.iburn.database.generated.PlayaDatabase db = com.gaiagps.iburn.database.generated.PlayaDatabase.getInstance(context);
            provider = new DataProvider(SqlBrite.create(db));
        }

        return provider;
    }

    public static SqlBrite getSqlBriteInstance(@NonNull Context context) {
        return getInstance(context).getDb();
    }

    private DataProvider(SqlBrite db) {
        this.db = db;
    }

    public SqlBrite getDb() {
        return db;
    }

    public Observable<SqlBrite.Query> observeTable(@NonNull String table) {
        return db.createQuery(table, "SELECT * FROM " + table);
    }

    public Observable<SqlBrite.Query> observeEventsOnDayOfTypes(@Nullable String day,
                                                                @Nullable ArrayList<String> types) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ");
        sql.append(PlayaDatabase.EVENTS);

        if (day != null || (types != null && types.size() > 0))
            sql.append(" WHERE ");

        if (types != null) {
            for (int x = 0; x < types.size(); x++) {
                sql.append('(')
                        .append(EventTable.eventType)
                        .append('=')
                        .append(DatabaseUtils.sqlEscapeString(types.get(x)))
                        .append(')');

                if (x < types.size() - 1) sql.append(" OR ");
            }

        }

        if (day != null) {
            if (types != null && types.size() > 0) sql.append(" AND ");

            sql.append(EventTable.startTimePrint)
                    .append(" LIKE ")
                    .append("'%")
                    .append(DatabaseUtils.sqlEscapeString(day).replace("\'", ""))
                    .append("%'");
        }
        Timber.d("Event filter query " + sql.toString());
        return db.createQuery(PlayaDatabase.EVENTS, sql.toString());

    }

    public Observable<SqlBrite.Query> observeFavorites() {

        StringBuilder sql = new StringBuilder();
        int tableIdx = 0;
        for (String table : PlayaDatabase.ALL_TABLES) {
            tableIdx++;

            sql.append("SELECT ")
                    .append(generatedProjectionString)
                    .append(", ")
                    .append(tableIdx)
                    .append(" as ")
                    .append(VirtualType)
                    .append(" FROM ")
                    .append(table)
                    .append(" WHERE ")
                    .append(PlayaItemTable.favorite)
                    .append(" = 1 ");

            if (tableIdx < PlayaDatabase.ALL_TABLES.size())
                sql.append(" UNION ");
        }

        return db.createQuery(PlayaDatabase.ALL_TABLES, sql.toString(), null);
    }

    public Observable<SqlBrite.Query> observeQuery(String query) {

        query = DatabaseUtils.sqlEscapeString(query);

        StringBuilder sql = new StringBuilder();
        int tableIdx = 0;
        for (String table : PlayaDatabase.ALL_TABLES) {
            tableIdx++;

            sql.append("SELECT ")
                    .append(generatedProjectionString)
                    .append(", ")
                    .append(tableIdx)
                    .append(" as ")
                    .append(VirtualType)
                    .append(" FROM ")
                    .append(table)
                    .append(" WHERE ")
                    .append(PlayaItemTable.name)
                    .append(" LIKE '%")
                    .append(query)
                    .append("%' ");

            if (tableIdx < PlayaDatabase.ALL_TABLES.size())
                sql.append(" UNION ");
        }

        return db.createQuery(PlayaDatabase.ALL_TABLES, sql.toString(), null);
    }

    public void updateFavorite(String table, int id, boolean isFavorite) {
        ContentValues values = new ContentValues(1);
        values.put(PlayaItemTable.favorite, isFavorite ? 1 : 0);
        db.update(table, values, PlayaItemTable.id + "=?", String.valueOf(id));
    }

    /**
     * @return the int value used in virtual columns to represent a {@link com.gaiagps.iburn.Constants.PlayaItemType}
     */
    public static int getTypeValue(Constants.PlayaItemType type) {
        switch (type) {
            case CAMP:
                return 1;
            case ART:
                return 2;
            case EVENT:
                return 3;
            case POI:
                return 4;
        }
        Timber.w("Unknown PlayaItemType");
        return -1;
    }

    public static Constants.PlayaItemType getTypeValue(int type) {
        switch (type) {
            case 1:
                return Constants.PlayaItemType.CAMP;
            case 2:
                return Constants.PlayaItemType.ART;
            case 3:
                return Constants.PlayaItemType.EVENT;
            case 4:
                return Constants.PlayaItemType.POI;
        }
        throw new IllegalArgumentException("Invalid type value");
    }
}

package com.gaiagps.iburn.database;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * ContentProvider definition. This defines a familiar API
 * for Android framework components to utilize.
 *
 * Created by davidbrodsky on 7/28/14.
 */
@ContentProvider(authority = PlayaContentProvider.AUTHORITY, database = PlayaDatabase.class)
public final class PlayaContentProvider {

    public static final String AUTHORITY      = "com.gaiagps.iburn.playacontentprovider";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    /** Art API **/

    @TableEndpoint(table = PlayaDatabase.ART)
    public static class Art {

        private static final String ENDPOINT = "art";

        @ContentUri(
                path = ENDPOINT,
                type = "vnd.android.cursor.dir/list",
                defaultSort = ArtTable.name + " ASC")
        public static final Uri ART = buildUri(ENDPOINT);

    }

    /** Camps API **/

    @TableEndpoint(table = PlayaDatabase.CAMPS)
    public static class Camps {

        private static final String ENDPOINT = "camps";

        @ContentUri(
                path = ENDPOINT,
                type = "vnd.android.cursor.dir/list",
                defaultSort = CampTable.name + " ASC")
        public static final Uri CAMPS = buildUri(ENDPOINT);

    }

    /** Events API **/

    @TableEndpoint(table = PlayaDatabase.EVENTS)
    public static class Events {

        private static final String ENDPOINT = "events";

        @ContentUri(
                path = ENDPOINT,
                type = "vnd.android.cursor.dir/list",
                defaultSort = EventTable.startTime + " ASC")
        public static final Uri EVENTS = buildUri(ENDPOINT);
    }

    /** User POI API **/

    @TableEndpoint(table = PlayaDatabase.POIS)
    public static class Pois {

        private static final String ENDPOINT = "pois";

        @ContentUri(
                path = ENDPOINT,
                type = "vnd.android.cursor.dir/list",
                defaultSort = UserPoiTable.name + " ASC")
        public static final Uri POIS = buildUri(ENDPOINT);
    }
}

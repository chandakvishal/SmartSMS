package com.example.vinay.sms.Search;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.vinay.sms.Utilities.DatabaseHandler;

import java.util.List;

public class SearchSuggestionProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        DatabaseHandler db = new DatabaseHandler(getContext());

        String inboxTable = "_INBOX";
        List<String> cities = db.searchTable(uri.getLastPathSegment(), inboxTable);

        MatrixCursor cursor = new MatrixCursor(
                new String[]{
                        BaseColumns._ID,
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
                }
        );

        if (cities != null) {
            String query = uri.getLastPathSegment().toUpperCase();
            int limit = Integer.parseInt(uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT));

            int length = cities.size();
            for (int i = 0; i < length && cursor.getCount() < limit; i++) {
                String city = cities.get(i);
                if (city.toUpperCase().contains(query)) {
                    cursor.addRow(new Object[]{i, city, i});
                }
            }
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

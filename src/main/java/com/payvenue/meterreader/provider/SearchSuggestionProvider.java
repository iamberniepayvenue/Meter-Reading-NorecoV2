package com.payvenue.meterreader.provider;

import android.content.SearchRecentSuggestionsProvider;

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "com.payvenue.SearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY,MODE);
    }
}

package com.phone.data;

import android.net.Uri;

public class Browser {

	static public class BookmarkColumns{
		public static final String TITLE = "title";
		public static final String URL = "url";
		public static final String DATE = "date";
		public static final String VISITS = "visits";
		public static final String BOOKMARK = "bookmark";
		public static final String CREATED = "created";
		
	}

	public static final Uri BOOKMARKS_URI = Uri.parse("content://browser/bookmarks");


}

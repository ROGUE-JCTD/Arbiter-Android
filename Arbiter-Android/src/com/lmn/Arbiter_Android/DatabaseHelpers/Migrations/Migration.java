package com.lmn.Arbiter_Android.DatabaseHelpers.Migrations;

import android.database.sqlite.SQLiteDatabase;

public interface Migration {

	public void migrate(SQLiteDatabase db);
}

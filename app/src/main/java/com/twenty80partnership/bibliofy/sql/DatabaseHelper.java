package com.twenty80partnership.bibliofy.sql;

import android.content.ContentValues;
	import android.content.Context;
	import android.database.Cursor;
	import android.database.sqlite.SQLiteDatabase;
	import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

	    public final static String DATABASE_NAME = "Bibliofy.db";
	    public final static String TABLE_NAME1 = "banner_table";
	    public static final String COL_1a = "ID";
	    public static final String COL_2a = "IMG";
	    public static final String COL_3a = "NAME";
	    public static final String COL_4a = "PRIORITY";

		public final static String TABLE_NAME2 = "update_info_table";
		public static final String COL_1b = "ID";
		public static final String COL_2b = "NAME";
		public static final String COL_3b = "VERSION";
		public static final String COL_4b = "DATEADDED";

	public final static String TABLE_NAME3 = "cart_table";
	public static final String COL_1c = "ITEM_ID";
	public static final String COL_2c = "LOCATION";
	public static final String COL_3c = "TYPE";
	public static final String COL_4c = "QUANTITY";
	public static final String COL_5c = "TIME_ADDED";

	    public DatabaseHelper(Context context) {
        	        super(context, DATABASE_NAME, null, 1);
        	    }

        	    @Override
        public void onCreate(SQLiteDatabase db) {
					Log.d("sqlitetesting","oncreate is called");
					db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME1+" (ID TEXT PRIMARY KEY,IMG TEXT,NAME TEXT,PRIORITY INTEGER)");
					db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME2+"(ID INTEGER PRIMARY KEY,NAME TEXT,VERSION INTEGER,DATEADDED TEXT)");
					db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME3+" (ITEM_ID TEXT PRIMARY KEY,LOCATION TEXT,TYPE TEXT,QUANTITY INTEGER,TIME_ADDED TEXT)");
				}

        	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME1);
					db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME2);
					db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME3);

					onCreate(db);
        	    }

        	    public boolean insertData1(String id, String img,String name, int priority) {
        	        SQLiteDatabase db = this.getWritableDatabase();
        	        ContentValues cv = new ContentValues();
        	        cv.put(COL_1a, id);
                    cv.put(COL_2a, img);
                    cv.put(COL_3a, name);
        	        cv.put(COL_4a, priority);
        	        long result = db.insert(TABLE_NAME1, null, cv);
        	        if (result == -1) return false;
        	        else return true;
        	    }

        	    public Cursor getData1(String id){
        	        SQLiteDatabase db = this.getWritableDatabase();
        	        String query="SELECT * FROM "+TABLE_NAME1+" WHERE ID='"+id+"'";
        	        Cursor  cursor = db.rawQuery(query,null);
        	        return cursor;
        	    }

        	    public boolean updateData1(String id, String img,String name, int priority) {
        	        SQLiteDatabase db = this.getWritableDatabase();
                    ContentValues cv = new ContentValues();
                    cv.put(COL_1a, id);
                    cv.put(COL_2a, img);
                    cv.put(COL_3a, name);
                    cv.put(COL_4a, priority);
        	        long result = db.update(TABLE_NAME1, cv, "ID=?", new String[]{id});
                    if (result == -1) return false;
                    else return true;
            }

        	    public Integer deleteData1 (String id) {
        	        SQLiteDatabase db = this.getWritableDatabase();
        	        return db.delete(TABLE_NAME1, "ID = ?", new String[]{id});
        	    }

        	    public Cursor getAllData1() {
        	        SQLiteDatabase db = this.getWritableDatabase();
        	        Cursor res = db.rawQuery("SELECT * FROM "+TABLE_NAME1, null);
        	        return res;
        	    }

	public boolean insertData2(int id,String name,int version) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String date = sdf.format(new Date());

		cv.put(COL_1b, id);
		cv.put(COL_2b, name);
		cv.put(COL_3b, version);
		cv.put(COL_4b, date);
		long result = db.insert(TABLE_NAME2, null, cv);
		if (result == -1) return false;
		else return true;
	}


	public Cursor getData2(int id){
		SQLiteDatabase db = this.getWritableDatabase();
		String query="SELECT * FROM "+TABLE_NAME2+" WHERE ID='"+id+"'";
		Cursor  cursor = db.rawQuery(query,null);
		return cursor;
	}

	public boolean updateData2(Integer id,String name,int version) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String date = sdf.format(new Date());
		cv.put(COL_1b, id);
		cv.put(COL_2b, name);
		cv.put(COL_3b, version);
		cv.put(COL_4b, date);
		long result = db.update(TABLE_NAME2, cv, "ID=?", new String[]{id.toString()});
		if (result == -1) return false;
		else return true;
	}

	public Integer deleteData2 (Integer id) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_NAME2, "ID = ?", new String[]{id.toString()});
	}

	public Cursor getAllData2() {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor res = db.rawQuery("SELECT * FROM "+TABLE_NAME2, null);
		return res;
	}

	public boolean insertData3(String id, String location,String type, int quantity, String timeAdded) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COL_1c, id);
		cv.put(COL_2c, location);
		cv.put(COL_3c, type);
		cv.put(COL_4c, quantity);
		cv.put(COL_5c,timeAdded);
		long result = db.insert(TABLE_NAME3, null, cv);
		if (result == -1) return false;
		else return true;
	}

	public Cursor getData3(String id){
		SQLiteDatabase db = this.getWritableDatabase();
		String query="SELECT * FROM "+TABLE_NAME3+" WHERE ID='"+id+"' ORDER BY TIMEADDED DESC";
		Cursor  cursor = db.rawQuery(query,null);
		return cursor;
	}

	public boolean updateData3(String id, String location,String type, int quantity,String timeAdded) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COL_1c, id);
		cv.put(COL_2c, location);
		cv.put(COL_3c, type);
		cv.put(COL_4c, quantity);
		cv.put(COL_5c,timeAdded);
		long result = db.update(TABLE_NAME3, cv, "ID=?", new String[]{id});
		if (result == -1) return false;
		else return true;
	}

	public Integer deleteData3 (String id) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_NAME3, "ITEM_ID = ?", new String[]{id});
	}

	public Cursor deleteAllData3 () {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor res = db.rawQuery("DELETE FROM "+TABLE_NAME3, null);
		return res;    }

	public Cursor getAllData() {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor res = db.rawQuery("SELECT * FROM "+TABLE_NAME3+" ORDER BY TIME_ADDED DESC", null);
		return res;
	}
	}

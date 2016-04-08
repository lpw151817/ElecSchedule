package nercms.schedule.utils;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AttachmentDatabase {
	public static final String DATABASE_NAME = "App.db";
	public static final int VERSION = 1;
	
	//单键处理
	private volatile static AttachmentDatabase _unique_instance = null;
	
	private AttachmentDatabase()
	{		
	}
	
	//注意每个进程都会创建一个EvidenceDatabase实例
	//绝对不可采用同步方法的方式，同步方法仅对类的一个实例起作用
	public static AttachmentDatabase instance(Context context)
	{
		//context实际无用
		
		// 检查实例,如是不存在就进入同步代码区
		if(null == _unique_instance)
		{
			// 对其进行锁,防止两个线程同时进入同步代码区
			synchronized(AttachmentDatabase.class)
			{
				//必须双重检查
				if(null == _unique_instance)
				{
					_unique_instance = new AttachmentDatabase();
					//_unique_instance.open_or_create_database(GD.get_global_context());
					_unique_instance.open_or_create_database(context);
				}
			}
		}
		
		return _unique_instance;
	}
	
    private static final String LOG_TAG = "Evidence";
    
    private EvidenceDatabaseHelper _evidence_database_helper = null;
    
    private SQLiteDatabase _db = null;
    
    private void open_or_create_database(Context context)
    {
    	Log.v(LOG_TAG, "open_or_create_database");
    	
    	if(null == _evidence_database_helper)
    	{
    		Log.v(LOG_TAG, "create EvidenceDatabaseHelper");
    		
    		_evidence_database_helper = new EvidenceDatabaseHelper(context, DATABASE_NAME, VERSION);
    	}
    	
    	if(null == _evidence_database_helper)
    	{
    		_db = null;
    		return;
    	}
    	
    	//getWritableDatabase()和getReadableDatabase()方法都可以获取一个用于操作数据库的SQLiteDatabase实例
    	//但getWritableDatabase() 方法以读写方式打开数据库，一旦数据库的磁盘空间满了，数据库就只能读而不能写，倘若使用的是getWritableDatabase() 方法就会出错。
    	//getReadableDatabase()方法先以读写方式打开数据库，如果数据库的磁盘空间满了，就会打开失败，当打开失败后会继续尝试以只读方式打开数据库。
    	try
    	{
    		_db = _evidence_database_helper.getWritableDatabase();
    	}
    	catch (SQLiteException e)
    	{
    		_db = _evidence_database_helper.getReadableDatabase();
    	}
    }
    
    public HashMap<String, String> query(String sql)
    {
    	//Log.v(LOG_TAG, "SQLite query: " + sql);
    	
    	if(null == _db)
    		return null;
    	
    	if(null == sql)
    		return null;
    	
    	if(0 == sql.length())
    		return null;
    	
    	try{
    		Cursor cursor = _db.rawQuery(sql, null);
    		
    		if(null == cursor)
    			return null;
    		
    		int raws_num = cursor.getCount();//记录数
    		if(0 == raws_num)
    		{
    			cursor.close();
    			return null;
    		}
    		
    		int columns_num = cursor.getColumnCount();//字段数
    		if(0 == columns_num)
    		{
    			cursor.close();
    			return null;
    		}
    		
    		HashMap<String, String> map = new HashMap<String, String>();
    		
    		map.put("records_num", Integer.toString(raws_num));
    		
            cursor.moveToFirst();
            
            int index = 0;
            while(false == cursor.isAfterLast())
            {
            	for(int i = 0; i < columns_num; ++i)
            	{
            		map.put(cursor.getColumnName(i) + "_" + Integer.toString(index), cursor.getString(i));
            		//Log.v(LOG_TAG, "query (" + cursor.getColumnName(i) + "_" + Integer.toString(index) + ", " + cursor.getString(i) + ")");            		
            	}
            	
            	++index;            	
                cursor.moveToNext();
            }
            
            cursor.close();
            
            return map;
        }
    	catch(SQLException e)
    	{
            Log.v(LOG_TAG, "query error: " + e.toString());
        }
        
        return null;
    }    
    
    public boolean is_exist(String sql)
    {
    	//Log.v(LOG_TAG, "SQLite is_exist: " + sql);
    	
    	if(null == _db)
    		return false;
    	
    	if(null == sql)
    		return false;
    	
    	if(0 == sql.length())
    		return false;
    	
    	try{
    		Cursor cursor = _db.rawQuery(sql, null);
    		
    		if(null != cursor && 0 != cursor.getCount())
    		{
    			cursor.close();
    			return true;
    		}
        }
    	catch(SQLException e)
    	{
            Log.v(LOG_TAG, "query error: " + e.toString());
        }
        
        return false;
    }
    
    public boolean execute(String sql)
    {
    	//Log.v(LOG_TAG, "SQLite execute: " + sql);
    	
    	if(null == _db)
    		return false;
    	
    	if(null == sql)
    		return false;
    	
    	if(0 == sql.length())
    		return false;
    	
    	try
    	{
    		_db.execSQL(sql);
    	}
    	catch(SQLException e)
    	{
            Log.v(LOG_TAG, "execute error: " + e.toString());
        }
    	
    	return true;
    }
    
    public class EvidenceDatabaseHelper extends SQLiteOpenHelper
    {
    	EvidenceDatabaseHelper(Context context, String database_name, int version)
        {
            super(context, database_name, null, version);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db)
        {
        	Log.v(LOG_TAG, "SQLite: onCreate");
        	
        	/*db.execSQL(CREATE_GPS_TABLE_SQL);
        	Log.v(LOG_TAG, "SQLite: onCreate1");
        	db.execSQL(CREATE_EVIDENCE_TABLE_SQL);
        	Log.v(LOG_TAG, "SQLite: onCreate2");
        	db.execSQL(CREATE_PHONE_NUMBER_TABLE_SQL);
        	Log.v(LOG_TAG, "SQLite: onCreate3");
        	db.execSQL(CREATE_LOG_TABLE_SQL);
        	Log.v(LOG_TAG, "SQLite: onCreate4");
        	db.execSQL(CREATE_ORGNIZATION_TABLE_SQL);
        	Log.v(LOG_TAG, "SQLite: onCreate5");
        	db.execSQL(CREATE_GROUP_TABLE_SQL);
        	Log.v(LOG_TAG, "SQLite: onCreate6");
        	db.execSQL(CREATE_RESOURCE_TABLE_SQL);
        	Log.v(LOG_TAG, "SQLite: onCreate7");
        	db.execSQL(CREATE_RESOURCE_POSITION_TABLE_SQL);
        	Log.v(LOG_TAG, "SQLite: onCreate7");*/
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
        	Log.v(LOG_TAG, "SQLite: onUpgrade");
        	
            onCreate(db);
        }
    }
    
    
}
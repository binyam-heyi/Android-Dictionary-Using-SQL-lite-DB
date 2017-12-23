package binyam.Android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple dictionary (word-definition) implemented using a database.
 */
public class DictionaryDbAdapter {

	public static final String KEY_WORD = "word";
	public static final String KEY_DEFINITION = "definition";
	// The name "_id" is required by CursorAdapter!
	public static final String KEY_ROWID = "_id"; 

	private static final String DATABASE_NAME = "dictionary.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_WORDS = "table_words";

	/**
	 * Database creation SQL statement(s)
	 */
	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_WORDS
			+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "word TEXT NOT NULL, definition TEXT NOT NULL);";

	private DatabaseHelper dbHelper;
	private SQLiteDatabase database;
	private final Context context;

	public DictionaryDbAdapter(Context context) {
		this.context = context;
	}

	/**
	 * Open the dictionary database. If the database can't be opened, 
	 * try to create a new instance of the database. If it can't be created, 
	 * getWritableDatabase will throw an exception.
	 */
	public void open() throws SQLException {
		dbHelper = new DatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	/**
	 * Create a new word-definition row and return the rowId on success, -1
	 * otherwise.
	 */
	public long addWord(String word, String definition) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_WORD, word);
		initialValues.put(KEY_DEFINITION, definition);

		return database.insert(TABLE_WORDS, null, initialValues);
	}

	public boolean deleteWord(long rowId) {

		return database.delete(TABLE_WORDS, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all words matching a wild card search,
	 * beginningOfWord%.
	 */
	public Cursor fetchMatchingWords(String beginningOfWord) {
		Cursor cursor = database.query(
				TABLE_WORDS,
				new String[] {KEY_ROWID, KEY_WORD, KEY_DEFINITION },
				KEY_WORD + " LIKE ?", // WHERE
				new String[] { beginningOfWord + "%" }, 
				null, null, null);
		return cursor;
	}

	/**
	 * Return a Cursor positioned at the words that matches the given rowId.
	 */
	public Cursor fetchWord(long rowId) throws SQLException {
		Cursor cursor = database.query(
				true, TABLE_WORDS, 
				new String[] {KEY_ROWID, KEY_WORD, KEY_DEFINITION }, 
				KEY_ROWID + "=" + rowId, // WHERE
				null, null, null, null, null);
		return cursor;
	}
	
	/**
	 * Return a Cursor over the list of all words in the database.
	 */
	public Cursor fetchAllWords() {
		Cursor cursor = database.query(
				TABLE_WORDS, 
				new String[] { KEY_ROWID, KEY_WORD, KEY_DEFINITION }, 
				null, null, null, null, null);
		return cursor;
	}

	/**
	 * Update the (definition of the) word specified by the provided rowId.
	 */
	public boolean updateRow(long rowId, String word, String definition) {
		ContentValues args = new ContentValues();
		args.put(KEY_WORD, word);
		args.put(KEY_DEFINITION, definition);

		return database.update(
				TABLE_WORDS, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	/**
	 * A helper class to manage database creation and version management.
	 */
	private class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			loadWords(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("DictionaryDbAdapter", "Upgrading database from version "
					+ oldVersion + " to " + newVersion
					+ ". All tables will be dropped.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
			onCreate(db);
		}
		
		// Don't mind this - it's just a way to get some words into 
		// the DB to start with (an ugly hack)
	    private void loadWords(SQLiteDatabase db) {
	        Resources resources = context.getResources();
	        BufferedReader reader = null;
	        try {
	        	InputStream is = resources.openRawResource(R.raw.dictionary);
	        	reader = new BufferedReader(new InputStreamReader(is));
	            String word = reader.readLine(), definition;
	            while (word != null && !word.equals("")) {
	                definition = reader.readLine();
	        		ContentValues initialValues = new ContentValues();
	        		initialValues.put(KEY_WORD, word);
	        		initialValues.put(KEY_DEFINITION, definition);
	        		db.insert(TABLE_WORDS, null, initialValues);
	                Log.i("DicitionaryDbAdapter", word + ", " + definition);
	                word = reader.readLine();
	            }
	        }
	        catch(IOException ioe) {
	        	Log.e("DictionaryDbAdapter", "Error loading words");
	        }
	        finally {
	        	try {
	        		if(reader != null) reader.close();
	        	}
	            catch(IOException ioe) {}
	        }
	    }
	}
}

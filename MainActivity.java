package binyam.Android;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {

	private DictionaryDbAdapter dbAdapter;

	private EditText searchInput;
	private Button searchButton;
	private ListView resultView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		resultView = (ListView) findViewById(R.id.ResultListView);
		searchInput = (EditText) findViewById(R.id.SearchInput);
		searchButton = (Button) findViewById(R.id.SearchButton);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchForMatchingWords();
			}
		});

		dbAdapter = new DictionaryDbAdapter(this);
		try {
			dbAdapter.open();
		} catch (SQLException se) {
			showToast("Error opening database");
		}
	}

	public void onDestroy() {
		super.onDestroy();
		// Close the DB
		dbAdapter.close();
	}
	
	private void searchForMatchingWords() {
		String input = searchInput.getText().toString();
		input = input.trim().toLowerCase();
		if (input.length() == 0) {
			return;
		}

		Cursor cursor = dbAdapter.fetchMatchingWords(input);
		adaptCursorToView(cursor);
	}

	private void adaptCursorToView(Cursor cursor) {
		if (cursor.getCount() == 0) {
			showToast("No matching entries were found");
			return;
		}

		logCursorInfo(cursor); // Debug info
		cursor.moveToFirst();
		startManagingCursor(cursor); // Life cycle managed
		String[] from = new String[] { DictionaryDbAdapter.KEY_WORD,
				DictionaryDbAdapter.KEY_DEFINITION };
		int[] to = new int[] { R.id.WordView, R.id.DefinitionView };
		SimpleCursorAdapter entries = new SimpleCursorAdapter(this,
				R.layout.entry_row, cursor, from, to);
		resultView.setAdapter(entries);
	}

	// Call back methods for displaying sub-activity AddActivity, and
	// handle the result
	private static final int SHOW_ADD_ACTIVITY = 1; // Request code(s)

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, Menu.NONE, "Add word");
		menu.add(0, 1, Menu.NONE, "Cancel");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case 0:
			Intent intent = new Intent(MainActivity.this, AddActivity.class);
			startActivityForResult(intent, SHOW_ADD_ACTIVITY);
			return true;
		}
		return false;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		super.onActivityResult(requestCode, resultCode, result);

		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case SHOW_ADD_ACTIVITY:
				String word = 
					result.getStringExtra(DictionaryDbAdapter.KEY_WORD);
				String definition = 
					result.getStringExtra(DictionaryDbAdapter.KEY_DEFINITION);
				dbAdapter.addWord(word, definition);
				showToast("The dictionary was updated with " + word);
				break;
			default:
			}
		}
	}

	// Debug info
	private void logCursorInfo(Cursor cursor) {
		Log.i("Dictionary-v1", "*Cursor begin*");
		Log.i("Dictionary-v1", "Rows = " + cursor.getCount());
		Log.i("Dictionary-v1", "Cols = " + cursor.getColumnCount());
	}

	private void showToast(String msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
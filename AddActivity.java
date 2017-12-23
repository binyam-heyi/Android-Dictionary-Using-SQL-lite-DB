package binyam.Android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddActivity extends Activity {

	private EditText wordEdit, definitionEdit;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add);
		wordEdit = (EditText) findViewById(R.id.WordEdit);
		definitionEdit = (EditText) findViewById(R.id.DefinitionEdit);
	}
	
	// These methods are marked as event listeners in the add.xml layout file
	// (button property "OnClicked")
	public void onOkClicked(View view) {
		String word =  
			wordEdit.getText().toString().trim().toLowerCase();
		String definition =  
			definitionEdit.getText().toString().trim();
		
		if(word.length() > 0 && definition.length() > 0) {
			Intent result = new Intent();
			result.putExtra(DictionaryDbAdapter.KEY_WORD, word);
			result.putExtra(DictionaryDbAdapter.KEY_DEFINITION, definition);
			setResult(Activity.RESULT_OK, result);
			finish();
		}
	}
	
	public void onCancelClicked(View view) {
		Intent intent = new Intent();
		setResult(Activity.RESULT_CANCELED, intent);
		finish();
	}
}

package com.jgeorgiou.committomemory;

/**
 * This abstract class enforces required methods for any activity in Commit To Memory. 
 * These methods ensure that the speech recognizer callbacks are handled 
 * and error messages are handled.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;

public abstract class ActivityCommitToMemory extends Activity{
	public abstract SpeechRecognizer getSpeechRecognizer();
	public abstract Intent getSpeechIntent();
	
	public abstract void setReadyForSpeech();
	public abstract void setOnEndOfSpeech();	
	public abstract void setOnResults(Bundle results);
	public abstract void setOnPartialResults(Bundle partialResults);
	public abstract void showError(String err);
	public abstract void closeApp();

}

package com.jgeorgiou.committomemory;

/**
 * This speech listener implements the required methods for Android's RecognitionListener and 
 * works with the Commit To Memory activities that utilize speech recognition.
 */

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class SpeechListener implements RecognitionListener {
	public ActivityCommitToMemory mainActivity;
	public SpeechRecognizer speechRecognizer;
	public Intent speechIntent;

	private static final String TAG_LISTENER = "spListener";
	
	public SpeechListener(ActivityCommitToMemory myActivity) {
		mainActivity = myActivity;
		speechRecognizer = mainActivity.getSpeechRecognizer();
		speechIntent = mainActivity.getSpeechIntent();			
	}

	public void onReadyForSpeech(Bundle params) {
		Log.d(TAG_LISTENER, "onReadyForSpeech");
		mainActivity.setReadyForSpeech();	
	}

	public void onBeginningOfSpeech() {
		Log.d(TAG_LISTENER, "onBeginningOfSpeech");
	}

	public void onRmsChanged(float rmsdB) {
		// Log.d(TAG_LISTENER, "onRmsChanged");
	}

	public void onBufferReceived(byte[] buffer) {
		Log.d(TAG_LISTENER, "onBufferReceived");
	}

	public void onEndOfSpeech() {
		Log.d(TAG_LISTENER, "onEndofSpeech");
		mainActivity.setOnEndOfSpeech();
	}

	public void onError(int error) {
		String TAG_ERROR = "SpError";

		Log.d(TAG_LISTENER, "Error code: " + error);
		String message;

		switch (error) {
		case SpeechRecognizer.ERROR_AUDIO:
			message = "Audio recording error";
			Log.d(TAG_ERROR, message);
			break;

		case SpeechRecognizer.ERROR_CLIENT:
			message = "Client side error";
			Log.d(TAG_ERROR, message);
			speechRecognizer.stopListening();
			speechRecognizer.startListening(speechIntent);
			break;

		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
			message = "Insufficient permissions";
			Log.d(TAG_ERROR, message);
			break;

		case SpeechRecognizer.ERROR_NETWORK:
			message = "Network error";
			Log.d(TAG_ERROR, message);
			break;

		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
			message = "Network timeout";
			Log.d(TAG_ERROR, message);
			break;

		case SpeechRecognizer.ERROR_NO_MATCH:
			message = "No match";
			Log.d(TAG_ERROR, message);
			break;

		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
			message = "RecognitionService busy";
			Log.d(TAG_ERROR, message);
			break;

		case SpeechRecognizer.ERROR_SERVER:
			message = "error from server";
			Log.d(TAG_ERROR, message);
			break;

		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			message = "No speech input";
			Log.d(TAG_ERROR, message);
			speechRecognizer.startListening(speechIntent);
			break;

		default:
			message = "Didn't understand, please try again.";
			break;
		}

	}

	public void onEvent(int eventType, Bundle params) {
		Log.d(TAG_LISTENER, "onEvent " + eventType);
	}

	public void onResults(Bundle results) {
		Log.d(TAG_LISTENER, "onResults " + results);
		mainActivity.setOnResults(results);
	}

	public void onPartialResults(Bundle partialResults) {
		Log.d(TAG_LISTENER, "onPartialResults " + partialResults);
		mainActivity.setOnPartialResults(partialResults);
	}
	
}

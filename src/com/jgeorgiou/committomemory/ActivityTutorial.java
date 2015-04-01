package com.jgeorgiou.committomemory;

/**
 * The tutorial teaches the user the voice and gestures that can used to navigate through the application
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.*;

public class ActivityTutorial extends ActivityCommitToMemory{
	
	private final static int TOTAL_CARDS = 10; //THERE ARE ONLY 10 LESSONS
	private int card_index = 0;

	private TextView header = null;
	private TextView action = null;
	private TextView status = null;
	private TextView index = null;
	private String str_status = "Listening";

	private GestureDetector mGestureDetector = null;
	protected Intent speechIntent = null;
	private SpeechRecognizer speechRecognizer = null;
	private MediaPlayer mPlayer = null;
	private TextToSpeech tts;

	private final String TAG_DEBUG = "SPEECH_RESULTS";
	private final String TAG_SPEECH = "SpeechRecog";

	private String[] next_dictionary = { "next", "text", "max", "fax" };
	private String[] previous_dictionary = { "previous", "prettiest" };
	private String[] flip_dictionary = { "flip", "flipcard", "flat", "foot",
			"slept", "slip", "flips", "flipped" };
	private String[] start_dictionary = { "start", "star" };
	private String[] mainmenu_dictionary = { "main", "menu" };
	private String[] exit_dictionary = { "exit" };
	private String[] play_dictionary = { "play" };

	private String str_tts = "";
	private String currentText = "\"Start\"";

	private boolean activityStarted = false;
	private boolean commandSet = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		displayIntroCard();
		initTTS();
		mGestureDetector = createGestureDetector(this);
		initSpeechRecognizer();
	}

	/**
	 * Set up "start" card
	 */
	protected void displayIntroCard() {
		setContentView(R.layout.layout_titlepage);
		TextView title_name = (TextView) findViewById(R.id.activity_name);
		title_name.setText(R.string.tutorial);
		TextView title_command = (TextView) findViewById(R.id.activity_command);
		title_command.setText(R.string.start);
	}

	/**
	 * Create the text-to-speech component
	 */
	protected void initTTS() {
		tts = new TextToSpeech(getApplicationContext(),
				new TextToSpeech.OnInitListener() {

					@Override
					public void onInit(int s) {
						if (s == TextToSpeech.SUCCESS) {
							tts.setSpeechRate((float) 0.75);
							tts.setLanguage(Locale.ENGLISH);
						}
					}
				});
	}
	/**
	 * Create the speech recognizer, listener and intent
	 */
	protected void initSpeechRecognizer() {
		speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
				getApplication().getPackageName());
		speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		speechRecognizer.setRecognitionListener(new SpeechListener(this));

		if (SpeechRecognizer.isRecognitionAvailable(this)) {
			speechRecognizer.startListening(speechIntent);

		} else {
			Log.d(TAG_SPEECH, "Recgonition not available on this device.");
			showError("Speech recgonition not available on this device.");
		}
	}
	@Override
	protected SpeechRecognizer getSpeechRecognizer() {
		return speechRecognizer;
	}

	@Override
	public Intent getSpeechIntent() {
		return speechIntent;
	}

	@Override
	public void setReadyForSpeech() {
		if (activityStarted) {
			str_status = "Listening";
			status.setText(str_status);
		}	
	}

	@Override
	public void setOnEndOfSpeech() {
		if (activityStarted) {
			str_status = "Processing";
			status.setText(str_status);
		}
		commandSet = false;
	}

	@Override
	public void setOnResults(Bundle results) {
		speechRecognizer.startListening(speechIntent);
		commandSet = false;		
	}

	@Override
	public void setOnPartialResults(Bundle partialResults) {
		if (!commandSet) {
			if (activityStarted) {
				str_status = "Processing";
				status.setText(str_status);
			}	
			ArrayList<String> data = partialResults
					.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

			int numOfResults = data.size();
			Log.d(TAG_SPEECH, "Data size = " + numOfResults);
			String speech_text = "";
			for (int i = 0; i < numOfResults; i++) {
				speech_text += data.get(i);
				Log.d(TAG_SPEECH, "result= " + data.get(i));
			}
			processResults(data, numOfResults);
		}
		
	}
	
	/**
	 * Create the gesture detector and only allow gestures when the appropriate card is displayed
	 */
	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {

					if (card_index == 0) {
						setUpTutorial();
						return true;
					}
					else if (card_index == 6) {
						tts.stop();
						card_index++;
						header.setText("Great!");
						currentText = "\"Play\"";
						action.setText(currentText);
						index.setText("" + card_index + "/" + TOTAL_CARDS);
						str_tts = "Great, here starts the fun part, say play to play the song";
						tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
						return true;
					}
					return true;
				}
				else if (gesture == Gesture.SWIPE_RIGHT) {
					if (card_index == 2) {
						tts.stop();
						card_index++;
						header.setText("Now say,");
						currentText = "\"Previous\"";
						action.setText(currentText);
						index.setText("" + card_index + "/" + TOTAL_CARDS);
						str_tts = "Great, now say previous to go to the previous card";
						tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
						return true;
					} else if (card_index == 4) {
						displayOtherWayCard();
					}

					else if (card_index == 10) {
						goToLastCard();
					}

					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {

					if (card_index == 4) {
						tts.stop();
						card_index++;
						header.setText("Success!");
						currentText = "\"Flip\"";
						action.setText(currentText);
						index.setText("" + card_index + "/" + TOTAL_CARDS);
						str_tts = "That was a success! Now say flip to flip the card to the other side";
						tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
						return true;

					} else if (card_index == 2 || card_index == 10) {
						displayOtherWayCard();
					}

					return true;
				} else if (gesture == Gesture.TWO_TAP) {
					// do something on two finger tap
					return true;
				}

				return false;
			}
		});

		gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
			@Override
			public void onFingerCountChanged(int previousCount, int currentCount) {
				// do something on finger count changes
			}
		});
		gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
			@Override
			public boolean onScroll(float displacement, float delta,
					float velocity) {
				return false;
				// do something on scrolling
			}
		});
		return gestureDetector;
	}

	/**
	 * Send generic motion events to the gesture detector Required
	 */

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}

	/**
	 * Set up the tutorial's first card
	 */
	protected void setUpTutorial() {
		setContentView(R.layout.layout_tutorial);
		tts.stop();
		card_index++;
		header = (TextView) findViewById(R.id.header_tutorial);
		action = (TextView) findViewById(R.id.action_tutorial);
		status = (TextView) findViewById(R.id.status_tutorial);
		index = (TextView) findViewById(R.id.index_tutorial);
		status.setText(str_status);
		header.setText("Great, let's start!");
		currentText = "\"Next\"";
		action.setText(currentText);
		index.setText("" + card_index + "/" + TOTAL_CARDS);
		activityStarted = true;
		str_tts = "Great, let's start. Say next to go to the next card.";
		tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
	}

	/**
	 * Process the results from speech listener, if a valid command is found, perfom the appropriate action
	 * @param results
	 * 		The results
	 * @param size
	 * 		The number of results
	 */
	protected void processResults(ArrayList<String> results, int size) {
		String speech_text = "";
		for (int i = 0; i < size; i++) {
			speech_text += results.get(i);
		}
		String[] command = speech_text.split("\\s+");
		boolean command_match = false;
		int idx = 0;

		/**
		 * Search through the list of spoken terms and compare each element for
		 * a match. When a match is found, process the command and do NOT
		 * continue to compare for a match
		 */
		String theCommand = "";
		while (!command_match && idx < command.length) {
			theCommand = command[idx];
			Log.d(TAG_SPEECH, "commands= " + command[idx]);

			if (!activityStarted && findMatch(theCommand, start_dictionary)) {
				command_match = true;
				commandSet = true;
				setUpTutorial();
			}
			else if (activityStarted && card_index == 1
					&& findMatch(theCommand, next_dictionary)) {
				command_match = true;
				commandSet = true;
				tts.stop();
				card_index++;
				header.setText("Good,");
				currentText = "swipe forward";
				action.setText(currentText);
				index.setText("" + card_index + "/" + TOTAL_CARDS);
				str_tts = "You can also swipe forward to go to the next card, try to swipe now.";
				tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
			}
			else if (activityStarted && card_index == 3
					&& findMatch(theCommand, previous_dictionary)) {
				command_match = true;
				commandSet = true;
				tts.stop();
				card_index++;
				header.setText("Good,");
				currentText = "swipe back";
				action.setText(currentText);
				index.setText("" + card_index + "/" + TOTAL_CARDS);
				str_tts = "You can also swipe back to go to the previous card, try to swipe now";
				tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
			}
			else if (activityStarted && card_index == 5
					&& findMatch(theCommand, flip_dictionary)) {
				command_match = true;
				commandSet = true;
				tts.stop();
				card_index++;
				header.setText("Flipped!");
				currentText = "tap";
				action.setText(currentText);
				index.setText("" + card_index + "/" + TOTAL_CARDS);
				str_tts = "You flipped the card! You can also tap the side to flip a card, try to tap now";
				tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
			} else if (activityStarted && card_index == 7
					&& findMatch(theCommand, play_dictionary)) {
				command_match = true;
				commandSet = true;
				tts.stop();
				card_index++;

				mPlayer = MediaPlayer.create(this, R.raw.american_robin_song);
				mPlayer.start();
				header.setText("");
				action.setText("Playing song...");
				index.setText("" + card_index + "/" + TOTAL_CARDS);
				speechRecognizer.stopListening();

				mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						card_index++;
						header.setText("Say");
						currentText = "\"Robin\"";
						action.setText(currentText);
						index.setText("" + card_index + "/" + TOTAL_CARDS);
						str_tts = "Nice job! In the flashcards you can also say the name of the bird."
								+ "Let's practice, say Robin.";
						tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
						speechRecognizer.startListening(speechIntent);
					}
				});
			} else if (activityStarted && card_index == 9
					&& speech_text.toLowerCase(Locale.US).contains("robin")) {
				command_match = true;
				commandSet = true;
				tts.stop();
				card_index++;
				header.setText("You got it!");
				currentText = "\"Next\"";
				action.setText(currentText);
				index.setText("" + card_index + "/" + TOTAL_CARDS);
				str_tts = "You got it! Say next or swipe forward to go to the next card";
				tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
			}
			else if (activityStarted && card_index == 10
					&& findMatch(theCommand, next_dictionary)) {
				command_match = true;
				commandSet = true;
				goToLastCard();
			} else if (findMatch(theCommand, mainmenu_dictionary)) {
				command_match = true;
				commandSet = true;
				Intent intent = new Intent(ActivityTutorial.this,
						ActivityMenu.class);
				startActivity(intent);
				onDestroy();
			} else if (findMatch(theCommand, exit_dictionary)) {
				command_match = true;
				commandSet = true;
				stopService(new Intent(ActivityTutorial.this, MainService.class));
				setResult(RESULT_OK, null);
				onDestroy();
			}
			else
				idx++;
		}

		/*
		 * If there was a voice command that did not match the instruction, then
		 * ask to try again
		 */
		if (!command_match
				&& !theCommand.equals("")
				&& (card_index == 1 || card_index == 3 || card_index == 5
						|| card_index == 7 || card_index == 9 || card_index == 10)) {
			tts.stop();
			str_tts = "Try again";
			tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
			header.setText("Try again");
			action.setText(currentText);
		}

		if (command_match) {
			speechRecognizer.stopListening();
		}
	}
	
	/**
	 * Search the dictionary for a valid command
	 * @param data
	 * 		The spoken term
	 * @param dictionary
	 * 		The dictionary of possibilities for one command
	 * @return
	 * 		true if match, false otherwise
	 */
	protected boolean findMatch(String data, String[] dictionary) {
		for (int i = 0; i < dictionary.length; i++)
			if (data.equals(dictionary[i]))
				return true;
		return false;
	}

	protected void goToLastCard() {
		tts.stop();
		card_index++;
		setContentView(R.layout.layout_commands);
		str_tts = getString(R.string.tutorial_end);
		tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
	}

	protected void displayOtherWayCard() {
		header.setText("");
		action.setText("Oops, other way!");
		str_tts = "Oops, the other way";
		tts.speak(str_tts, TextToSpeech.QUEUE_FLUSH, null);
	}
	/**
	 * Display error card with message and close the app
	 * @param message the error message
	 */
	public void showError(String err) {
		Log.e(TAG_DEBUG, "ERROR: " + err);
		if (speechRecognizer != null) {
				speechRecognizer.stopListening();
				speechRecognizer.cancel();
				speechRecognizer.destroy(); // release SpeechRecognizer resources
				speechRecognizer = null; // set to null do it does not try this in onDestroy();
				}
		ErrorCard errorCard = new ErrorCard(this);
		errorCard.displayErrorCard(err);
	}
	@Override
	public void closeApp() {
		stopService(new Intent(ActivityTutorial.this,
				MainService.class));
		setResult(RESULT_OK, null);
		onDestroy();
		
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		try {
			if (speechRecognizer != null) {
				speechRecognizer.stopListening();
				speechRecognizer.cancel();
				speechRecognizer.destroy();

			}
			if (mPlayer != null) {
				if (mPlayer.isPlaying())
					mPlayer.stop();
			}
			finish();
		} catch (Exception e) {
			Log.d(TAG_DEBUG, "ERROR: " + e.getMessage());
		}

	}

	

}

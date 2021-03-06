package com.jgeorgiou.committomemory;

/**
 * This activity creates the flashcards with an image, text and media 
 * and handles the voice commands for navigating through the flashcards
 */

import java.util.ArrayList;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.*;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.*;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class ActivityFlashcards extends ActivityCommitToMemory {

	private FrameLayout frontOfCardView;
	private FrameLayout backOfCardView;
	private ViewSwitcher vs;
	private TextView textOfItem;
	private ImageView imageOfItem;
	private TextView statusFront;
	private TextView statusBack;
	private TextView indexFront;
	private TextView indexBack;
	private TextView scoreMsgFront;
	private TextView scoreMsgBack;
	
	private GestureDetector gestureDetector;
	private SpeechRecognizer speechRecognizer;
	private MediaPlayer mPlayer;
	private TextToSpeech tts;
	private Intent speechIntent;
	private FlashcardItem[] flashcardItem;

	private String[] next_dictionary = { "next", "text", "max", "fax" };
	private String[] previous_dictionary = { "previous", "prettiest" };
	private String[] flip_dictionary = { "flip", "flipcard", "flat", "foot",
			"slept", "slip", "flips", "flipped" };
	private String[] start_dictionary = { "start", "star" };
	private String[] mainmenu_dictionary = { "main", "menu" };
	private String[] exit_dictionary = { "exit" };
	private String[] play_dictionary = { "play" };
	private String[] repeat_dictionary = { "repeat", "what" };
	private String[] soundOnly_dictionary = { "sound", "only", "lonely",
			"soundly" };
	private String[] showImage_dictionary = { "show", "images" };

	private boolean activityStarted = false;
	private boolean commandSet = false;
	private boolean exitApp = false;
	private boolean mediaCardActive = false;

	private int cardIndex = 0;
	private int numOfCards = 0;
	private int totalCorrectImages = 0;
	private int totalCorrectMedia = 0;

	private String indexStr;
	private String status = "Listening";
	private String strTTS = "";
	private static final String TAG_SPEECH = "SPEECH_RESULTS";
	private static final String TAG_DEBUG = "FLASHCARD_DEBUG";
	
	private ModuleAllAboutBirds myModule = new ModuleAllAboutBirds();
	private Drawable audioOnlyImage; //image for the "sound only" mode
	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // required to keep screen on
		gestureDetector = createGestureDetector(this);
		displayIntroCard();
		initSpeechRecognizer();
		initTTS();
		setUpCards(myModule);
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
	public SpeechRecognizer getSpeechRecognizer() {
		return speechRecognizer;
	}
	public Intent getSpeechIntent() {
		return speechIntent;
	}
	/**
	 * Following are the methods for after the speech listener has made required callbacks
	 */
	public void setOnResults(Bundle results) {
		speechRecognizer.startListening(speechIntent);
		commandSet = false;
	}

	public void setOnPartialResults(Bundle partialResults) {
		if (!commandSet) {
			if (activityStarted) {
				status = "Processing";
				statusFront.setText(status);
				statusBack.setText(status);
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
	
	public void setReadyForSpeech() {
		if (activityStarted) {
			status = "Listening";
			statusFront.setText(status);
			statusBack.setText(status);
		}
	}

	public void setOnEndOfSpeech() {
		if (activityStarted) {
			status = "Processing";
			statusFront.setText(status);
			statusBack.setText(status);
		}
	}
	/**
	 * Create text-to-speech element
	 */
	protected void initTTS() {
		tts = new TextToSpeech(getApplicationContext(),
				new TextToSpeech.OnInitListener() {
					@Override
					public void onInit(int s) {
						if (s == TextToSpeech.SUCCESS) {
							tts.setSpeechRate((float) 0.75);
							tts.setLanguage(Locale.ENGLISH);
							// Following 2 lines removed. There is a delay and the user would speak before the tts
							// strTTS = getString(R.string.flashcard_intro); 
							// tts.speak(strTTS, TextToSpeech.QUEUE_FLUSH, null); 
						}
					}
				});
	}

	/**
	 * Set up "start" card
	 */
	protected void displayIntroCard() {
		setContentView(R.layout.layout_titlepage);
		TextView title_name = (TextView) findViewById(R.id.activity_name);
		title_name.setText(R.string.flashcards);
		TextView title_command = (TextView) findViewById(R.id.activity_command);
		title_command.setText(R.string.start);
	}

	/**
	 * Set flashcard items
	 */
	protected void setUpCards(ModuleCommitToMemory module) {
		String text;
		Drawable image;
		audioOnlyImage = getResources().getDrawable(module.getAudioImageResource());
		int mediaId;
		int defValue = 0;
		ArrayList<String> allText = getRecordsFromXML(this, module);// Get names from XML 
		if (numOfCards == 0)
			showError("The text file has no elements");
		else if (numOfCards == -1)
			showError("Cannot read the text file");

		flashcardItem = new FlashcardItem[numOfCards];
		// Get images and media from XML
		TypedArray allImages = getResources().obtainTypedArray(module.getImageResource()); 
		TypedArray allMedia = getResources().obtainTypedArray(module.getAudioResource());

		try {
			for (int i = 0; i < numOfCards; i++) {
				text = allText.get(i);
				image = allImages.getResources().getDrawable(
						allImages.getResourceId(i, defValue));
				mediaId = allMedia.getResourceId(i, defValue);
				flashcardItem[i] = new FlashcardItem(text, image, mediaId);
			}
		} catch (Exception e) {
			// check size of images and names arrays equal
			if (allImages.length() != numOfCards || allMedia.length() != numOfCards) { 
				showError("Lists length do not match");
			}
		}
		allImages.recycle();
		allMedia.recycle();
	}

	/**
	 * Set flashcard view elements
	 */
	protected void setFlashcardView() {
		vs = (ViewSwitcher) findViewById(R.id.viewswitcher);

		imageOfItem = (ImageView) findViewById(R.id.front_image);
		textOfItem = (TextView) findViewById(R.id.back_text);

		frontOfCardView = (FrameLayout) findViewById(R.id.front_of_card);
		frontOfCardView.setFocusable(true);
		frontOfCardView.setFocusableInTouchMode(true);

		backOfCardView = (FrameLayout) findViewById(R.id.back_of_card);
		backOfCardView.setFocusable(true);
		backOfCardView.setFocusableInTouchMode(true);

		statusFront = (TextView) findViewById(R.id.status_front);
		statusBack = (TextView) findViewById(R.id.status_back);

		indexFront = (TextView) findViewById(R.id.index_front);
		indexBack = (TextView) findViewById(R.id.index_back);

		scoreMsgFront = (TextView) findViewById(R.id.score_message_front);
		scoreMsgBack = (TextView) findViewById(R.id.score_message_back);

		setCard();
	}

	/**
	 * Set up the current bird card
	 */
	protected void setCard() {
		if (!mediaCardActive) {
			imageOfItem.setImageDrawable(flashcardItem[cardIndex].getImage());
		}
		textOfItem.setText(flashcardItem[cardIndex].getText());
		indexStr = "" + (cardIndex + 1) + "/" + numOfCards;
		indexFront.setText(indexStr);
		indexBack.setText(indexStr);

		if (cardIndex == numOfCards - 1) {
			scoreMsgFront.setVisibility(View.VISIBLE);
			scoreMsgBack.setVisibility(View.VISIBLE);
			if (mediaCardActive)
				scoreMsgFront.setTextColor(Color.WHITE);
		}
		mPlayer = MediaPlayer.create(this, flashcardItem[cardIndex].getAudio());
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.start();
	}

	/**
	 * Set the sound card to the front of the flashcard, replaces image
	 */
	protected void displaySoundCard() {
		mediaCardActive = true;
		imageOfItem.setImageDrawable(audioOnlyImage);
		indexFront.setTextColor(Color.WHITE);
		statusFront.setTextColor(Color.WHITE);
		if (backOfCardView.getVisibility() == View.VISIBLE)
			vs.showPrevious();
		playMedia();
	}

	/**
	 * Set the image to the front of the flashcard, replaces the sound card
	 */
	protected void displayImageCard() {
		mediaCardActive = false;
		imageOfItem.setImageDrawable(flashcardItem[cardIndex].getImage());
		indexFront.setTextColor(Color.BLACK);
		statusFront.setTextColor(Color.BLACK);
		if (backOfCardView.getVisibility() == View.VISIBLE)
			vs.showPrevious();
	}

	/**
	 * Read in the bird names from the XML file
	 * 
	 * @param Activity
	 * @return ArrayList of the item names
	 */
	protected ArrayList<String> getRecordsFromXML(Activity activity, ModuleCommitToMemory module) {
		numOfCards = 0;
		String name;
		ArrayList<String> allText = new ArrayList<String>();
		try {
			Resources res = activity.getResources();
			XmlResourceParser xrp = res.getXml(module.getTextResource());
			xrp.next();// skips descriptor line in XML file
			int eventType = xrp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if (xrp.getName().equals("text")) {
						eventType = xrp.next();
						if (eventType == XmlPullParser.TEXT) {
							name = xrp.getText();
							allText.add(name);
							numOfCards++;
						}
					}
				}
				if (eventType == XmlPullParser.END_TAG) {
				}
				eventType = xrp.next();
			}
			return allText;

		} catch (Exception e) {
			Log.e("xml_error", e.getMessage());
			numOfCards = -1;
			return null;
		}
	}

	/**
	 * Create the gesture detector
	 * 
	 * @param Context
	 * @return the gesture detector
	 */
	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					if (activityStarted)
						flipCard();
					else {
						setContentView(R.layout.layout_flashcard);
						activityStarted = true;
						setFlashcardView();
					}
					return true;
				} else if (gesture == Gesture.TWO_TAP) {
					// do something on two finger tap
					return true;
				} else if (activityStarted && gesture == Gesture.SWIPE_RIGHT) {
					goToNextCard();
					return true;
				} else if (activityStarted && gesture == Gesture.SWIPE_LEFT) {
					goToPreviousCard();
					return true;
				} else if (gesture == Gesture.SWIPE_DOWN) {
					onDestroy();
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

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) { // Required 
		if (gestureDetector != null) {
			return gestureDetector.onMotionEvent(event);
		}
		return false;
	}

	/**
	 * Process the results to find a valid command
	 * 
	 * @param results
	 * @param size
	 */
	protected void processResults(ArrayList<String> results, int size) {
		String speech_text = "";
		for (int i = 0; i < size; i++) {
			speech_text += results.get(i);
		}
		String[] command = speech_text.split("\\s+");
		boolean command_match = false;
		int idx = 0;

		/*
		 * Search through the list of spoken terms and compare each element for
		 * a match. When a match is found, process the command and do NOT
		 * continue to compare for a match
		 */
		while (!command_match && idx < command.length) {
			String theCommand = command[idx];
			Log.d(TAG_SPEECH, "commands= " + command[idx]);

			if (!activityStarted && findMatch(theCommand, start_dictionary)) {
				command_match = true;
				setContentView(R.layout.layout_flashcard);
				setFlashcardView();
				activityStarted = true;
			} else if (findMatch(theCommand, mainmenu_dictionary)) {
				command_match = true;
				pauseMediaAndStopTTS();
				onDestroy();
			} else if (findMatch(theCommand, exit_dictionary)) {
				command_match = true;
				// pauseMediaAndStopTTS(); //not needed, in onDestory()
				stopService(new Intent(ActivityFlashcards.this,
						MainService.class));
				exitApp = true;
				onDestroy();
			} else if (activityStarted) {
				if ((cardIndex < numOfCards)
						&& speech_text.toLowerCase(Locale.US).contains(
								flashcardItem[cardIndex].getText().toLowerCase(Locale.US))) {
					command_match = true;
					flipCardCorrectMatch();

					//*********WARNING: Hard coded required. Speech processing comes back "gray cat bird"
					//*********but the name is listed as "gray catbird"**********//
				} else if (cardIndex == 8
						&& speech_text.toLowerCase(Locale.US).contains(
								"gray cat bird")) {
					command_match = true;
					flipCardCorrectMatch();
					//*********End hard code**********//

				} else if (findMatch(theCommand, next_dictionary)) {
					command_match = true;
					goToNextCard();

				} else if (findMatch(theCommand, previous_dictionary)) {
					command_match = true;
					goToPreviousCard();
				}

				else if (findMatch(theCommand, flip_dictionary)) {
					command_match = true;
					flipCard();
					
				} else if (findMatch(theCommand, play_dictionary)) {
					command_match = true;
					if (!mPlayer.isPlaying()) {
						if (tts != null)
							if (tts.isSpeaking())
								tts.stop();
						playMedia();
					}
				}
				else if (findMatch(theCommand, repeat_dictionary)) {
					command_match = true;
					pauseMediaAndStopTTS();
					if (!(strTTS.length() == 0) && !strTTS.equals(null)) {
						Log.d(TAG_DEBUG, "strTTS = " + strTTS);
						repeat();
						Log.d(TAG_DEBUG, "repeat excecuted");
					}

					else {
						speakMessageToUser("Sorry, nothing to repeat");
						Log.d(TAG_DEBUG, "No text to repeat");
					}
				}
				else if (findMatch(theCommand, soundOnly_dictionary)) {
					command_match = true;
					if (!mediaCardActive) {
						pauseMediaAndStopTTS();
						Log.d(TAG_DEBUG, "Removing the image card...");
						displaySoundCard();
					}
				} else if (findMatch(theCommand, showImage_dictionary)) {
					command_match = true;
					if (mediaCardActive) {
						pauseMediaAndStopTTS();
						Log.d(TAG_DEBUG, "Displaying the image card...");
						displayImageCard();
					}
				} else if (speech_text.toLowerCase(Locale.US).contains(
						"start over")) {
					command_match = true;
					startOverFlashcards();
				}
			} // END IF activityStarted()

			if (!command_match)
				idx++;
		}
		if (command_match) {
			commandSet = true;
			speechRecognizer.stopListening();
		}
	}

	/**
	 * Compares data to dictionary values to find a match
	 * 
	 * @param data
	 * @param dictionary
	 * @return true if match found, else false
	 */
	protected boolean findMatch(String data, String[] dictionary) {
		for (int i = 0; i < dictionary.length; i++)
			if (data.equals(dictionary[i]))
				return true;
		return false;
	}

	/**
	 * Display the next flashcard item
	 */

	protected void goToNextCard() {
		if (cardIndex < numOfCards - 1) {
			pauseMediaAndStopTTS();
			mPlayer.reset();
			mPlayer.release();
			strTTS = "";
			cardIndex++;
			setCard();			
			if (backOfCardView.getVisibility() == View.VISIBLE)
				vs.showPrevious();
			Log.d(TAG_DEBUG, "Next command performed");
		} else if (cardIndex == numOfCards - 1) {
			cardIndex++;
			pauseMediaAndStopTTS();
			displayScoreCard();
		}
	}

	/**
	 * Display the previous flashcard item
	 */
	protected void goToPreviousCard() {
		if (cardIndex > 0) {
			pauseMediaAndStopTTS();
			mPlayer.reset();
			mPlayer.release();
			if (textOfItem.getTextSize() != 55)
				textOfItem.setTextSize(55);
			strTTS = "";
			if (scoreMsgFront.getVisibility() == View.VISIBLE) {
				scoreMsgFront.setVisibility(View.INVISIBLE);
				scoreMsgBack.setVisibility(View.INVISIBLE);
			}
			cardIndex--;
			setCard();
			if (backOfCardView.getVisibility() == View.VISIBLE)
				vs.showPrevious();
			Log.d(TAG_DEBUG, "Back command performed");
		}
	}

	/**
	 * Flip the flashcard to display the other side
	 */
	protected void flipCard() {
		if (cardIndex < numOfCards) {
			pauseMediaAndStopTTS();
			
			if (frontOfCardView.getVisibility() == View.VISIBLE) {
				vs.showNext();
				strTTS = flashcardItem[cardIndex].getText();
				tts.speak(strTTS, TextToSpeech.QUEUE_FLUSH, null);
				Log.d(TAG_DEBUG, "Flip command performed");
			} else if (backOfCardView.getVisibility() == View.VISIBLE) {
				vs.showPrevious();
				Log.d(TAG_DEBUG, "Flip command performed");
			}
		}
	}

	/**
	 * Flip the flashcard to display the back if the correct name was spoken
	 */
	protected void flipCardCorrectMatch() {
		if (cardIndex < numOfCards) {
			pauseMediaAndStopTTS();
			if (frontOfCardView.getVisibility() == View.VISIBLE) {
				if (mediaCardActive)
					totalCorrectMedia++;
				else
					totalCorrectImages++;
				
				String message = "";
				if (totalCorrectImages % 3 == 0)
					message = getString(R.string.flip_flashcard_message1);
				else if (totalCorrectImages % 2 == 0)
					message = getString(R.string.flip_flashcard_message2);
				else if (totalCorrectImages == numOfCards)
					message = getString(R.string.flip_flashcard_message3);
				else
					message = getString(R.string.flip_flashcard_message4);

				textOfItem.setText("" + message + "\n\n"
						+ flashcardItem[cardIndex].getText());
				textOfItem.setTextSize(45);
				vs.showNext();
				strTTS = message + "That is a" + flashcardItem[cardIndex].getText();
				tts.speak(strTTS, TextToSpeech.QUEUE_FLUSH, null);
				Log.d(TAG_DEBUG, "Flip correct match command performed");
			}
		}
	}

	/**
	 * Play the media
	 */
	protected void playMedia() {
		if (cardIndex < numOfCards) {
			if (mPlayer != null)
				mPlayer.start();
		}
	}

	/**
	 * Use text-to-speech to tell the user a message
	 * 
	 * @param the
	 *            message
	 */
	protected void speakMessageToUser(String message) {
		if (message != null) {
			tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	/**
	 * Repeat the text-to-speech
	 */
	protected void repeat() {
		tts.speak(strTTS, TextToSpeech.QUEUE_FLUSH, null);
	}

	/**
	 * Pause playing the media and stop text-to-speech
	 */
	protected void pauseMediaAndStopTTS() {
		if (mPlayer != null)
			if (mPlayer.isPlaying())
				mPlayer.pause();

			else if (tts != null)
				if (tts.isSpeaking())
					tts.stop();
	}

	/**
	 * Display the score on the card after the last flashcard item
	 */
	protected void displayScoreCard() {
		if (frontOfCardView.getVisibility() == View.VISIBLE)
			vs.showNext();
		String end_message = "";
		if (totalCorrectImages < 5)
			end_message = getString(R.string.end_flashcard_message1);
		else if (totalCorrectImages >= 5 && totalCorrectImages < 10)
			end_message = getString(R.string.end_flashcard_message2);
		else if (totalCorrectImages >= 10 && totalCorrectImages < 20)
			end_message =getString(R.string.end_flashcard_message3); 
		else if (totalCorrectImages >= 20)
			end_message =getString(R.string.end_flashcard_message4); 
		String score_information = end_message + "\nTotal Images Correct: "
				+ totalCorrectImages + "\nTotal Calls Correct: "
				+ totalCorrectMedia;
		textOfItem.setTextSize(30);
		textOfItem.setText(score_information
				+ "\n\"Start Over\"\t\"Main Menu\"\t\"Exit\"");
		scoreMsgFront.setVisibility(View.INVISIBLE);
		scoreMsgBack.setVisibility(View.INVISIBLE);
		indexBack.setText("End");
		strTTS = end_message;
		tts.speak(strTTS, TextToSpeech.QUEUE_FLUSH, null);
	}

	/**
	 * Start the flashcards from the beginning
	 */
	protected void startOverFlashcards() {
		Log.d(TAG_DEBUG, "Start over command performed");
		pauseMediaAndStopTTS();
		totalCorrectImages = 0;
		totalCorrectMedia = 0;
		strTTS = "";
		if (backOfCardView.getVisibility() == View.VISIBLE)
			vs.showPrevious();

		cardIndex = 0;
		if (textOfItem.getTextSize() != 55)
			textOfItem.setTextSize(55);
		setCard();
		scoreMsgFront.setVisibility(View.INVISIBLE);
		scoreMsgBack.setVisibility(View.INVISIBLE);
	}

	/**
	 * Display error card with message and close the app
	 * 
	 * @param message
	 *            the error message
	 */
	public void showError(String err) {
		Log.e(TAG_DEBUG, "ERROR: " + err);
		if (speechRecognizer != null) {
			speechRecognizer.stopListening();
			speechRecognizer.cancel();
			speechRecognizer.destroy(); // release SpeechRecognizer resources
			speechRecognizer = null; // set to null do it does not try this in
										// onDestroy();
		}
		ErrorCard errorCard = new ErrorCard(this);
		errorCard.displayErrorCard(err);
	}
	
	/**
	 * Exits the app
	 */
	public void closeApp() {
		exitApp = true;
		stopService(new Intent(ActivityFlashcards.this,
				MainService.class));
		onDestroy();
	}
	/**
	 * Stop the flashcard activity and go to main menu or exit
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (tts != null) {
			if (tts.isSpeaking())
				tts.stop();
			tts.shutdown(); // release tts resources
		}
		try {
			if (speechRecognizer != null) {
				speechRecognizer.stopListening();
				speechRecognizer.cancel();
				speechRecognizer.destroy(); // release SpeechRecognizer resources				
			}
			if (mPlayer != null) {
				if (mPlayer.isPlaying())
					mPlayer.stop();
				mPlayer.reset();
				mPlayer.release(); // release MediaPlayer resources
			}
			if (!exitApp) {
				Intent intent = new Intent(ActivityFlashcards.this,
						ActivityMenu.class); 
				startActivity(intent);
			}
			finish();
		} catch (Exception e) {
			Log.e(TAG_DEBUG, "ERROR: " + e.getMessage());
		}
	}
}

package com.jgeorgiou.committomemory;

/**
 * The menu activity allows for the Google Glass "ok glass" command to launch the menu, therefore the user can launch 
 * and select from the menu via voice commands. Additionally, the user can tap to launch the menu and swipe for menu options.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import java.lang.Runnable;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;

public class ActivityMenu extends Activity {
	private final Handler mHandler = new Handler();
	private final String TAG = "ActivityMenu";
	public final int REQUEST_EXIT = 0;
	private TextView instruction = null;
	private GestureDetector mGestureDetector;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mGestureDetector = createGestureDetector(this);
		setContentView(R.layout.layout_intro);
		instruction = (TextView) findViewById(R.id.menu_instructions_text);
	}

	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS
				|| featureId == Window.FEATURE_OPTIONS_PANEL) {
			getMenuInflater().inflate(R.menu.menu_activities, menu);
			return true;
		}
		// Pass through to super to setup touch menu.
		return super.onCreatePanelMenu(featureId, menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_activities, menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS
				|| featureId == Window.FEATURE_OPTIONS_PANEL) {
			Intent intent;
			switch (item.getItemId()) {
			case R.id.tutorial_item:
				intent = new Intent(ActivityMenu.this, ActivityTutorial.class);
				startActivityForResult(intent, REQUEST_EXIT);
				return true;

			case R.id.start_flashcards_item:
				instruction.setText(R.string.loading);
				intent = new Intent(ActivityMenu.this, ActivityFlashcards.class);
				startActivity(intent);
				finish();
				// following 2 lines remove so the flashcard activity does not
				// come back to a loading page. Functionality to come back to the menu is
				//implemented in the flashcard activity.
				// startActivityForResult(intent, REQUEST_EXIT);
				// instruction.setText(R.string.menu_instructions);
				return true;

			case R.id.exit_item:
				Log.d(TAG, "Exit menu item selected");
				stopService(new Intent(ActivityMenu.this, MainService.class));
				finish();
				return true;

			default:
				return super.onMenuItemSelected(featureId, item);
			}
		}
		return true;

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_EXIT) {
			if (resultCode == RESULT_OK) {
				stopService(new Intent(ActivityMenu.this, MainService.class));
				finish();
			}
		}
	}

	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					Log.d("TAG", "I Heard A TAP");
					openOptionsMenu();
					return true;
				} else if (gesture == Gesture.TWO_TAP) {
					// do something on two finger tap
					return true;
				} else if (gesture == Gesture.SWIPE_RIGHT) {
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
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

	/*
	 * Send generic motion events to the gesture detector. Required.
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}

	// Stop the service at the end of the message queue for proper options menu
	// animation. This is only needed when starting a new Activity or stopping a
	// Service that published a LiveCard.
	public void closeApplication() {
		post(new Runnable() {
			@Override
			public void run() {
				stopService(new Intent(ActivityMenu.this, MainService.class));
			}
		});
		finish();
	}

	/**
	 * Posts a {@link Runnable} at the end of the message loop
	 */
	public void post(Runnable runnable) {
		mHandler.post(runnable);
	}

}

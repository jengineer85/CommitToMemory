package com.jgeorgiou.committomemory;
 
/**
  *Creates an error card and displays it before exiting the application 
  */
import android.util.Log;
import android.widget.TextView;

public class ErrorCard {
	
	public ActivityCommitToMemory mainActivity;
	private static final String TAG_DEBUG  = "ERROR CARD";
	
	public ErrorCard(ActivityCommitToMemory myActivity) {
		mainActivity = myActivity;
	}
	
	public void displayErrorCard(String err) {
		Log.d(TAG_DEBUG, "Displaying error card...");
		mainActivity.setContentView(R.layout.layout_error);
		TextView title_name = (TextView) mainActivity.findViewById(R.id.error_message);
		title_name.setText("ERROR: " + err + ", closing app");
		createThread();
	}
	
	/**
	 * Displays the error for a specified time before closing the app
	 */
	private void createThread() {
		Log.d(TAG_DEBUG, "Creating thread...");
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(3500);	
					mainActivity.closeApp();					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		
	}
}

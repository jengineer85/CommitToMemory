package com.jgeorgiou.committomemory;

/**
 * This service hosts the live card that the application runs on
 */
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

public class MainService extends Service {
	private static final String LIVE_CARD_TAG = "CommitToMemory";
	private LiveCard mainLiveCard;
	private RemoteViews liveCardView;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mainLiveCard == null) {
			mainLiveCard = new LiveCard(this, LIVE_CARD_TAG);

			liveCardView = new RemoteViews(this.getPackageName(),
					R.layout.layout_launch);
			mainLiveCard.setViews(liveCardView);

			Intent menuIntent = new Intent(this, ActivityMenu.class);
			menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			mainLiveCard.setAction(PendingIntent.getActivity(this, 0,
					menuIntent, 0));

			mainLiveCard.attach(this);
			mainLiveCard.publish(PublishMode.REVEAL);
		} else {
			mainLiveCard.navigate();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (mainLiveCard != null && mainLiveCard.isPublished()) {
			mainLiveCard.unpublish();
			mainLiveCard = null;
		}
		super.onDestroy();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}

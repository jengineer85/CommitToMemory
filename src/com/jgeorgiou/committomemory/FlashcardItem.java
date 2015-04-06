package com.jgeorgiou.committomemory;

import android.graphics.drawable.Drawable;

public class FlashcardItem {
	private Drawable image;
	private String text;
	private int audioId;
	
	public FlashcardItem(String txt, Drawable img, int mId) {
		text = txt;
		image = img;		
		audioId = mId;
	}

	public Drawable getImage() {
		return image;
	}
	
	public String getText() {
		return text;
	}
	
	public int getAudio() {
		return audioId;
	}

}

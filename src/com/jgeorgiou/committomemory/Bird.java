package com.jgeorgiou.committomemory;

import android.graphics.drawable.Drawable;

/**
 * A bird has a name, image and song
 *
 */

public class Bird {
	private Drawable birdImage;
	private String birdName;
	private int birdSongID;
	
	public Bird(String name, Drawable image, int song) {
		birdImage = image;
		birdName = name;
		birdSongID = song;
	}

	public Drawable getImage() {
		return birdImage;
	}
	
	public String getName() {
		return birdName;
	}
	
	public int getCall() {
		return birdSongID;
	}
}

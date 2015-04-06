package com.jgeorgiou.committomemory;

public abstract class ModuleCommitToMemory {
	public int resourceText;
	public int resourceImage;
	public int resourceAudio;
	public int resourceAudioImage;
	
	public abstract int getImageResource();
	public abstract int getTextResource();
	public abstract int getAudioResource();
	public abstract int getAudioImageResource();

}

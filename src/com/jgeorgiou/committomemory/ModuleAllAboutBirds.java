package com.jgeorgiou.committomemory;

public class ModuleAllAboutBirds extends ModuleCommitToMemory{
	
	public int resourceText = R.xml.bird_names;
	public int resourceImage = R.array.bird_imgs;
	public int resourceAudio = R.array.bird_sounds;
	public int resourceAudioImage = R.drawable.bird_singing;
	
	public ModuleAllAboutBirds() {		
	}

	@Override
	public int getImageResource() {
		return resourceImage;
	}
	@Override
	public int getTextResource() {
		return resourceText;
	}
	@Override
	public int getAudioResource() {
		return resourceAudio;
	}
	public int getAudioImageResource() {
		return resourceAudioImage;
	}
}

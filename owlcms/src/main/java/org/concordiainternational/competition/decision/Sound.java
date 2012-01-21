package org.concordiainternational.competition.decision;

import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Mixer;

public class Sound {
	Mixer mixer;
	private InputStream resource;

	
	public Sound(Mixer mixer, String soundRelativeURL) throws IllegalArgumentException {
		this.mixer = mixer;
		this.resource = Sound.class.getResourceAsStream(soundRelativeURL);
	}

	
	public void emit() {
        try {
        	if (mixer == null) return;
			// must use wav format
        	AudioInputStream inputStream = AudioSystem.getAudioInputStream(resource);
        	Clip clip = AudioSystem.getClip(mixer.getMixerInfo());
        	clip.open(inputStream);
        	clip.start(); 			    
          } catch (Exception e) {
        	  throw new RuntimeException(e);
          }
	}

}
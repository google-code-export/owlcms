package org.concordiainternational.competition.decision;

import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Mixer;

public class Sound {
	Mixer mixer;
	private InputStream resource;

	public Sound(String soundRelativeURL) throws IllegalArgumentException {
		this(AudioSystem.getMixer(null), soundRelativeURL);
	}
	
	Sound(Mixer mixer, String soundRelativeURL) throws IllegalArgumentException {
		this.mixer = mixer;
		this.resource = Sound.class.getResourceAsStream(soundRelativeURL);
	}

	
	public void emit() {
        try {
			// must use wav format; did not find easy way to get mp3spi to work.
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(resource);
			
	        try {
	            Clip clip = AudioSystem.getClip(mixer.getMixerInfo());
	            clip.open(inputStream);
	            clip.start(); 
	          } catch (Exception e) {
	            System.err.println(e.getMessage());
	          }
			    
          } catch (Exception e) {
        	  throw new RuntimeException(e);
          }
	}

}
package org.concordiainternational.competition.decision;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;

public class Speakers {
	public static void main(String[] args) throws Exception {
		List<Mixer> mixers = getOutputs();
		for (Mixer mixer: mixers) {
			System.out.println(mixer.getMixerInfo().getName());
			new Speakers().testSound(mixer);
		}
	}

	/**
	 * @return
	 */
	public static List<Mixer> getOutputs() {
		List<Mixer> mixers = outputs(AudioSystem.getMixer(null), AudioSystem.getMixerInfo());
		return mixers;
	}

	/**
	 * @param defaultMixer
	 * @param infos
	 */
	protected static List<Mixer> outputs(Mixer defaultMixer, Mixer.Info[] infos) {
		List<Mixer> mixers = new ArrayList<Mixer>();
		for (Mixer.Info info : infos) {
			Mixer mixer = AudioSystem.getMixer(info);

			try {
				if (! mixer.getMixerInfo().toString().startsWith("Java")) {
					AudioFormat af = new AudioFormat(8000f,8,1,true,false);
					if (AudioSystem.getSourceDataLine(af,info) != null) {
						mixers.add(mixer);
					}					
				}
			} catch (IllegalArgumentException e) {
			} catch (LineUnavailableException e) {
			} 
		}
		return mixers;
	}

	/**
	 * @param mixer
	 */
	public synchronized void testSound(Mixer mixer) {
		try {
			if (mixer == null) return;
			// both sounds should be heard simultaneously
			new Sound(mixer,"/sounds/initialWarning2.wav").emit();
			new Tone(mixer, 1100, 1200, 1.0).emit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	    
	}
	

	/**
	 * @param infos
	 * @throws LineUnavailableException
	 */
	protected static void speakers(Mixer.Info[] infos)
			throws LineUnavailableException {
		for (Mixer.Info info : infos) {
			Mixer mixer = AudioSystem.getMixer(info);
			if (mixer.isLineSupported(Port.Info.SPEAKER)) {
				Port port = (Port) mixer.getLine(Port.Info.SPEAKER);
				port.open();
				if (port.isControlSupported(FloatControl.Type.VOLUME)) {
					FloatControl volume = (FloatControl) port.getControl(FloatControl.Type.VOLUME);
					System.out.println(info);
					System.out.println("- " + Port.Info.SPEAKER);
					System.out.println("  - " + volume);
				}
				port.close();
			}
		}
	}

	public static List<String> getOutputNames() {
		ArrayList<String> outputNames = new ArrayList<String>();
		for (Mixer mixer: getOutputs()){
			outputNames.add(mixer.getMixerInfo().getName());
		};
		return outputNames;
	}
}
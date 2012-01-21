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
				if (mixer != defaultMixer && ! mixer.getMixerInfo().toString().startsWith("Java")) {
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
	public static void testSound(Mixer mixer) {
		try {
			// must use wav format; did not find easy way to get mp3spi to work.
//			final InputStream resource = Speakers.class
//			.getResourceAsStream("/sounds/initialWarning.wav");
//			AudioInputStream inputStream = AudioSystem
//			.getAudioInputStream(resource);
//			Clip clip = AudioSystem.getClip(mixer.getMixerInfo());
//			clip.open(inputStream);
			System.err.println(mixer.getMixerInfo().getName());
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
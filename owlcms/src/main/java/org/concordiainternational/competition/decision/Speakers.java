package org.concordiainternational.competition.decision;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;

public class Speakers {
	public static void main(String[] args) throws Exception {
		Mixer defaultMixer = AudioSystem.getMixer(null);
		System.out.println("default mixer: " + defaultMixer.getMixerInfo().toString());

		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		//speakers(infos);
		outputs(defaultMixer, infos);
	}

	/**
	 * @param defaultMixer
	 * @param infos
	 */
	protected static void outputs(Mixer defaultMixer, Mixer.Info[] infos) {
		for (Mixer.Info info : infos) {
			Mixer mixer = AudioSystem.getMixer(info);

			try {
				if (mixer != defaultMixer && ! mixer.getMixerInfo().toString().startsWith("Java")) {
					AudioFormat af = new AudioFormat(8000f,8,1,true,false);
					if (AudioSystem.getSourceDataLine(af,info) != null) {
						//new Tone(mixer, 1100, 1200, 1.0).emit();
						System.out.println("**** ok: " + info.toString());
					}					
				}
			} catch (IllegalArgumentException e) {
				System.out.println("not ok: " + info.toString()+" "+e);
			} catch (LineUnavailableException e) {
				System.out.println("not ok: " + info.toString()+" "+e);
			}
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
}
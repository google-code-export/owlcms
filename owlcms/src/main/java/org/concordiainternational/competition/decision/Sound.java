/*
 * Copyright 2009-2012, Jean-François Lamy
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.concordiainternational.competition.decision;

import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;

/**
 * Play a sampled sound. Requires an uncompressed format (WAV), not a compressed (MP3) format.
 * 
 * @author jflamy
 */
public class Sound {
    Mixer mixer;
    private InputStream resource;

    public Sound(Mixer mixer, String soundRelativeURL) throws IllegalArgumentException {
        this.mixer = mixer;
        this.resource = Sound.class.getResourceAsStream(soundRelativeURL);
    }

    public synchronized void emit() {
        try {
            if (mixer == null)
                return;

            final AudioInputStream inputStream = AudioSystem.getAudioInputStream(resource);
            final Clip clip = AudioSystem.getClip(mixer.getMixerInfo());
            clip.open(inputStream);

            // clip.start() creates a native thread 'behind the scenes'
            // unless this is added, it never goes away
            // ref: http://stackoverflow.com/questions/837974/determine-when-to-close-a-sound-playing-thread-in-java
            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent evt) {
                    if (evt.getType() == LineEvent.Type.STOP) {
                        evt.getLine().close();
                    }
                }
            });
            clip.start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

package org.concordiainternational.competition.decision;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
  
public class Speakers  
{  
    public static void main(String[] args) throws Exception  
    {  
        Mixer.Info[] infos = AudioSystem.getMixerInfo();  
        for (Mixer.Info info: infos)  
        {  
            Mixer mixer = AudioSystem.getMixer(info);  
            if (mixer.isLineSupported(Port.Info.SPEAKER))  
            {  
                Port port = (Port)mixer.getLine(Port.Info.SPEAKER);  
                port.open();  
                if (port.isControlSupported(FloatControl.Type.VOLUME))  
                {  
                    FloatControl volume = (FloatControl)port.getControl(FloatControl.Type.VOLUME);  
                    System.out.println(info);  
                    System.out.println("- " + Port.Info.SPEAKER);  
                    System.out.println("  - " + volume);  
                }  
                port.close();  
            }  
        }  
    }  
} 
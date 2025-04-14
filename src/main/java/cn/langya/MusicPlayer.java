package cn.langya;

import de.maxhenkel.lame4j.DecodedAudio;
import de.maxhenkel.lame4j.Mp3Decoder;
import de.maxhenkel.lame4j.UnknownPlatformException;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

/**
 * @author LangYa466
 * @since 4/12/2025 3:13 PM
 */
public class MusicPlayer {

    // Play MP3 file
    public static void playMp3(String filePath) throws IOException, LineUnavailableException, UnknownPlatformException {
        // Decode the MP3 file
        DecodedAudio decodedAudio = Mp3Decoder.decode(Files.newInputStream(Paths.get(filePath)));
        short[] decode = decodedAudio.getSamples();

        /*
        System.out.println("Sample Rate: " + decodedAudio.getSampleRate());
        System.out.println("Bit Rate: " + decodedAudio.getBitRate());
        System.out.println("Channels: " + decodedAudio.getChannelCount());
        System.out.println("Frame Size: " + decodedAudio.getSampleSizeInBits());
        System.out.println("Length: " + decode.length + " samples");
        System.out.println("Duration: " + ((float) decode.length / (float) decodedAudio.getSampleRate()) + " seconds");
         */

        // Play the decoded audio
        playDecodedAudio(decodedAudio.getSampleRate(), decodedAudio.getChannelCount(), decode);
    }

    // Play WAV file
    // funny code
    public static void playWav(String filePath) {
        File file = new File(filePath);
        AudioInputStream audioInputStream = null;
        Clip clip = null;

        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioInputStream);

            Clip finalClip = clip;
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    finalClip.close();
                }
            });

            clip.start();

            synchronized (clip) {
                try {
                    clip.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        } finally {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
            if (audioInputStream != null) {
                try {
                    audioInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Helper method to play decoded MP3 audio
    private static void playDecodedAudio(int sampleRate, int channelCount, short[] decode) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(sampleRate, 16, channelCount, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        byte[] byteData = new byte[decode.length * 2];
        for (int i = 0; i < decode.length; i++) {
            byteData[i * 2] = (byte) (decode[i] & 0xFF);
            byteData[i * 2 + 1] = (byte) ((decode[i] >> 8) & 0xFF);
        }

        line.write(byteData, 0, byteData.length);
        line.drain();
        line.close();
    }

    public static FlacPlayer playFlac(String filePath) {
        return new FlacPlayer(filePath).play();
    }
}

package cn.langya;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author LangYa466
 * @since 4/14/2025 2:12 PM
 */
public class FlacPlayer {

    private final String filePath;
    private volatile boolean playing = false;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    private AudioFormat format;
    private long frameSize;
    private AudioInputStream audioInputStream;
    private SourceDataLine line;
    private Thread playThread;

    /**
     * 构造函数 : 初始化流和音频输出行
     */
    public FlacPlayer(String filePath) {
        this.filePath = filePath;
        try {
            initStream();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * (重新)打开 AudioInputStream 和 SourceDataLine
     */
    private void initStream() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        // 打开 FLAC 文件
        AudioInputStream sourceStream = AudioSystem.getAudioInputStream(file);
        AudioFormat sourceFormat = sourceStream.getFormat();

        // 定义目标 PCM 格式
        AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(),
                16, // sampleSizeInBits
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * 2, // frameSize = channels * 2 bytes
                sourceFormat.getSampleRate(),
                false // little endian
        );

        // 转换成 PCM
        audioInputStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
        format = audioInputStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
    }

    /**
     * 开始播放
     */
    public FlacPlayer play() {
        if (playing) return this;
        playing = true;
        playThread = new Thread(() -> {
            line.start();
            byte[] buffer = new byte[4096];
            try {
                int bytesRead;
                while (playing && (bytesRead = audioInputStream.read(buffer)) != -1) {
                    // 如果 paused 则等待
                    synchronized (pauseLock) {
                        while (paused) {
                            line.flush();
                            pauseLock.wait();
                        }
                    }
                    line.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }, "FlacPlayer-Thread");
        playThread.start();

        return this;
    }

    /**
     * 暂停播放
     */
    public void pause() {
        paused = true;
    }

    /**
     * 恢复播放
     */
    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    /**
     * 跳转到指定位置 (微秒)
     *
     * @param microseconds 目标位置 单位微秒
     */
    public void seek(long microseconds)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
        // 先停止当前播放 并等待线程结束
        stop();
        // 重新打开流
        initStream();
        // 计算要跳过的字节数 :  frameRate(帧/秒) * (microseconds/1_000_000) * frameSize
        long bytesToSkip = (long) (format.getFrameRate() * (microseconds / 1_000_000.0)) * frameSize;
        long skipped = 0;
        while (skipped < bytesToSkip) {
            long n = audioInputStream.skip(bytesToSkip - skipped);
            if (n <= 0) break;
            skipped += n;
        }
        // 跳转后开始播放
        play();
    }

    /**
     * 停止播放
     */
    public void stop() throws InterruptedException {
        playing = false;
        // 如果在 paused 状态 需要先唤醒以让线程退出
        resume();
        if (playThread != null) {
            playThread.join();
        }
    }

    /**
     * 播放结束或停止时清理资源
     */
    private void cleanup() {
        line.drain();
        line.stop();
        line.close();
        try {
            audioInputStream.close();
        } catch (IOException ignored) {}
    }

    // 静态调用示例
    public static void playFlac(String filePath) {
        FlacPlayer player = new FlacPlayer(filePath);
        player.play();
        // test
        // player.pause();
        // player.resume();
        // player.seek(10_000_000); // 跳到 10 秒
        // player.stop();
    }
}

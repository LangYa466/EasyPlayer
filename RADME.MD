# MusicPlayer

一个简单轻量的音乐播放库 用于播放 MP3 和 WAV 音频文件
使用lame解码器

## 示例用法

```java
import cn.langya.MusicPlayer;
import java.util.Scanner;

public class TestMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入音频文件路径 P3 或 WAV :");
        String filePath = scanner.nextLine();

        try {
            if (filePath.endsWith(".mp3")) {
                MusicPlayer.playMp3(filePath);
            } else if (filePath.endsWith(".wav")) {
                MusicPlayer.playWav(filePath);
            } else {
                System.out.println("不支持的音频格式");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

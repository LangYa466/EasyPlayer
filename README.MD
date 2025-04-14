# MusicPlayer

一个简单轻量的音乐播放库 用于播放 MP3 和 WAV 和 FLAC 音频文件
使用lame解码器(mp3)

gralde/maven加载:
https://jitpack.io/#LangYa466/EasyPlayer/-SNAPSHOT

## 示例用法

```java
public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入音频文件路径 MP3 或 WAV 或 FLAC");
        String filePath = scanner.nextLine();

        try {
            if (filePath.endsWith(".mp3")) {
                MusicPlayer.playMp3(filePath);
            } else if (filePath.endsWith(".wav")) {
                MusicPlayer.playWav(filePath);
            } else if (filePath.endsWith(".flac")) {
                MusicPlayer.playFlac(filePath);
            } else {
                System.out.println("不支持的音频格式");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
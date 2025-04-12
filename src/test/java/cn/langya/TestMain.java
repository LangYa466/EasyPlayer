package cn.langya;

import java.util.Scanner;

/**
 * @author LangYa466
 * @since 4/12/2025 3:13 PM
 */
public class TestMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入音频文件路径 MP3 或 WAV");
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
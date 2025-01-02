package top.weixiansen574.bilibiliArchive.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class FFmpegUtil {

    public static void merge(String audioFilePath, String videoFilePath, String outputFilePath) throws IOException {
        String[] cmd = {
                "ffmpeg",
                "-i",
                audioFilePath,
                "-i",
                videoFilePath,
                "-acodec",
                "copy",
                "-vcodec",
                "copy",
                outputFilePath,
                "-y"
        };
        //String cmd = "ffmpeg -i \""+audioFilePath+"\" -i \""+videoFilePath+"\" -acodec copy -vcodec copy \""+outputFilePath+"\"";
        File outputFile = new File(outputFilePath);
        if (outputFile.exists()) {
            System.out.println("为保证正常ffmpeg能再次合并视频，已删除视频文件：" + outputFilePath);
        }
        //System.out.println(Arrays.toString(cmd));
        executeCmd(cmd);
    }

    public static void executeCmd(String[] cmd) throws IOException {
        //String cmdBin = "cmd /c ";
        //Process process = Runtime.getRuntime().exec(cmd);
        Process process = new ProcessBuilder(cmd).start();
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        //BufferedReader errorReader = process.errorReader();
        StringBuilder errorSB = new StringBuilder();
        String l;
        while ((l = errorReader.readLine()) != null) {
            errorSB.append(l).append("\n");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder inputSB = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            inputSB.append(line).append("\n");;
        }
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        if (exitCode != 0) {
            throw new IOException("视频合并失败，命令："+ Arrays.toString(cmd) +"，ffmpeg exitCode:" +
                    exitCode + "\n输出信息:" + inputSB + "\nerror输出信息:" + errorSB);
        }
    }

    public static boolean checkFFmpegInstalled() {
        // 要执行的命令
        String command = "ffmpeg -version";

        try {
            // 执行系统命令
            Process process = Runtime.getRuntime().exec(command);

            // 读取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 等待命令执行完毕
            int exitCode = process.waitFor();

            // 输出命令执行结果
            //System.out.println("Command Output:\n" + output);

            // 检查退出代码
            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false; // 如果发生异常，认为FFmpeg未安装
        }
    }


}

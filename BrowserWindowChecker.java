package com.yuanshenqidong;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import java.util.Locale;

/**
 * 用于检测是否有特定标题的浏览器窗口打开。
 */
public class BrowserWindowChecker {

    /**
     * 检查是否有浏览器窗口打开了指定的标题。
     *
     * @param targetTitle 要检测的窗口标题的一部分（不区分大小写）。
     * @return 如果有窗口打开了该网页，返回true，否则返回false。
     */
    public static boolean isWebPageOpen(String targetTitle) {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return isWebPageOpenWindows(targetTitle);
        } else if (os.contains("mac")) {
            return isWebPageOpenMac(targetTitle);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return isWebPageOpenLinux(targetTitle);
        }
        return false;
    }

    private static boolean isWebPageOpenWindows(String targetTitle) {
        final boolean[] found = {false};
        final String target = targetTitle.toLowerCase(Locale.ROOT);

        User32.INSTANCE.EnumWindows((hWnd, arg) -> {
            // 获取窗口标题
            char[] windowText = new char[512];
            User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
            String wText = Native.toString(windowText).toLowerCase(Locale.ROOT);

            // 打印每个窗口的标题（用于调试）
            System.out.println("检测标题: " + wText);

            if (wText.contains(target)) {
                found[0] = true;
                return false;
            }
            return true;
        }, null);

        return found[0];
    }

    private static boolean isWebPageOpenMac(String targetTitle) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "osascript", "-e",
                    "tell application \"System Events\" to get the name of every window of (every process whose visible is true and name contains \"Google Chrome\" or name contains \"Safari\" or name contains \"Firefox\")"
            });
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase(Locale.ROOT).contains(targetTitle.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isWebPageOpenLinux(String targetTitle) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", "wmctrl -l"});
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase(Locale.ROOT).contains(targetTitle.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

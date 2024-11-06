package com.yuanshenqidong;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import java.util.Locale;

public class BrowserWindowChecker {

    /**
     * 检查是否有浏览器窗口打开了指定的URL。
     *
     * @param targetTitle 要检测的窗口标题的一部分（不区分大小写）。
     * @return 如果有窗口打开了该网页，返回true，否则返回false。
     */
    public static boolean isWebPageOpen(String targetTitle) {
        final boolean[] found = {false};
        final String target = targetTitle.toLowerCase(Locale.ROOT);

        User32.INSTANCE.EnumWindows((hWnd, arg) -> {
            // 获取窗口标题
            char[] windowText = new char[512];
            User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
            String wText = Native.toString(windowText).toLowerCase(Locale.ROOT);
            System.out.println(wText);

            // 检查窗口标题是否包含目标字符串
            if (wText.contains(target)) {
                found[0] = true;
                return false;
            }
            return true;
        }, null);

        return found[0];
    }
}

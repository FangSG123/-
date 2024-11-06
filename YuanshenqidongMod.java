package com.yuanshenqidong;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 主 MOD 类
 */
@Mod("yuanshenqidong")
public class YuanshenqidongMod {

    private ScheduledExecutorService scheduler;
    private boolean isTimerRunning = false;

    public YuanshenqidongMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 监听玩家死亡事件
     *
     * @param event 生命死亡事件
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            // 启动五分钟的计时器
            startDeathTimer();
            openWebPages();
        }
    }

    /**
     *
     * 如果在五分钟内检测不到窗口，则打开指定的网页。
     */
    private void startDeathTimer() {
        if (isTimerRunning) {
            System.out.println("计时器已在运行中，跳过启动。");
            return;
        }
        isTimerRunning = true;

        scheduler = Executors.newScheduledThreadPool(1);

        final int totalDurationSeconds = 300; // 五分钟 = 300秒
        final int intervalSeconds = 30; // 每10秒检测一次
        final int totalChecks = totalDurationSeconds / intervalSeconds; // 300 / 10 = 30

        // Runnable task to check webpage
        Runnable checkTask = new Runnable() {
            private int count = 0;

            @Override
            public void run() {
                count++;
                boolean isOpen = BrowserWindowChecker.isWebPageOpen("原神");
                if (!isOpen) {
                    System.out.println("原神未启动，启原中...");
                    openWebPages();
                } else {
                    System.out.println("HappyGaming");
                }

                if (count >= totalChecks) {
                    // 时间到，关闭调度器
                    scheduler.shutdown();
                    isTimerRunning = false;
                    System.out.println("欢乐的时光总是短暂的。");
                }
            }
        };

        // Schedule the task at fixed rate
        scheduler.scheduleAtFixedRate(checkTask, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * 打开指定的网页。
     */
    private void openWebPages() {
        try {
            String[] urls = {
                    "https://ys.mihoyo.com/cloud/#/",
                    "https://autopatchcn.yuanshen.com/client_app/download/launcher/20240927174612_nuShjqNATtQ0nJQd/pcweb/yuanshen_setup_202409241950.exe"
            };

            for (String url : urls) {
                openWebPage(url);
            }

            // 向玩家发送消息通知
            notifyPlayer("原神?启动！");
        } catch (Exception e) {
            e.printStackTrace();
            notifyPlayer("原神?启动失败QAQ");
        }
    }

    /**
     * 根据操作系统打开指定的网页或下载链接。
     *
     * @param url 要打开的URL或下载链接。
     */
    private void openWebPage(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Runtime rt = Runtime.getRuntime();

            if (os.contains("win")) {
                // Windows
                rt.exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            } else if (os.contains("mac")) {
                // macOS
                rt.exec(new String[]{"open", url});
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                // Linux/Unix
                rt.exec(new String[]{"xdg-open", url});
            } else {
                System.out.println("不支持的操作系统，无法启原: " + url);
                return;
            }

            System.out.println("启原成功: " + url);
        } catch (Exception e) {
            System.out.println("启原失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param message 要发送的消息
     */
    private void notifyPlayer(String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.screen == null) {
            minecraft.execute(() -> {
                minecraft.player.sendSystemMessage(Component.literal(message));
            });
        }
    }
}

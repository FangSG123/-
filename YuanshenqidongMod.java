package com.yuanshenqidong;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;

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
    private volatile boolean featureEnabled = false; // 控制功能启用与否

    public YuanshenqidongMod() {
        // 注册事件总线
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
            if (featureEnabled) {
                startDeathTimer();
            }
        }
    }

    /**
     * 注册自定义指令
     *
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> toggleCommand = Commands.literal("yuanshenqidong")
                .requires(source -> source.hasPermission(2)) // 仅 OP 可以执行
                .executes(context -> toggleFeature(context.getSource()));

        dispatcher.register(toggleCommand);
    }

    /**
     * 切换功能启用与否
     *
     * @param source 命令源
     * @return 命令执行状态
     */
    private int toggleFeature(CommandSourceStack source) {
        featureEnabled = !featureEnabled;
        source.sendSuccess(() -> Component.literal("原神启动器" + (featureEnabled ? "已开启" : "已关闭") + "。"), true);
        System.out.println("启动器" + (featureEnabled ? "启用" : "禁用") + "。");
        return 1;
    }

    /**
     * 如果在五分钟内检测不到窗口，则打开指定的网页。
     */
    private void startDeathTimer() {
        if (isTimerRunning) {
            System.out.println("计时器已在运行中。");
            return;
        }
        isTimerRunning = true;

        scheduler = Executors.newScheduledThreadPool(1);

        final int totalDurationSeconds = 120;
        final int intervalSeconds = 30;
        final int totalChecks = totalDurationSeconds / intervalSeconds;
        Runnable checkTask = new Runnable() {
            private int count = 0;

            @Override
            public void run() {
                count++;
                boolean isOpen = BrowserWindowChecker.isWebPageOpen("原神");
                if (!isOpen) {
                    System.out.println("你怎敢关闭原神...");
                    openWebPages();
                } else {
                    System.out.println("仍然启原中");
                }

                if (count >= totalChecks) {
                    // 时间到，关闭调度器
                    scheduler.shutdown();
                    isTimerRunning = false;
                    System.out.println("计时器已关闭。");
                }
            }
        };

        // Schedule the task at fixed rate
        scheduler.scheduleAtFixedRate(checkTask, 0, intervalSeconds, TimeUnit.SECONDS);
        System.out.println("计时器启动。");
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
            notifyPlayer("有人开玩了");
        } catch (Exception e) {
            e.printStackTrace();
            notifyPlayer("fuxx，有人无法启动Genshin");
        }
    }

    /**
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

            System.out.println("启原成功!: " + url);
        } catch (Exception e) {
            System.out.println("启原失败QAQ " + e.getMessage());
            e.printStackTrace();
        }
    }

    //向玩家发送消息通知
    private void notifyPlayer(String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.screen == null) {
            minecraft.execute(() -> {
                minecraft.player.sendSystemMessage(Component.literal(message));
            });
        }
    }
}

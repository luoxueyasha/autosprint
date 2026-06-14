package com.roseyasa.autosprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

import static com.roseyasa.autosprint.AutoSprint.MODID;
//import static com.roseyasa.autosprint.Config.AUTO_JUMP;
import static com.roseyasa.autosprint.Config.AUTO_SPRINT;
import static com.roseyasa.autosprint.Config.CUSTOM_MESSAGE;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class EventHandler {
    public static final KeyMapping.Category MOD_KEY_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(MODID, "category"));
    public static final String MOD_KEY_ID = "key." + MODID + ".auto_run";

    public static final int CUSTOM_MESSAGE_ON_NUM = 5;
    public static final int CUSTOM_MESSAGE_OFF_NUM = 5;

    private static boolean isAutoSprinting = false;
    private static boolean autoSprintRunState = false;

    private static boolean wasW = false;
    private static boolean wasS = false;
    private static boolean wasCtrl = false;

    public static final Lazy<KeyMapping> AUTO_SPRINT_KEY = Lazy.of(() -> new KeyMapping(
        MOD_KEY_ID,
        KeyConflictContext.IN_GAME,
        InputConstants.Type.MOUSE,
        GLFW.GLFW_MOUSE_BUTTON_5,
        MOD_KEY_CATEGORY
    ));

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(AUTO_SPRINT_KEY.get());
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        AutoSprint.LOGGER.info("Now Playing: SPYAIR - [RAGE OF DUST]");
    }

    public static void setPlayerSprinting(Player player, boolean state){
        if(player == null){
            return;
        }
        Level level = player.level();
        if(!level.isClientSide()){
            return;
        }
        if(!AUTO_SPRINT.get()){
            state = false;
        }
        player.setSprinting(state);


    }

    public static void showCustomMessage(Player player, boolean state){
        if(!CUSTOM_MESSAGE.get()) {
            return;
        }
        Level level = player.level();
        int t = level.getRandom().nextInt(1,10000);
        String key = "autosprint.custom_message.";
        // @debug, 后期改成数据驱动
        if(state){
            t %= CUSTOM_MESSAGE_ON_NUM;
            key += "on.";
        } else{
            t %= CUSTOM_MESSAGE_OFF_NUM;
            key += "off.";
        }
        t++;
        key+=t;
        player.sendSystemMessage(Component.translatable(key));
    }

//    public static void setPlayerAutojump(LocalPlayer player, boolean state) {
//        if (player == null) {
//            return;
//        }
//        if(!player.level().isClientSide()){
//            return;
//        }
//
//        Options options = Minecraft.getInstance().options;
//        // 如果用户已在控制选项中手动启用了自动跳跃，则不修改
//        if (options.autoJump().get()) {
//            return;
//        }
//
//        if(!AUTO_JUMP.get()){
//            state = false;
//        }
//
//        // @debug,todo: mixin Localplayer，使得能控制玩家自动跳跃而不更改界面选项
//
//    }


    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        KeyMapping keyForward = mc.options.keyUp;
        KeyMapping keyBack = mc.options.keyDown;
        KeyMapping keySprint = mc.options.keySprint;
        KeyMapping keyAuto = AUTO_SPRINT_KEY.get();

        long window = mc.getWindow().handle();

        boolean isW = GLFW.glfwGetKey(window, keyForward.getKey().getValue()) == GLFW.GLFW_PRESS;
        boolean isS = GLFW.glfwGetKey(window, keyBack.getKey().getValue()) == GLFW.GLFW_PRESS;
        boolean isCtrl = GLFW.glfwGetKey(window, keySprint.getKey().getValue()) == GLFW.GLFW_PRESS;

        boolean wPressed = !wasW && isW;
        boolean sPressed = !wasS && isS;
        boolean ctrlPressed = !wasCtrl && isCtrl;

        boolean autoPressed = keyAuto.consumeClick();

        // 进入/退出自动模式
        if (autoPressed) {
            if (!isAutoSprinting) {
                isAutoSprinting = true;
                boolean isMoving = player.getDeltaMovement().horizontalDistanceSqr() > 0.001;
                // 继承玩家按下W键时的跑步状态
                if (isMoving) {
                    autoSprintRunState = player.isSprinting();
                } else {
                    autoSprintRunState = true;
                }
                keyForward.setDown(true);
                setPlayerSprinting(player,autoSprintRunState);
                showCustomMessage(player,true);
            } else {
                isAutoSprinting = false;
                keyForward.setDown(false);
                setPlayerSprinting(player,false);
                showCustomMessage(player,false);
            }
        }

        if (isAutoSprinting) {
            keyForward.setDown(true);
            setPlayerSprinting(player,autoSprintRunState);

            // 自动状态下切疾跑/走
            if (ctrlPressed) {
                autoSprintRunState = !autoSprintRunState;
                setPlayerSprinting(player,autoSprintRunState);
            }

            // 按W接管
            if (wPressed) {
                isAutoSprinting = false;
                keyForward.setDown(true);
                // 继承跑步状态
                setPlayerSprinting(player, autoSprintRunState);
                showCustomMessage(player,false);
            }

            // 按S退出
            if (sPressed) {
                isAutoSprinting = false;
                keyForward.setDown(false);
                setPlayerSprinting(player,false);
                showCustomMessage(player,false);
            }
        }

        wasW = isW;
        wasS = isS;
        wasCtrl = isCtrl;

//         // @debug
//         if (player.tickCount % 5 == 0) {
//             player.sendSystemMessage(Component.literal(
//                 "Auto: " + isAutoSprinting + " RunState: " + autoSprintRunState
//             ));
//         }
    }

}

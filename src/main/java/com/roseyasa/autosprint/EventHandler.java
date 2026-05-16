package com.roseyasa.autosprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import static com.roseyasa.autosprint.AutoSprint.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class EventHandler {
    public static final KeyMapping.Category MOD_KEY_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(MODID, "category"));
    public static final String MOD_KEY_ID = "key." + MODID + ".auto_run";
    private static boolean isAutoSprinting = false;
    private static boolean autoSprintRunState = false;

    private static boolean wasW = false;
    private static boolean wasS = false;
    private static boolean wasCtrl = false;

    public static final Lazy<KeyMapping> AUTO_SPRINT_KEY = Lazy.of(() -> new KeyMapping(
        MOD_KEY_ID,
        KeyConflictContext.IN_GAME,
        InputConstants.Type.MOUSE,
        GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
        MOD_KEY_CATEGORY
    ));

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(AUTO_SPRINT_KEY.get());
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        AutoSprint.LOGGER.info("Now Playing: SPYAIR「RAGE OF DUST」");
    }



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
                player.setSprinting(autoSprintRunState);
            } else {
                isAutoSprinting = false;
                keyForward.setDown(false);
                player.setSprinting(false);
            }
        }

        if (isAutoSprinting) {
            keyForward.setDown(true);
            player.setSprinting(autoSprintRunState);

            // 自动状态下切疾跑/走
            if (ctrlPressed) {
                autoSprintRunState = !autoSprintRunState;
                player.setSprinting(autoSprintRunState);
            }

            // 按W接管
            if (wPressed) {
                isAutoSprinting = false;
                keyForward.setDown(true);
                // 继承跑步状态
                player.setSprinting(autoSprintRunState);
            }

            // 按S退出
            if (sPressed) {
                isAutoSprinting = false;
                keyForward.setDown(false);
                player.setSprinting(false);
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

package com.roseyasa.autosprint;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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
    private static boolean isAutoRunning = false;

    public static final Lazy<KeyMapping> AUTO_RUN_KEY = Lazy.of(() -> new KeyMapping(
        MOD_KEY_ID,
        KeyConflictContext.IN_GAME,
        InputConstants.Type.MOUSE,
        GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
        MOD_KEY_CATEGORY
    ));

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(AUTO_RUN_KEY.get());
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        AutoSprint.LOGGER.info("Now Playing: SPYAIR「RAGE OF DUST」");
    }

    private enum state { STOP,WALK,SPRINT,AUTO_SPRINT };

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft level = Minecraft.getInstance();
        Player player = level.player;
        if (player == null) return;
        KeyMapping forwardKey = level.options.keyUp;
        KeyMapping backwardKey = level.options.keyDown;
        KeyMapping autoRunKey = AUTO_RUN_KEY.get();
        long window = level.getWindow().handle();

        // 物理按键状态
        boolean pressingW = GLFW.glfwGetKey(window, forwardKey.getKey().getValue()) == GLFW.GLFW_PRESS;
        boolean pressingS = GLFW.glfwGetKey(window, backwardKey.getKey().getValue()) == GLFW.GLFW_PRESS;


    }

}

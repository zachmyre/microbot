package net.runelite.client.plugins.microbot.mntn.fishing;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

// * Plugin Details and logic for on start and on shutdown.
@PluginDescriptor(
        name = PluginDescriptor.Zxcharyy + "Auto FishNCooker",
        description = "Zxch's Auto FishNCooker plugin",
        tags = {"Fishing", "zxcharyy", "skilling", "cooking"},
        enabledByDefault = false
)
@Slf4j
public class AutoFishNCookPlugin extends Plugin {
    @Inject
    private AutoFishNCookConfig config;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    Notifier notifier;

    @Provides
    AutoFishNCookConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoFishNCookConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AutoFishNCookOverlay fishingOverlay;

    @Inject
    AutoFishNCookScript fishingScript;


    @Override
    protected void startUp() throws AWTException {
        Microbot.pauseAllScripts = false;
        Microbot.setClient(client);
        Microbot.setClientThread(clientThread);
        Microbot.setNotifier(notifier);
        Microbot.setMouse(new VirtualMouse());
        if (overlayManager != null) {
            overlayManager.add(fishingOverlay);
        }
        fishingScript.run(config);
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        fishingScript.onGameTick();
    }

    protected void shutDown() {
        fishingScript.shutdown();
        overlayManager.remove(fishingOverlay);
    }
}

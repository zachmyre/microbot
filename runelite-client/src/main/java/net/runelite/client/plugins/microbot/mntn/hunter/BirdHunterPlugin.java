package net.runelite.client.plugins.microbot.mntn.hunter;

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
        name = PluginDescriptor.Mntn + "Bird Hunter",
        description = "Mntn's Bird Hunter for low level hunting.",
        tags = {"hunter", "mntn", "bird"},
        enabledByDefault = false
)
@Slf4j
public class BirdHunterPlugin extends Plugin {
    @Inject
    private BirdHunterConfig config;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    Notifier notifier;

    @Provides
    BirdHunterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BirdHunterConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private BirdHunterOverlay fishingOverlay;

    @Inject
    BirdHunterScript birdHunterScript;


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
        birdHunterScript.run(config);
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        birdHunterScript.onGameTick();
    }

    protected void shutDown() {
        birdHunterScript.shutdown();
        overlayManager.remove(fishingOverlay);
    }
}

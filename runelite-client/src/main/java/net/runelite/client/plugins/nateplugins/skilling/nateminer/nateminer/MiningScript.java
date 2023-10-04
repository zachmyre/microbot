package net.runelite.client.plugins.nateplugins.skilling.nateminer.nateminer;

import net.runelite.client.plugins.envisionplugins.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.envisionplugins.breakhandler.util.BreakHandlerExecutor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Inventory;

import java.util.concurrent.TimeUnit;


public class MiningScript extends Script {

    public static double version = 1.3;
    BreakHandlerExecutor breakHandlerExecutor = new BreakHandlerExecutor();

    public boolean run(MiningConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;

            try {
                if (Microbot.isMoving() || Microbot.isAnimating() || Microbot.pauseAllScripts) return;

                breakHandlerExecutor.sendDiscordNotificationBeforeBreak(
                        new String[]{"Mining: " + MiningOverlay.getExpGained()},
                        new String[]{"NONE"},
                        "WIP");

                breakHandlerExecutor.breakOrExecute(() -> {
                    if (Inventory.isFull()) {
                        if (config.hasPickaxeInventory()) {
                            Inventory.dropAllStartingFrom(1);
                        } else {
                            Inventory.dropAll();
                        }
                        return;
                    }
                    Rs2GameObject.interact(config.ORE().getName());
                });

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
}

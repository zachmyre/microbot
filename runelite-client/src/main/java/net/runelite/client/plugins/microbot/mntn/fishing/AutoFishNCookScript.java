package net.runelite.client.plugins.microbot.mntn.fishing;

import net.runelite.api.NPC;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.mntn.fishing.enums.Fishs;
import net.runelite.client.plugins.microbot.playerassist.enums.PlayStyle;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.DropOrder;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.validateInteractable;

// * Script with actual logic that plugin performs
public class AutoFishNCookScript extends Script {

    public static String version = "1.0.0";
    public static int timeout = 0;

    public boolean run(AutoFishNCookConfig config) {
        initialPlayerLocation = null;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {

                if (!super.run()) return;
                if (!Microbot.isLoggedIn()) return;
// ******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************
                // Set initial location of player
                if (initialPlayerLocation == null) {
                    initialPlayerLocation = Rs2Player.getWorldLocation();
                }

                if (timeout > 0) {
                    Microbot.status = "Sleeping (AFK antiban) : " + timeout;
                    sleep(800, 22000);
                    return;
                }

                // Check if cook is enabled and verify if axe + tinderbox are available (wielded/inventory)
                if (config.Cook() && (!Rs2Inventory.hasItem("tinderbox") || (!Rs2Inventory.hasItem("axe") && !Rs2Equipment.isWearing("axe")))) {
                    Microbot.status = "Must have axe / tinderbox if cooking is enabled!";
                    return;
                }

                // If moving / animation / (fishing trout but no feathers) then return
                if (Rs2Player.isMoving() || Rs2Player.isAnimating() || config.Fish() == Fishs.TROUT && !Rs2Inventory.hasItem("feather")) {

                    return;
                }




                List<String> itemNames = Arrays.stream(config.itemsToBank().split(",")).map(String::toLowerCase).collect(Collectors.toList());

                NPC fishingspot = null;
                for (int fishingSpotId : config.Fish().getFishingSpot()) {
                    fishingspot = Rs2Npc.getNpc(fishingSpotId);
                    if (fishingspot != null) {
                        break;
                    }
                }


                if (config.useBank()) {
                    if (fishingspot == null || Rs2Inventory.isFull()) {
                        Microbot.status = "Banking.";
                        if (!Rs2Bank.bankItemsAndWalkBackToOriginalPosition(itemNames, initialPlayerLocation))
                            return;
                    }
                } else if (config.Cook()) {
                    Microbot.status = "Cooking fish..";
                    // !!! TODO -- finish cooking
                    // Find tree if log not in inventory
                    // Chop tree
                    // Light log
                    // Use fish on fire
                    // Drop all when done
                    // continue fishing
                    System.out.println("Cooking time :) ");
                } else if (Rs2Inventory.isFull()) {
                    Rs2Inventory.dropAllExcept(false, DropOrder.random(),"rod", "net", "pot", "harpoon", "feather", "bait", "vessel", "axe", "coins", "tinderbox");
                    return;
                }

                Microbot.status = "Sleeping before interacting with fishing spot.";
                sleep(100,2250);

                if (fishingspot != null && !Rs2Camera.isTileOnScreen(fishingspot.getLocalLocation())) {
                    validateInteractable(fishingspot);
                }

                Rs2Npc.interact(fishingspot, config.Fish().getAction());
// END ******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    public void onGameTick() {
        if (timeout > 0 && !Rs2Player.isInteracting()) {
            timeout--;
        }
        if (Rs2Player.isInteracting() && timeout == 0) {
            timeout = PlayStyle.PASSIVE.getRandomTickInterval();
        }
    }
}

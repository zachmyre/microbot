package net.runelite.client.plugins.microbot.mntn.hunter;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.mntn.hunter.enums.State;
import net.runelite.client.plugins.microbot.playerassist.enums.PlayStyle;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BirdHunterScript extends Script {

    public static String version = "1.0.0";
    public static int timeout = 0;
    public static final int BIRD_TRAP = 10006;
    public static State currentState = null;
    public static GameObject currentTrap = null;
    private int retryCount = 0;
    private WorldPoint startLocation;

    public boolean run(BirdHunterConfig config) {

        // Record starting location
        startLocation = Rs2Player.getWorldLocation();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {

                if (!super.run()) return;
                if (!Microbot.isLoggedIn()) return;




                System.out.println("Current State: " + currentState);

                if(currentState == null){
                    currentState = State.CHECK_TRAP;
                }

                this.pickUpBirdSnares();

                switch (currentState) {
                    case SET_TRAP:
                        Microbot.status = "Setting trap.";
                        this.setTrap(config);
                        break;
                    case PICKUP_TRAP:
                        Microbot.status = "Picking up trap.";
                        this.pickupTrap(config);
                        break;
                    case WALK_AWAY:
                        Microbot.status = "Walking away.";
                        this.walkAway(config);
                        break;
                    case CHECK_TRAP:
                        Microbot.status = "Checking trap.";
                        this.checkTrap(config);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + currentState);
                }

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

    public void checkTrap(BirdHunterConfig config) {
        System.out.println("Checking trap..");
        // Set of target IDs
        Set<Integer> targetIds = new HashSet<>();
        targetIds.add(9345); // Laid trap id
        targetIds.add(9344); // Broken trap id
        targetIds.add(9373); // Caught trap id

        // List of game objects
        List<GameObject> gameObjectList = Rs2GameObject.getGameObjects();

        boolean hasLaidTrap = false;
        boolean hasBrokenTrap = false;
        boolean hasCaughtTrap = false;

        // Iterate through the list and check if the ID is in the set
        for (GameObject gameObject : gameObjectList) {
            int id = gameObject.getId();
            if (targetIds.contains(id)) {
                currentTrap = gameObject;
                switch (id) {
                    case 9345:
                        hasLaidTrap = true;
                        break;
                    case 9344:
                        hasBrokenTrap = true;
                        break;
                    case 9373:
                        hasCaughtTrap = true;
                        break;
                }
            }
        }






        // Handle traps based on the flags
        if (hasLaidTrap || hasBrokenTrap || hasCaughtTrap) {
            System.out.println("Trap found.");
            sleep(800, 4500);
            retryCount = 0; // Reset retry count on success

            // Ensure the current trap is within view of the camera
            if (!Rs2Camera.isTileOnScreen(currentTrap)) {
                Rs2Camera.turnTo(currentTrap);
                sleepUntil(() -> Rs2Camera.isTileOnScreen(currentTrap), 3000);
            }

            if (hasBrokenTrap || hasCaughtTrap) {
                currentState = State.PICKUP_TRAP;
            }
        }

        if (!hasLaidTrap && !hasBrokenTrap && !hasCaughtTrap) {
            System.out.println("No relevant traps found. Find a good spot and set the trap.");
            if (retryCount < 3) {
                retryCount++;
                sleep(1000); // Wait before retrying
                checkTrap(config); // Recheck traps
            } else {
                currentState = State.SET_TRAP;
                retryCount = 0; // Reset retry count
            }
        }
    }

    public void pickUpBirdSnares() {
                if(Rs2GroundItem.exists("Bird snare", 1)){
                    Rs2GroundItem.interact("Bird snare", "Take");
                    sleep(1200, 2000);
                    currentState = State.CHECK_TRAP;
                }
    }

    public void pickupTrap(BirdHunterConfig config){
        if (Rs2Camera.isTileOnScreen(currentTrap)) {
            Rs2Walker.walkFastLocal(currentTrap.getLocalLocation());
            sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(currentTrap.getWorldLocation()) < 10);
            Rs2GameObject.interact(currentTrap.getId());
            sleepUntil(() -> !Rs2Player.isMoving());
            sleep(600, 1000);

            // Check if the trap is still there
            if (!Rs2GameObject.exists(currentTrap.getId())) {
                this.handleInventory(config);
                System.out.println("Trap picked up, time to set!");
                currentState = State.SET_TRAP;
            } else {
                currentState = State.CHECK_TRAP;
            }
            return;
        } else {
            Rs2Camera.turnTo(currentTrap);
            sleep(300, 900);
            return;
        }
    }


    public void setTrap(BirdHunterConfig config) {
        // Ensure the player is near the starting location
        if (!Rs2Inventory.hasItem(BIRD_TRAP)) {
            Microbot.status = "No bird traps found.";
            return;
        }

        this.handleInventory(config);

        if (Rs2Player.getWorldLocation().distanceTo(startLocation) > 10) {
            Rs2Walker.walkTo(startLocation);
            sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(startLocation) <= 5, 5000);
        }

        System.out.println("Setting trap :)");
        sleep(500, 900);
        Rs2Inventory.interact(BIRD_TRAP, "Lay");
        sleep(500, 900);
        // Wait until the trap is set
        boolean trapSet = sleepUntil(() -> {
            List<GameObject> traps = Rs2GameObject.getGameObjects();
            for (GameObject trap : traps) {
                if (trap.getId() == 9345) { // ID for a laid trap
                    System.out.println("Trap set, let's move on!");
                    return true;
                }
            }
            return false;
        }, 5000); // Wait up to 5 seconds for the trap to be set
        sleep(1200, 1800);
        if (trapSet) {
            currentState = State.WALK_AWAY;
        } else {
            currentState = State.SET_TRAP;
        }
    }

    public void walkAway(BirdHunterConfig config) {
        System.out.println("Walking away now..");
        WorldPoint currentLocation = Rs2Player.getWorldLocation();

        // Generate random dx and dy between 2 and 5
        int randomDx = Random.random(2, 6) * (Random.random(0, 2) == 1 ? -1 : 1); // Randomly choose between negative and positive
        int randomDy = Random.random(3, 6) * (Random.random(0, 2) == 1 ? -1 : 1); // Randomly choose between negative and positive

        WorldPoint targetLocation = currentLocation.dx(randomDx).dy(randomDy);

        // Convert the target world point to a local point
        LocalPoint localPoint = LocalPoint.fromWorld(Microbot.getClient(), targetLocation);

        // Translate the local point to a canvas point
        if (localPoint != null) {
            Point canvasPoint = Perspective.localToCanvas(Microbot.getClient(), localPoint, Microbot.getClient().getPlane());

            if (canvasPoint != null) {
                // Simulate a mouse click on the canvas point
                Microbot.getMouse().click(canvasPoint);
            }
        }

        sleep(400, 900);
        currentState = State.CHECK_TRAP;
    }





    public void handleInventory(BirdHunterConfig config){
        boolean shouldHandle = Random.random(0,2) == 1;
        // Handle burying bones if config option is enabled
        if(shouldHandle){
            if (config.BuryBones() && Rs2Inventory.hasItem("Bones")) {
                System.out.println("Burying bones...");
                while(Rs2Inventory.hasItem("Bones")){
                    Rs2Inventory.interact("Bones", "Bury");
                }
                sleep(600, 1000);

                Rs2Inventory.dropAll("Raw bird meat");
            } else if (!config.BuryBones() && (Rs2Inventory.hasItem("Bones") || Rs2Inventory.hasItem("Raw bird meat"))) {
                System.out.println("Dropping items...");
                Rs2Inventory.dropAll("Bones", "Raw bird meat");
            }
        }

    }

}

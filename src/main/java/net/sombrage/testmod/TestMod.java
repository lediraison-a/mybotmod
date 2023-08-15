package net.sombrage.testmod;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.PlayerUpdateEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.sombrage.testmod.actions.CloseContainerAction;
import net.sombrage.testmod.actions.GotoAction;
import net.sombrage.testmod.actions.IMyAction;
import net.sombrage.testmod.actions.InteractContainerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;


public class TestMod {

    public static final Logger LOGGER = LoggerFactory.getLogger(TestMod.class);

    public enum STATUS {
        STOP,
        PATHING,
        IDLE,
        INTERACTING;
    }

    private IBaritone baritone;

    ContainerInteractionManager containerInteractionManager;

    public STATUS currentStatus;

    private ContainerAccessPosition dumpPos;


    private Queue<IMyAction> actionList;
    private IMyAction currentAction;

    public TestMod() {
        this.baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        currentStatus = STATUS.STOP;
        actionList = new LinkedList<>();

        containerInteractionManager = null;

        new MyBaritoneEventListener(() -> {
            if (currentAction instanceof GotoAction) {
                currentStatus = STATUS.IDLE;
                if (!((GotoAction) currentAction).isGoalReached()) {
                    return;
                }
                next();
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (currentAction == null) {
                return;
            }
            if (client.currentScreen instanceof GenericContainerScreen && currentAction instanceof InteractContainerAction) {
                currentStatus = STATUS.INTERACTING;
                containerInteractionManager = new ContainerInteractionManager((GenericContainerScreen) client.currentScreen);
                containerInteractionManager.printContent();
                next();
            }
        });

        // register onCloseContainer listener
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (currentAction == null) {
                return;
            }
            if (client.currentScreen == null && currentAction instanceof CloseContainerAction) {
                currentStatus = STATUS.IDLE;
                next();
            }
        });
    }
    public void start() {
        LOGGER.info("start");
        BaritoneAPI.getSettings().allowBreak.value = false;
        currentStatus = STATUS.IDLE;


        actionList.add(new GotoAction(dumpPos));
        actionList.add(new InteractContainerAction());
        actionList.add(new CloseContainerAction());

        next();
    }

    public void stop() {
        LOGGER.info("stop");
        currentStatus = STATUS.STOP;
        baritone.getPathingBehavior().cancelEverything();
        actionList.clear();
    }

    public ContainerAccessPosition getDumpPos() {
        return dumpPos;
    }

    public void setDumpPos() {
        var player = baritone.getPlayerContext().player();
        this.dumpPos = new ContainerAccessPosition(player.getPos(), player.getYaw(), player.getPitch());

    }

    private void next() {
        if (actionList.isEmpty()) {
            stop();
            return;
        }
        currentAction = actionList.remove();
        currentStatus = currentAction.run();
    }





//    private void gotoDump() {
//        currentStatus = STATUS.PATHING;
//        interactPos = dumpPos;
//        // baritoneGoal = new GoalBlock(new BlockPos(Utils.convertVec3dToVec3i(dumpPos.pos)));
//        // baritone.getCustomGoalProcess().setGoalAndPath(baritoneGoal);
//    }

//    public void openContainer() {
//        // currentStatus = STATUS.INTERACTING;
//        var client = MinecraftClient.getInstance();
//        client.player.setYaw(interactPos.yaw);
//        client.player.setPitch(interactPos.pitch);
//
//        if (client.crosshairTarget instanceof BlockHitResult) {
//            BlockHitResult blockHit = (BlockHitResult) client.crosshairTarget;
//            var ar = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
//        }
//    }



//    public boolean isGoalReached() {
//        if (currentStatus != STATUS.PATHING) {
//            return false;
//        }
//        if (baritoneGoal.isInGoal(new BlockPos(Utils.convertVec3dToVec3i(interactPos.pos)))) {
//            currentStatus = STATUS.IDLE;
//        }
//        return true;
//    }




}



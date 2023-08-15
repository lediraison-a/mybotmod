package net.sombrage.testmod;

import baritone.api.BaritoneAPI;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.sombrage.testmod.actions.*;
import net.sombrage.testmod.position.PositionRegister;
import net.sombrage.testmod.utils.ActionExecutionException;
import net.sombrage.testmod.utils.TickDelayExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;


public class TestMod {

    private static TestMod instance;

    public static final Logger LOGGER = LoggerFactory.getLogger(TestMod.class);



    public enum STATUS {
        STOP,
        PATHING,
        IDLE,
        WAITING,
        INTERACTING;
    }

    ContainerInteractionManager containerInteractionManager;

    private PositionRegister positionRegister;


    private Queue<IMyAction> actionList;
    private IMyAction currentAction;
    public STATUS currentStatus;

    private TickDelayExecutor tickDelayExecutor;

    private TestMod() {
        currentStatus = STATUS.STOP;
        actionList = new LinkedList<>();

        containerInteractionManager = null;
        positionRegister = new PositionRegister();

        tickDelayExecutor = new TickDelayExecutor();

        setListeners();
    }

    private void setListeners() {

        new MyBaritoneEventListener(() -> {
            if (currentAction instanceof GotoAction) {
                currentStatus = STATUS.IDLE;
                if (((GotoAction) currentAction).isGoalReached()) {
                    tickDelayExecutor.scheduleTask(() -> {
                        next();
                    }, 1);
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (currentAction == null) {
                return;
            }
            LOGGER.info("action " + currentAction.getClass().getSimpleName());
            if (currentAction instanceof WaitAction) {
                currentStatus = STATUS.WAITING;
                if (!((WaitAction) currentAction).isDone()) {
                    return;
                }
                next();
            }
            if (client.currentScreen instanceof GenericContainerScreen && currentAction instanceof InteractBlockAction) {
                currentStatus = STATUS.INTERACTING;
                var ci = new ContainerInteractionManager((GenericContainerScreen) client.currentScreen);
                containerInteractionManager = ci;
                next();
            }

        });
    }

    public ContainerInteractionManager getContainerInteractionManager() throws ActionExecutionException {
        if (containerInteractionManager == null) {
            throw new ActionExecutionException("no container open");
        }
        return containerInteractionManager;
    }
    public static TestMod getInstance() {
        if (instance == null) {
            instance = new TestMod();
        }
        return instance;
    }

    public PositionRegister getPositionRegister() {
        return positionRegister;
    }
    public void start() {
        LOGGER.info("start");
        BaritoneAPI.getSettings().allowBreak.value = false;
        currentStatus = STATUS.IDLE;


        actionList.add(new GotoAction(positionRegister.get("pos")));
        actionList.add(new InteractBlockAction());
        actionList.add(new TakeAllAction());
        actionList.add(new CloseContainerAction());
        actionList.add(new GotoAction(positionRegister.get("pos2")));
        actionList.add(new InteractBlockAction());
        actionList.add(new DepositAllAction());
        actionList.add(new CloseContainerAction());
        actionList.add(new GotoAction(positionRegister.get("pos3")));
        actionList.add(new WaitAction(100));
        actionList.add(new GotoAction(positionRegister.get("pos4")));

        next();
    }

    public void stop() {
        LOGGER.info("stop");
        currentStatus = STATUS.STOP;
        currentAction = null;
//        if (containerInteractionManager != null) {
//            containerInteractionManager.closeContainer();
//        }
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        actionList.clear();
    }

    private void next() {
        if (actionList.isEmpty()) {
            stop();
            return;
        }

        currentAction = actionList.remove();
        LOGGER.info("next action: " + currentAction.getClass().getSimpleName());
        runAction();

    }

    private void runAction() {
        LOGGER.info("run action: " + currentAction.getClass().getSimpleName());
        var playNext = currentAction.playNextAction();
        try {
            currentStatus = currentAction.run();
            if (playNext) {
                next();
            }
        } catch (ActionExecutionException e) {
            LOGGER.error("ActionExecutionException", e);
            stop();
        }
    }

}



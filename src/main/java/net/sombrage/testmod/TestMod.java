package net.sombrage.testmod;

import baritone.api.BaritoneAPI;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.text.Text;
import net.sombrage.testmod.actions.GotoAction;
import net.sombrage.testmod.actions.IMyAction;
import net.sombrage.testmod.actions.InteractBlockAction;
import net.sombrage.testmod.actions.WaitAction;
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


    private LinkedList<IMyAction> actionList;

    private LinkedList<IMyAction> currentActionList;


    private IMyAction currentAction;
    public STATUS currentStatus;

    private TickDelayExecutor tickDelayExecutor;

    private boolean logStatus = true;

    private TestMod() {
        currentStatus = STATUS.STOP;
        actionList = new LinkedList<>();
        currentActionList = new LinkedList<>();

        positionRegister = new PositionRegister();

        containerInteractionManager = null;

        tickDelayExecutor = new TickDelayExecutor();

        setListeners();
    }

    private void setListeners() {

        new MyBaritoneEventListener(() -> {
            if (currentAction instanceof GotoAction) {
                updateStatus(currentStatus = STATUS.IDLE);
                if (((GotoAction) currentAction).isGoalReached()) {
                    tickDelayExecutor.scheduleTask(this::next, 1);
                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.info("Disconnected from the server!");
            stop();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            positionRegister.loadFromCsv();
            LOGGER.info("Connected to the server!");
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (currentAction == null) {
                return;
            }
            LOGGER.info("action " + currentAction.getClass().getSimpleName());
            if (currentAction instanceof WaitAction) {
                updateStatus(currentStatus = STATUS.WAITING);
                if (!((WaitAction) currentAction).isDone()) {
                    return;
                }
                next();
            }
            if (client.currentScreen instanceof GenericContainerScreen && currentAction instanceof InteractBlockAction) {
                updateStatus(currentStatus = STATUS.INTERACTING);
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

    public Queue<IMyAction> getActionList() {
        return actionList;
    }

    public PositionRegister getPositionRegister() {
        return positionRegister;
    }
    public void start() {
        LOGGER.info("start");
        BaritoneAPI.getSettings().allowBreak.value = false;
        currentStatus = STATUS.IDLE;
        next();
    }

    public void stop() {
        LOGGER.info("stop");
        updateStatus(STATUS.STOP);
        currentAction = null;
//        if (containerInteractionManager != null) {
//            containerInteractionManager.closeContainer();
//        }
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        actionList.clear();
    }

    private void next() {
        if (currentActionList.isEmpty()) {
            stop();
            return;
        }

        currentAction = currentActionList.remove();
        LOGGER.info("next action: " + currentAction.getClass().getSimpleName());
        runAction();

    }

    private void runAction() {
        LOGGER.info("run action: " + currentAction.getClass().getSimpleName());
        var playNext = currentAction.playNextAction();
        try {
            updateStatus(currentAction.run());
            if (playNext) {
                next();
            }
        } catch (ActionExecutionException e) {
            LOGGER.error("ActionExecutionException", e);
            stop();
        }
    }

    private void updateStatus(STATUS status) {
        if(logStatus) {
            LOGGER.info("status: " + status);
            MinecraftClient.getInstance().player.sendMessage(Text.of("status: " + status), false);
        }
        currentStatus = status;
    }

}



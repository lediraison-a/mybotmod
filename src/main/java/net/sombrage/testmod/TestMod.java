package net.sombrage.testmod;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.sombrage.testmod.position.ContainerAccessPosition;
import net.sombrage.testmod.position.PositionRegister;
import net.sombrage.testmod.utils.ActionExecutionException;
import net.sombrage.testmod.utils.TickDelayExecutor;
import net.sombrage.testmod.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public class TestMod {

    public enum STATUS {
        WAIT,
        INTERACT,
        PATHING,
        STOP,
        IDLE,
    }

    private static TestMod instance;

    public static final Logger LOGGER = LoggerFactory.getLogger(TestMod.class);

    ContainerInteractionManager containerInteractionManager;

    private PositionRegister positionRegister;

    private ContainerAccessPosition goalPosition;
    private GoalBlock goal;


    private List<Callable<Boolean>> actions;

    private List<ActionList> runningActions;
    private int runningActionIndex;

    private int runningActionListIndex;

    private STATUS currentStatus;
    private TickDelayExecutor tickDelayExecutor;
    private int waitingTicks;


    private TestMod() {
        actions = new ArrayList<>();
        runningActions = new ArrayList<>();

        positionRegister = new PositionRegister();

        containerInteractionManager = null;

        tickDelayExecutor = new TickDelayExecutor();

        waitingTicks = 0;
        runningActionIndex = -1;
        runningActionListIndex = 0;

        currentStatus = STATUS.STOP;

        setListeners();
    }

    private void setListeners() {

        new MyBaritoneEventListener(() -> {
            if (currentStatus == STATUS.PATHING) {
                if (isGoalReached()) {
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
            if (currentStatus == null) {
                return;
            }
            if (currentStatus == STATUS.WAIT) {
                if (!isDone()) {
                    return;
                }
                next();
            }
            if (client.currentScreen instanceof GenericContainerScreen && currentStatus.equals(STATUS.INTERACT)) {
                var ci = new ContainerInteractionManager((GenericContainerScreen) client.currentScreen);
                containerInteractionManager = ci;
                next();
            }

        });
    }

    public void start() {
        LOGGER.info("start");
        currentStatus = STATUS.IDLE;
        // runningActions = List.of(new ActionList(repeatActions, List.copyOf(actions)));
        BaritoneAPI.getSettings().allowBreak.value = false;
        next();
    }

    public void stop() {
        LOGGER.info("stop");
        currentStatus = STATUS.STOP;
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        runningActionIndex = -1;
    }

    public boolean actionGoto(ContainerAccessPosition position) {
        goalPosition = position;
        goal = new GoalBlock(new BlockPos(Utils.convertVec3dToVec3i(position.pos)));
        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        baritone.getCustomGoalProcess().setGoalAndPath(goal);
        currentStatus = STATUS.PATHING;
        return false;
    }

    public boolean actionInteractBlock() {
        var client = MinecraftClient.getInstance();
        if (client.crosshairTarget instanceof BlockHitResult) {
            BlockHitResult blockHit = (BlockHitResult) client.crosshairTarget;
            var ar = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
            currentStatus = STATUS.INTERACT;
        }
        return false;
    }

    public boolean actionWait(int ticks) {
        waitingTicks = ticks;
        currentStatus = STATUS.WAIT;
        return false;
    }

    public boolean actionCloseContainer() throws ActionExecutionException {
        getContainerInteractionManager().closeContainer();
        currentStatus = STATUS.IDLE;
        return true;
    }

    private boolean isGoalReached() {
        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if(goal.isInGoal(new BlockPos(Utils.convertVec3dToVec3i(goalPosition.pos)))
        ) {
            baritone.getPlayerContext().player().lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, goalPosition.targetPos);
            return true;
        }
        return false;
    }

    public boolean isDone() {
        waitingTicks--;
        return waitingTicks == 0;
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


    private void next() {
        runningActionIndex++;
        if (runningActionIndex > runningActions.get(runningActionListIndex).actions.size() - 1) {
            if (!runningActions.get(runningActionListIndex).repeatActions) {
                runningActionListIndex++;
            }
            runningActionIndex = 0;
        }
        if (runningActionListIndex > runningActions.size() - 1) {
            stop();
            return;
        }

        try {
            var playNext = runningActions
                    .get(runningActionListIndex).actions
                    .get(runningActionIndex).call();
            if (playNext) {
                next();
            }
        } catch (ActionExecutionException e) {
            LOGGER.error("ActionExecutionException", e);
            stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Callable<Boolean>> getActions() {
        return actions;
    }

    public String actionsToString() {
        var sb = new StringBuilder();
        for (var action : actions) {
            sb.append(action.getClass().getSimpleName());
            sb.append("\n");
        }
        return sb.toString();
    }

    public void actionSortPositionsContent(List<ContainerAccessPosition> positions) {
        var filteredEmptyPos = positions.stream().filter(pos -> pos.doFilter && pos.filterItems.isEmpty()).toList();
        if (!filteredEmptyPos.isEmpty()) {
            var actions = new ArrayList<Callable<Boolean>>();
            for (var pos : filteredEmptyPos) {
                actions.add(() -> actionGoto(pos));
                actions.add(() -> actionInteractBlock());
                actions.add(() -> {
                    pos.filterItems = getContainerInteractionManager().getAllItemTypes(containerInteractionManager.containerSlots);
                    return true;
                });
                actions.add(() -> actionCloseContainer());
            }
            runningActions.add(new ActionList(false, actions));
        }

        var actions = new ArrayList<Callable<Boolean>>();
        for (var pos : positions) {
            actions.add(() -> actionGoto(pos));
            actions.add(() -> actionInteractBlock());
            actions.add(() -> {
                if (pos.doFilter) {
                    containerInteractionManager.transferAllOf(pos.filterItems, ContainerInteractionManager.TRANSFER_DIRECTION.PLAYER_TO_CONTAINER);
                    containerInteractionManager.transferAllExcept(pos.filterItems, ContainerInteractionManager.TRANSFER_DIRECTION.CONTAINER_TO_PLAYER);
                } else {
                    containerInteractionManager.transferAll(ContainerInteractionManager.TRANSFER_DIRECTION.CONTAINER_TO_PLAYER);
                }
                return true;
            });
            actions.add(() -> actionCloseContainer());
        }
        runningActions.add(new ActionList(false, actions));

    }

}



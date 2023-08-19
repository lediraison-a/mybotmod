package net.sombrage.testmod;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.sombrage.testmod.models.ActionList;
import net.sombrage.testmod.models.ContainerAccessPosition;
import net.sombrage.testmod.utils.ActionExecutionException;
import net.sombrage.testmod.utils.TickDelayExecutor;
import net.sombrage.testmod.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


public class TestMod {

    public enum STATUS {
        WAIT,
        INTERACT,
        PATHING,
        STOP,
        IDLE,
    }

    // _____________________________________________________________________________

    private static TestMod instance;

    public static TestMod getInstance() {
        if (instance == null) {
            instance = new TestMod();
        }
        return instance;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMod.class);



    ContainerInteractionManager containerInteractionManager;

    // baritone goal
    private ContainerAccessPosition goalPosition;
    private GoalBlock goal;


    // actions
    private List<ActionList> runningActions;
    private int runningActionIndex;
    private int runningActionListIndex;

    // status
    private STATUS currentStatus;

    // tick delay
    private final TickDelayExecutor tickDelayExecutor;
    private int waitingTicks;


    // settings
    private TestModSettings settings;

    // positions
    private Map<String, ContainerAccessPosition> positions;

    private ContainerAccessPosition startPostion;

    // _____________________________________________________________________________

    private TestMod() {
        runningActions = new ArrayList<>();

        containerInteractionManager = null;

        tickDelayExecutor = new TickDelayExecutor();

        waitingTicks = 0;
        runningActionIndex = -1;
        runningActionListIndex = 0;

        currentStatus = STATUS.STOP;

        settings = new TestModSettings();

        positions = new HashMap<>();

        setListeners();
    }

    // _____________________________________________________________________________
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
            // positionRegister.loadFromCsv();
            LOGGER.info("Connected to the server!");
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (currentStatus == null) {
                return;
            }
            if (currentStatus == STATUS.WAIT) {
                if (!isDoneWaiting()) {
                    return;
                }
                next();
            }
            if (client.currentScreen instanceof GenericContainerScreen &&
                            currentStatus.equals(STATUS.INTERACT)) {
                containerInteractionManager = new ContainerInteractionManager(
                        (GenericContainerScreen) client.currentScreen,
                        settings.ignoreHandBar);
                next();
            }
        });
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
            if (settings.endAtStartPosition) {
                actionEndAtStart();
                return;
            }
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


    private boolean isGoalReached() {
        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if(goal.isInGoal(new BlockPos(Utils.convertVec3dToVec3i(goalPosition.pos)))
        ) {
            baritone.getPlayerContext()
                    .player()
                    .lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, goalPosition.targetPos);
            return true;
        }
        return false;
    }

    private boolean isDoneWaiting() {
        waitingTicks--;
        return waitingTicks == 0;
    }



    private static void logToPlayer(String text) {
        var player = MinecraftClient.getInstance().player;
        player.sendMessage(Text.of(text), false);
    }

    // _____________________________________________________________________________

    private ContainerInteractionManager getContainerInteractionManager() throws ActionExecutionException {
        if (containerInteractionManager == null) {
            throw new ActionExecutionException("no container open");
        }
        return containerInteractionManager;
    }

    public Map<String, ContainerAccessPosition> getPositions() {
        return positions;
    }

    // _____________________________________________________________________________

    public void start() {
        LOGGER.info("start");
        logToPlayer("start");
        currentStatus = STATUS.IDLE;
        // runningActions = List.of(new ActionList(repeatActions, List.copyOf(actions)));
        BaritoneAPI.getSettings().allowBreak.value = false;
        startPostion = new ContainerAccessPosition();
        next();
    }

    public void stop() {
        LOGGER.info("stop");
        logToPlayer("stop");
        currentStatus = STATUS.STOP;
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        runningActions = new ArrayList<>();
        runningActionIndex = -1;
        runningActionListIndex = 0;
    }

    // _____________________________________________________________________________

    private boolean actionGoto(ContainerAccessPosition position) {
        goalPosition = position;
        goal = new GoalBlock(new BlockPos(Utils.convertVec3dToVec3i(position.pos)));
        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        baritone.getCustomGoalProcess().setGoalAndPath(goal);
        currentStatus = STATUS.PATHING;
        return false;
    }

    private boolean actionInteractBlock() {
        var client = MinecraftClient.getInstance();
        if (client.crosshairTarget instanceof BlockHitResult) {
            BlockHitResult blockHit = (BlockHitResult) client.crosshairTarget;
            var ar = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
            currentStatus = STATUS.INTERACT;
        }
        return false;
    }

    private boolean actionWait(int ticks) {
        waitingTicks = ticks;
        currentStatus = STATUS.WAIT;
        return false;
    }

    private boolean actionCloseContainer() throws ActionExecutionException {
        getContainerInteractionManager().closeContainer();
        currentStatus = STATUS.IDLE;
        return true;
    }

    // _____________________________________________________________________________


    private void actionExploreUnknownFilters(List<ContainerAccessPosition> positions) {
        if (!settings.exploreUnknownFilters) {
            return;
        }
        var filteredEmptyPos = positions.stream()
                .filter(pos -> pos.doFilter && pos.filterItems.isEmpty())
                .toList();

        if (filteredEmptyPos.isEmpty()) {
            return;
        }

        var actions = new ArrayList<Callable<Boolean>>();
        for (var pos : filteredEmptyPos) {
            actions.add(() -> actionGoto(pos));
            actions.add(this::actionInteractBlock);
            actions.add(() -> {
                pos.filterItems = ContainerInteractionManager
                        .getAllItemTypes(containerInteractionManager.containerSlots);
                return true;
            });
            actions.add(this::actionCloseContainer);
        }
        runningActions.add(new ActionList(false, actions));
    }

    public void actionSortPositionsContent(List<ContainerAccessPosition> positions) {
        actionEndAtStart();
        actionExploreUnknownFilters(positions);

        runningActions.add(new ActionList(false, List.of(() -> {
            List<Item> filterItems = new ArrayList<>();
            var actions = new ArrayList<Callable<Boolean>>();

            positions.stream()
                    .filter(pos -> pos.doFilter)
                    .map(pos -> pos.filterItems)
                    .forEachOrdered(filterItems::addAll);

            for (var pos : positions) {
                actions.add(() -> actionGoto(pos));
                actions.add(this::actionInteractBlock);
                actions.add(() -> {
                    if (pos.doFilter) {
                        containerInteractionManager.transferAllOf(
                                pos.filterItems,
                                ContainerInteractionManager.TRANSFER_DIRECTION.PLAYER_TO_CONTAINER);
                        containerInteractionManager.transferAllExcept(
                                pos.filterItems,
                                ContainerInteractionManager.TRANSFER_DIRECTION.CONTAINER_TO_PLAYER);
                    } else {
                        containerInteractionManager.transferAllOf(
                                filterItems,
                                ContainerInteractionManager.TRANSFER_DIRECTION.CONTAINER_TO_PLAYER);
                    }
                    return true;
                });
                actions.add(this::actionCloseContainer);
            }

            if (!actions.isEmpty()) {
                runningActions.add(new ActionList(false, actions));
            }
            return true;
        })));
    }

    public void actionSortPlayer(List<ContainerAccessPosition> positions) {
        actionEndAtStart();
        actionExploreUnknownFilters(positions);

        runningActions.add(new ActionList(false, List.of(() -> {
            var playerItems = ContainerInteractionManager.getPlayerItems();
            var actions = new ArrayList<Callable<Boolean>>();

            var filterPos = positions.stream()
                    .filter(pos -> pos.doFilter && pos.filterItems.stream().anyMatch(playerItems::contains))
                    .toList();

            for (var pos : filterPos) {
                actions.add(() -> actionGoto(pos));
                actions.add(this::actionInteractBlock);
                actions.add(() -> {
                    containerInteractionManager.transferAllOf(
                            pos.filterItems,
                            ContainerInteractionManager.TRANSFER_DIRECTION.PLAYER_TO_CONTAINER);
                    return true;
                });
                actions.add(this::actionCloseContainer);
            }

            if (!actions.isEmpty()) {
                runningActions.add(new ActionList(false, actions));
            }

            return true;
        })));
    }

    private void actionEndAtStart() {
        var actions = new ArrayList<Callable<Boolean>>();
        actions.add(() -> {
            runningActions.add(new ActionList(false, List.of(() -> actionGoto(startPostion))));
            return true;
        });
        runningActions.add(new ActionList(false, actions));
    }


}



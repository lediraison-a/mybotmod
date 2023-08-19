package net.sombrage.testmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.sombrage.testmod.TestMod;
import net.sombrage.testmod.models.ContainerAccessPosition;
import net.sombrage.testmod.utils.Utils;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientCommandManager {

    private String baseCommand = "mm";

    public ClientCommandManager() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));

    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var base = literal(baseCommand)
                // .then(buildStartCommand())
                .then(buildSortPlayerCommand())
                .then(buildSortContainers())
                .then(buildStopCommand())
                .then(buildPositionCommand());

        dispatcher.register(base);
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildPositionCommand() {
        return literal("container")
                .then(buildPositionListCommand())
                // .then(buildPositionSaveCommand())
                .then(buildPositionClearCommand())
                .then(buildPositionAddCommand())
                .then(buildPositionRemoveCommand())
                .then(buildPositionGetCommand());

    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildStartCommand() {
        return literal("start")
                .executes(context -> {
                    context.getSource().getPlayer().sendMessage(Text.of("Start"));
                    TestMod.getInstance().start();
                    return 1;
                });
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildStopCommand() {
        return literal("stop")
                .executes(context -> {
                    context.getSource().getPlayer().sendMessage(Text.of("Stop"));
                    TestMod.getInstance().stop();
                    return 1;
                });
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildPositionListCommand() {
        return literal("list")
                .executes(context -> {
                    var pg = TestMod.getInstance().getPositions();
                    var t = "position list : \n";
                    for (var tag : pg.keySet()) {
                        t += tag + " \n";
                    }
                    context.getSource().getPlayer().sendMessage(Text.of(t));
                    return 1;
                });
    }

//    private LiteralArgumentBuilder<FabricClientCommandSource> buildPositionSaveCommand() {
//        return literal("save")
//                .executes(context -> {
//                    context.getSource().getPlayer().sendMessage(Text.of("Save positions"));
//                    TestMod.getInstance().getPositions().saveToCsv();
//                    return 1;
//                });
//    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildPositionClearCommand() {
        return literal("clear")
                .executes(context -> {
                    context.getSource().getPlayer().sendMessage(Text.of("Clear positions"));
                    TestMod.getInstance().getPositions().clear();
                    return 1;
                });
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildPositionAddCommand() {
        return literal("add")
                .then(argument("tag", StringArgumentType.string())
                        .executes(context -> {
                            var testMod = TestMod.getInstance();
                            var tag = StringArgumentType.getString(context, "tag");
                            var pg = testMod.getPositions();

                            pg.put(tag, new ContainerAccessPosition());
                            var pos = pg.get(tag);
                            var t = "position added ["+ tag + "]\n" +
                                    "-pos: " +
                                    Utils.convertVec3dToVec3i(pos.pos) +
                                    "\n-target: " +
                                    Utils.convertVec3dToVec3i(pos.targetPos);

                            context.getSource().getPlayer().sendMessage(Text.of(t));
                            return 1;
                        })
                        .then(literal("filtered")
                                .executes(context -> {
                                    var testMod = TestMod.getInstance();
                                    var tag = StringArgumentType.getString(context, "tag");
                                    var pg = testMod.getPositions();
                                    var pos = new ContainerAccessPosition();
                                    pos.doFilter = true;
                                    pg.put(tag, pos);
                                    var t = "position added ["+ tag + "]\n" +
                                            "-pos: " +
                                            Utils.convertVec3dToVec3i(pos.pos) +
                                            "\n-target: " +
                                            Utils.convertVec3dToVec3i(pos.targetPos);

                                    context.getSource().getPlayer().sendMessage(Text.of(t));
                                    return 1;
                                })
                        )
                );
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildPositionRemoveCommand() {
        return literal("remove")
                .then(argument("tag", StringArgumentType.string())
                        .executes(context -> {
                            var tag = StringArgumentType.getString(context, "tag");
                            var pg = TestMod.getInstance().getPositions();
                            pg.remove(tag);
                            var t = "position ["+ tag + "] removed";
                            context.getSource().getPlayer().sendMessage(Text.of(t));
                            return 1;
                        })
                );
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildPositionGetCommand() {
        return literal("get")
                .then(argument("tag", StringArgumentType.string())
                        .executes(context -> {
                            var tag = StringArgumentType.getString(context, "tag");
                            var pg = TestMod.getInstance().getPositions();
                            var pos = pg.get(tag);
                            if (pos == null) {
                                context.getSource().getPlayer().sendMessage(Text.of("position ["+ tag + "] not found"));
                                return 1;
                            }
                            var t = "position ["+ tag + "]\n" +
                                    "pos: " +
                                    Utils.convertVec3dToVec3i(pos.pos) +
                                    "\ntarget: " +
                                    Utils.convertVec3dToVec3i(pos.targetPos);
                            context.getSource().getPlayer().sendMessage(Text.of(t));
                            return 1;
                        })
                );
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildSortPlayerCommand() {
        return literal("sortPlayer")
                .executes(context -> {
                    context.getSource().getPlayer().sendMessage(Text.of("sortPlayer"));
                    TestMod.getInstance().actionSortPlayer(TestMod.getInstance().getPositions().values().stream().toList());
                    TestMod.getInstance().start();
                    return 1;
                });
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildSortContainers() {
        return literal("sortContainers")
                .executes(context -> {
                    context.getSource().getPlayer().sendMessage(Text.of("sortContainers"));
                    TestMod.getInstance().actionSortPositionsContent(TestMod.getInstance().getPositions().values().stream().toList());
                    TestMod.getInstance().start();
                    return 1;
                });
    }

//    private LiteralArgumentBuilder<FabricClientCommandSource> buildAddActionCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
//        return literal("add")
//                .then(literal("closeContainer")
//                        .executes(context -> {
//                            context.getSource().getPlayer().sendMessage(Text.of("action addCloseContainer"));
//                            TestMod.getInstance().getActions().add(() -> TestMod.getInstance().actionCloseContainer());
//                            return 1;
//                        })
//                )
//                .then(literal("goto")
//                        .then(argument("tag", StringArgumentType.string())
//                                .executes(context -> {
//                                    var tag = StringArgumentType.getString(context, "tag");
//                                    var pg = TestMod.getInstance().getPositionRegister();
//                                    var pos = pg.get(tag);
//                                    if (pos == null) {
//                                        context.getSource().getPlayer().sendMessage(Text.of("position ["+ tag + "] not found"));
//                                        return 1;
//                                    }
//                                    context.getSource().getPlayer().sendMessage(Text.of("action addGoto " + tag));
//                                    TestMod.getInstance().getActions().add(() -> TestMod.getInstance().actionGoto(pos));
//                                    return 1;
//                                })
//                        )
//                )
//                .then(literal("interactBlock")
//                        .executes(context -> {
//                            context.getSource().getPlayer().sendMessage(Text.of("action addInteractBlock"));
//                            TestMod.getInstance().getActions().add(() -> TestMod.getInstance().actionInteractBlock());
//                            return 1;
//                        })
//                )
//                .then(literal("wait")
//                        .then(argument("time", IntegerArgumentType.integer())
//                                .executes(context -> {
//                                    var time = IntegerArgumentType.getInteger(context, "time");
//                                    context.getSource().getPlayer().sendMessage(Text.of("action addWait " + time));
//                                    TestMod.getInstance().getActions().add(() -> TestMod.getInstance().actionWait(time));
//                                    return 1;
//                                })
//                        )
//                )
//                .then(literal("depositAll")
//                        .executes(context -> {
//                            context.getSource().getPlayer().sendMessage(Text.of("action addDepositAll"));
//                            TestMod
//                                    .getInstance()
//                                    .getActions()
//                                    .add(() -> TestMod
//                                            .getInstance()
//                                            .getContainerInteractionManager()
//                                            .transferAll(ContainerInteractionManager.TRANSFER_DIRECTION.PLAYER_TO_CONTAINER));
//                            return 1;
//                        })
//                )
//                .then(literal("takeAll")
//                        .executes(context -> {
//                            context.getSource().getPlayer().sendMessage(Text.of("action addTakeAll"));
//                            TestMod
//                                    .getInstance()
//                                    .getActions()
//                                    .add(() -> TestMod
//                                            .getInstance()
//                                            .getContainerInteractionManager()
//                                            .transferAll(ContainerInteractionManager.TRANSFER_DIRECTION.CONTAINER_TO_PLAYER));
//                            return 1;
//                        })
//                )
//                .then(literal("sort")
//                        .executes(context -> {
//                            context.getSource().getPlayer().sendMessage(Text.of("action addSort"));
//                            TestMod.getInstance().actionSortPositionsContent(TestMod.getInstance().getPositionRegister().getPositions());
//                            return 1;
//                        })
//                );
//
//    }
//
//    private LiteralArgumentBuilder<FabricClientCommandSource> buildActionCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
//        return literal("action")
//                .then(literal("list")
//                        .executes(context -> {
//                            var t = "action list : \n";
//                            t += TestMod.getInstance().actionsToString();
//
//                            context.getSource().getPlayer().sendMessage(Text.of(t));
//                            return 1;
//                        })
//                )
//                .then(literal("clear")
//                        .executes(context -> {
//                            context.getSource().getPlayer().sendMessage(Text.of("Clear actions"));
//                            TestMod.getInstance().getActions().clear();
//                            return 1;
//                        })
//                )
//                .then(literal("removeLast")
//                        .executes(context -> {
//                            context.getSource().getPlayer().sendMessage(Text.of("Remove last action"));
//                            TestMod.getInstance().getActions().remove(TestMod.getInstance().getActions().size() - 1);
//                            return 1;
//                        })
//                )
//                .then(buildAddActionCommand(dispatcher));
//    }



}


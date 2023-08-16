package net.sombrage.testmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.sombrage.testmod.TestMod;
import net.sombrage.testmod.actions.*;
import net.sombrage.testmod.utils.Utils;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientCommandManager {

    public ClientCommandManager() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));

    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("start")
                .executes(context -> {
                    context.getSource().getPlayer().sendMessage(Text.of("Start"));
                    TestMod.getInstance().start();
                    return 1;
                })
        );

        dispatcher.register(literal("stop")
                .executes(context -> {
                    context.getSource().getPlayer().sendMessage(Text.of("Stop"));
                    TestMod.getInstance().stop();
                    return 1;
                })
        );

        dispatcher.register(buildPositionCommand(dispatcher));

        dispatcher.register(buildActionCommand(dispatcher));

    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildPositionCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        return literal("position")
                .then(literal("list")
                        .executes(context -> {
                            var pg = TestMod.getInstance().getPositionRegister();
                            var t = "position list : \n";
                            for (var tag : pg.getTags()) {
                                t += tag + " \n";
                            }
                            context.getSource().getPlayer().sendMessage(Text.of(t));
                            return 1;
                        })
                )
                .then(literal("save")
                        .executes(context -> {
                            context.getSource().getPlayer().sendMessage(Text.of("Save positions"));
                            TestMod.getInstance().getPositionRegister().saveToCsv();
                            return 1;
                        })
                )
                .then(literal("clear")
                        .executes(context -> {
                            context.getSource().getPlayer().sendMessage(Text.of("Clear positions"));
                            TestMod.getInstance().getPositionRegister().clear();
                            return 1;
                        })
                )
                .then(literal("add")
                        .then(argument("tag", StringArgumentType.string())
                                .executes(context -> {
                                    var tag = StringArgumentType.getString(context, "tag");
                                    var pg = TestMod.getInstance().getPositionRegister();
                                    pg.addFromPlayer(tag);
                                    var pos = pg.get(tag);
                                    var t = "position added ["+ tag + "]\n" +
                                            "-pos: " +
                                            Utils.convertVec3dToVec3i(pos.pos) +
                                            "\n-target: " +
                                            Utils.convertVec3dToVec3i(pos.targetPos);

                                    context.getSource().getPlayer().sendMessage(Text.of(t));
                                    return 1;
                                })
                        )
                )
                .then(literal("remove")
                        .then(argument("tag", StringArgumentType.string())
                                .executes(context -> {
                                    var tag = StringArgumentType.getString(context, "tag");
                                    var pg = TestMod.getInstance().getPositionRegister();
                                    pg.remove(tag);
                                    var t = "position ["+ tag + "] removed";
                                    context.getSource().getPlayer().sendMessage(Text.of(t));
                                    return 1;
                                })
                        )
                )
                .then(literal("get")
                        .then(argument("tag", StringArgumentType.string())
                                .executes(context -> {
                                    var tag = StringArgumentType.getString(context, "tag");
                                    var pg = TestMod.getInstance().getPositionRegister();
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
                        )
                );

    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildAddActionCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        return literal("add")
                .then(literal("closeContainer")
                        .executes(context -> {
                            context.getSource().getPlayer().sendMessage(Text.of("action addCloseContainer"));
                            TestMod.getInstance().getActionList().add(new CloseContainerAction());
                            return 1;
                        })
                )
                .then(literal("goto")
                        .then(argument("tag", StringArgumentType.string())
                                .executes(context -> {
                                    var tag = StringArgumentType.getString(context, "tag");
                                    var pg = TestMod.getInstance().getPositionRegister();
                                    var pos = pg.get(tag);
                                    if (pos == null) {
                                        context.getSource().getPlayer().sendMessage(Text.of("position ["+ tag + "] not found"));
                                        return 1;
                                    }
                                    context.getSource().getPlayer().sendMessage(Text.of("action addGoto " + tag));
                                    TestMod.getInstance().getActionList().add(new GotoAction(pos));
                                    return 1;
                                })
                        )
                )
                .then(literal("interactBlock")
                        .executes(context -> {
                            context.getSource().getPlayer().sendMessage(Text.of("action addInteractBlock"));
                            TestMod.getInstance().getActionList().add(new InteractBlockAction());
                            return 1;
                        })
                )
                .then(literal("wait")
                        .then(argument("time", IntegerArgumentType.integer())
                                .executes(context -> {
                                    var time = IntegerArgumentType.getInteger(context, "time");
                                    context.getSource().getPlayer().sendMessage(Text.of("action addWait " + time));
                                    TestMod.getInstance().getActionList().add(new WaitAction(time));
                                    return 1;
                                })
                        )
                )
                .then(literal("depositAll")
                        .executes(context -> {
                            context.getSource().getPlayer().sendMessage(Text.of("action addDepositAll"));
                            TestMod.getInstance().getActionList().add(new DepositAllAction());
                            return 1;
                        })
                )
                .then(literal("takeAll")
                        .executes(context -> {
                            context.getSource().getPlayer().sendMessage(Text.of("action addTakeAll"));
                            TestMod.getInstance().getActionList().add(new TakeAllAction());
                            return 1;
                        })
                );
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> buildActionCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        return literal("action")
                .then(literal("list")
                        .executes(context -> {
                            var t = "action list : \n";
                            int i = 1;
                            for(var action : TestMod.getInstance().getActionList()) {
                                t += i + " " + action.getClass().getSimpleName() + "\n";
                                i++;
                            }

                            context.getSource().getPlayer().sendMessage(Text.of(t));
                            return 1;
                        })
                )
                .then(literal("clear")
                        .executes(context -> {
                            context.getSource().getPlayer().sendMessage(Text.of("Clear actions"));
                            TestMod.getInstance().getActionList().clear();
                            return 1;
                        })
                )
                .then(literal("removeLast")
                        .executes(context -> {
                            context.getSource().getPlayer().sendMessage(Text.of("Remove last action"));
                            TestMod.getInstance().getActionList().remove();
                            return 1;
                        })
                )
                .then(buildAddActionCommand(dispatcher));
    }



}


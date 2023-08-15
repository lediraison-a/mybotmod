package net.sombrage.testmod;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.text.Text;
import net.sombrage.testmod.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public class ExampleMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("testmod");


	public static final String MOD_ID = "testmod";

	@Override
	public void onInitialize() {


		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
		LOGGER.info("Hello Fabric world!");
	}

	private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(literal("registerPosition")
				.then(argument("tag", StringArgumentType.string())
						.executes(context -> {
							var tag = StringArgumentType.getString(context, "tag");
							var pg = TestMod.getInstance().getPositionRegister();
							pg.addFromPlayer(tag);
							var t = "position ["+ tag + "]" + Utils.convertVec3dToVec3i(pg.get(tag).pos);
							context.getSource().getPlayer().sendMessage(Text.of(t));
							return 1;
						})
				)
		);


		dispatcher.register(literal("start")
				.executes(context -> {
					context.getSource().getPlayer().sendMessage(Text.of("Start"));
					TestMod.getInstance().start();
					return 1;
				})

		);

	}

}
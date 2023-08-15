package net.sombrage.testmod;


import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public class ExampleMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("testmod");

	private TestMod testMod;

	public static final String MOD_ID = "testmod";

	@Override
	public void onInitialize() {


		testMod = new TestMod();




		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
		LOGGER.info("Hello Fabric world!");
	}

	private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(literal("registerDump")
				.executes(context -> {
					testMod.setDumpPos();
					var t = "dump position " + testMod.getDumpPos().pos.toString();
					context.getSource().getPlayer().sendMessage(Text.of(t));
					return 1;
				})

		);


		dispatcher.register(literal("start")
				.executes(context -> {
					context.getSource().getPlayer().sendMessage(Text.of("Start"));
					testMod.start();
					return 1;
				})

		);

	}

}
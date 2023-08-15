package net.sombrage.testmod;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.text.Text;
import net.sombrage.testmod.command.ClientCommandManager;
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

		new ClientCommandManager();
		var testMod = TestMod.getInstance();
		LOGGER.info("Hello Fabric world!");
	}


}
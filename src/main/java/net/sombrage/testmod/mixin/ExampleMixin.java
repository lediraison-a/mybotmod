package net.sombrage.testmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ExampleMixin {

	@Inject(method = "closeHandledScreen", at = @At("HEAD"))
	public void onCloseHandledScreen(CallbackInfo ci) {
		// Your code here
		// MinecraftClient.getInstance().player.sendMessage(Text.of("Player closed a container!"), false);
	}
}


//@Mixin(MinecraftServer.class)
//public class ExampleMixin {
//	@Inject(at = @At("HEAD"), method = "loadWorld")
//	private void init(CallbackInfo info) {
//		System.out.println("Test Mixin");
//		// This code is injected into the start of MinecraftServer.loadWorld()V
//
//		// send hello message to player
//		 var client = MinecraftClient.getInstance();
//		 client.player.sendMessage(Text.of("Hello from TestMod!"), false);
//	}
//}
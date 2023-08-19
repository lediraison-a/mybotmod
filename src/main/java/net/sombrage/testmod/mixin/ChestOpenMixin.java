package net.sombrage.testmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class ChestOpenMixin {
    @Inject(at = @At("HEAD"), method = "openHandledScreen")
    private void onOpenHandledScreen(NamedScreenHandlerFactory namedScreenHandlerFactory, CallbackInfoReturnable callbackInfoReturnable) {
    }

}
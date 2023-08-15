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
        System.out.println("Test");
        MinecraftClient.getInstance().player.sendMessage(Text.of("Test"));
        takeAllItems();
    }


    private void takeAllItems() {
        PlayerInventory playerInv = MinecraftClient.getInstance().player.getInventory();

        var container = MinecraftClient.getInstance().player.currentScreenHandler;

        for (int slot = 0; slot < container.getStacks().size(); slot++) {
            // This simulates a click on a slot. Parameters are: slot ID, mouse button (0 = left, 1 = right), type of click (quick or normal), and the player.
            MinecraftClient.getInstance().interactionManager.clickSlot(container.syncId, slot, 0, SlotActionType.PICKUP, MinecraftClient.getInstance().player);

            // This moves the item into the player's inventory.
            MinecraftClient.getInstance().interactionManager.clickSlot(container.syncId, playerInv.selectedSlot + 36, 0, SlotActionType.PICKUP, MinecraftClient.getInstance().player);
        }
    }
}
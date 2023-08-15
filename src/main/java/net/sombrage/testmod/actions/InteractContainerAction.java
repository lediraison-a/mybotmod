package net.sombrage.testmod.actions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.sombrage.testmod.TestMod;

public class InteractContainerAction implements IMyAction {
    @Override
    public TestMod.STATUS run() {
        var client = MinecraftClient.getInstance();
        if (client.crosshairTarget instanceof BlockHitResult) {
            BlockHitResult blockHit = (BlockHitResult) client.crosshairTarget;
            var ar = client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
        }
        return TestMod.STATUS.IDLE;
    }
}

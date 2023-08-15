package net.sombrage.testmod.actions;

import net.minecraft.client.MinecraftClient;
import net.sombrage.testmod.TestMod;

public class CloseContainerAction implements IMyAction{
    @Override
    public TestMod.STATUS run() {
        var client = MinecraftClient.getInstance();
        if (client.currentScreen != null)
            client.currentScreen.close();
        client.player.closeHandledScreen();
        return TestMod.STATUS.IDLE;
    }

    @Override
    public boolean playNextAction() {
        return true;
    }
}

package net.sombrage.testmod.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class DelayedTaskScheduler {
    private Runnable delayedTask = null;
    private int ticksRemaining = 0;

    public DelayedTaskScheduler() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    public void scheduleTask(Runnable task, int delayTicks) {
        delayedTask = task;
        ticksRemaining = delayTicks;
    }

    private void onEndTick(MinecraftClient client) {
        if (delayedTask != null) {
            if (ticksRemaining <= 0) {
                delayedTask.run();
                delayedTask = null;
            } else {
                ticksRemaining--;
            }
        }
    }
}

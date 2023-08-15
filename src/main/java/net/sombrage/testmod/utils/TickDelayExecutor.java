package net.sombrage.testmod.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.sombrage.testmod.MyBaritoneEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TickDelayExecutor {
    public static final Logger LOGGER = LoggerFactory.getLogger(TickDelayExecutor.class);
    private Runnable delayedTask = null;
    private int ticksRemaining = 0;

    public TickDelayExecutor() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    public void scheduleTask(Runnable task, int delayTicks) {
        if (delayedTask != null) {
            return;
        }
        delayedTask = task;
        ticksRemaining = delayTicks;
    }

    private void onEndTick(MinecraftClient client) {
        if (delayedTask != null) {
            LOGGER.info("tick" + ticksRemaining);
            if (ticksRemaining <= 0) {
                delayedTask.run();
                delayedTask = null;
            } else {
                ticksRemaining--;
            }
        }
    }
}

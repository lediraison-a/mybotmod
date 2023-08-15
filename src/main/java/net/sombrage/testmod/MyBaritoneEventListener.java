package net.sombrage.testmod;

import baritone.api.BaritoneAPI;
import baritone.api.event.events.PathEvent;
import baritone.api.event.listener.AbstractGameEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyBaritoneEventListener implements AbstractGameEventListener {

    private Runnable runnable;

    public static final Logger LOGGER = LoggerFactory.getLogger(MyBaritoneEventListener.class);

    public MyBaritoneEventListener(Runnable runnable) {
        this.runnable = runnable;
        BaritoneAPI.getProvider().getPrimaryBaritone().getGameEventHandler().registerEventListener(this);
    }
    @Override
    public void onPathEvent(PathEvent pathEvent) {
        LOGGER.info("event : " + pathEvent);
        if (pathEvent.equals(PathEvent.CANCELED)) {
            runnable.run();
        }
    }
}

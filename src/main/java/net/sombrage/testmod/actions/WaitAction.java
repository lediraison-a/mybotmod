package net.sombrage.testmod.actions;

import net.sombrage.testmod.TestMod;
public class WaitAction implements IMyAction {
    private int ticks;

    public WaitAction(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public TestMod.STATUS run() {
        return TestMod.STATUS.WAITING;
    }

    public boolean isDone() {
        ticks--;
        return ticks == 0;
    }

    @Override
    public boolean playNextAction() {
        return false;
    }

}

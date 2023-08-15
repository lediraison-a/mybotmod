package net.sombrage.testmod.actions;

import net.sombrage.testmod.TestMod;
import net.sombrage.testmod.utils.ActionExecutionException;

public class TakeSlotAction implements IMyAction {

    private int containerSlot;

    public TakeSlotAction(int containerSlot) {
        this.containerSlot = containerSlot;
    }

    @Override
    public TestMod.STATUS run() throws ActionExecutionException {
        var ci = TestMod.getInstance().getContainerInteractionManager();
        ci.pickup(containerSlot);
        var playerEmptySlot = ci.getFirstPlayerEmptySlot();
        if(playerEmptySlot == -1) {
            return TestMod.STATUS.IDLE;
        }
        ci.pickup(playerEmptySlot);
        return TestMod.STATUS.INTERACTING;
    }

    @Override
    public boolean playNextAction() {
        return true;
    }
}

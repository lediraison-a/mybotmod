package net.sombrage.testmod.actions;

import net.sombrage.testmod.TestMod;
import net.sombrage.testmod.utils.ActionExecutionException;

public class TakeFirstNonEmptySlotAction implements IMyAction {
    @Override
    public TestMod.STATUS run() throws ActionExecutionException {
        var ci = TestMod.getInstance().getContainerInteractionManager();
        var containerNonEmptySlot = ci.getFirstContainerNonEmptySlot();
        if(containerNonEmptySlot == -1) {
            return TestMod.STATUS.INTERACTING;
        }
        return TestMod.STATUS.INTERACTING;
    }

    @Override
    public boolean playNextAction() {
        return true;
    }
}

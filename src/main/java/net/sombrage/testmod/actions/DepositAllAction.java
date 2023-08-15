package net.sombrage.testmod.actions;

import net.sombrage.testmod.TestMod;
import net.sombrage.testmod.utils.ActionExecutionException;

public class DepositAllAction implements IMyAction {

        @Override
        public TestMod.STATUS run() throws ActionExecutionException {
            var ci = TestMod.getInstance().getContainerInteractionManager();
            ci.depositAll();
            return TestMod.STATUS.INTERACTING;
        }

        @Override
        public boolean playNextAction() {
            return true;
        }

}
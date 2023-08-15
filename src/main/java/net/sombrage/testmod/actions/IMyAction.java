package net.sombrage.testmod.actions;

import net.sombrage.testmod.TestMod;
import net.sombrage.testmod.utils.ActionExecutionException;

public interface IMyAction {

    TestMod.STATUS run() throws ActionExecutionException;

    boolean playNextAction();

}

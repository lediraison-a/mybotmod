package net.sombrage.testmod;

import java.util.List;
import java.util.concurrent.Callable;

public class ActionList {

    public List<Callable<Boolean>> actions;

    public boolean repeatActions;

    public ActionList(boolean repeatActions, List<Callable<Boolean>> actions) {
        this.repeatActions = repeatActions;
        this.actions = actions;
    }
}

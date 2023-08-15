package net.sombrage.testmod.actions;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.util.math.BlockPos;
import net.sombrage.testmod.ContainerAccessPosition;
import net.sombrage.testmod.TestMod;
import net.sombrage.testmod.utils.Utils;

public class GotoAction implements IMyAction {

    private final IBaritone baritone;

    private final ContainerAccessPosition position;

    private GoalBlock goal;

    public GotoAction(ContainerAccessPosition position) {
        baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        this.position = position;
    }

    @Override
    public TestMod.STATUS run() {
        goal = new GoalBlock(new BlockPos(Utils.convertVec3dToVec3i(position.pos)));
        baritone.getCustomGoalProcess().setGoalAndPath(goal);
        return TestMod.STATUS.PATHING;
    }

    public boolean isGoalReached() {
        if(goal.isInGoal(new BlockPos(Utils.convertVec3dToVec3i(position.pos)))) {
            baritone.getPlayerContext().player().setYaw(position.yaw);
            baritone.getPlayerContext().player().setPitch(position.pitch);
            return true;
        }
        return false;
    }

}

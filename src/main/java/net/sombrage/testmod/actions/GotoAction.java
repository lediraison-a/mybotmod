package net.sombrage.testmod.actions;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.sombrage.testmod.position.ContainerAccessPosition;
import net.sombrage.testmod.TestMod;
import net.sombrage.testmod.utils.Utils;

public class GotoAction implements IMyAction {

    private final IBaritone baritone;

    private final ContainerAccessPosition position;

    private GoalBlock goal;

    public GotoAction(ContainerAccessPosition position) {
        baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        this.position = position;
        goal = new GoalBlock(new BlockPos(Utils.convertVec3dToVec3i(position.pos)));
    }

    @Override
    public TestMod.STATUS run() {
        baritone.getCustomGoalProcess().setGoalAndPath(goal);
        return TestMod.STATUS.PATHING;
    }

    @Override
    public boolean playNextAction() {
        return false;
    }

    public boolean isGoalReached() {
        if(goal.isInGoal(new BlockPos(Utils.convertVec3dToVec3i(position.pos)))

        ) {
            baritone.getPlayerContext().player().lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, position.targetPos);
            return true;
        }
        return false;
    }

}

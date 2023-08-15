package net.sombrage.testmod.position;

import net.minecraft.util.math.Vec3d;

public class ContainerAccessPosition {

    public Vec3d pos;

    public Vec3d targetPos;


    public ContainerAccessPosition(Vec3d pos, Vec3d targetPos) {
        this.pos = pos;
        this.targetPos = targetPos;
    }
}

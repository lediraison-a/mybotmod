package net.sombrage.testmod;

import net.minecraft.util.math.Vec3d;

public class ContainerAccessPosition {

    public Vec3d pos;
    public float yaw;
    public float pitch;

    public ContainerAccessPosition(Vec3d pos, float yaw, float pitch) {
        this.pos = pos;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}

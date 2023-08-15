package net.sombrage.testmod.utils;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class Utils {

    public static Vec3i convertVec3dToVec3i(Vec3d vec) {
        return new Vec3i((int) vec.x, (int) vec.y, (int) vec.z);
    }
}

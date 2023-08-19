package net.sombrage.testmod.utils;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public class Utils {

    public static Vec3i convertVec3dToVec3i(Vec3d vec) {
        return new Vec3i((int) vec.x, (int) vec.y, (int) vec.z);
    }

    public static String listOfStringToString(List<String> list) {
        var t = "";
        for (int i = 0; i < list.size(); i++) {
            t += list.get(i);
            if (i < list.size() - 1) {
                t += ",";
            }
        }

        return t;
    }

    public static List<String> stringToListOfString(String string) {
        return List.of(string.split(","));
    }
}

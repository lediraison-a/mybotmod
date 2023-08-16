package net.sombrage.testmod.position;

import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ContainerAccessPosition {

    public Vec3d pos;

    public Vec3d targetPos;

    public List<Item> filterItems;

    public boolean doFilter;


    public ContainerAccessPosition(Vec3d pos, Vec3d targetPos) {
        this.pos = pos;
        this.targetPos = targetPos;
        filterItems = new ArrayList<>();
        doFilter = false;
    }
}

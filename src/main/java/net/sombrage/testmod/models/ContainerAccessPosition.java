package net.sombrage.testmod.models;

import net.minecraft.client.MinecraftClient;
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

    public ContainerAccessPosition() {
        var client = MinecraftClient.getInstance();
        this.pos = client.player.getPos();
        this.targetPos = client.crosshairTarget.getPos();
        filterItems = new ArrayList<>();
        doFilter = false;
    }

    public static List<ContainerAccessPosition> sortByDoFilter(List<ContainerAccessPosition> positions) {
        return positions.stream().sorted((o1, o2) -> {
            if (o1.doFilter && !o2.doFilter) {
                return 1;
            } else if (!o1.doFilter && o2.doFilter) {
                return -1;
            } else {
                return 0;
            }
        }).toList();
    }
}

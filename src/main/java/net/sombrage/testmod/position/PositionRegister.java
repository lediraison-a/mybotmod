package net.sombrage.testmod.position;

import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;

public class PositionRegister {

    private Map<String, ContainerAccessPosition> positions;

    public PositionRegister() {
        positions = new HashMap<String, ContainerAccessPosition>();
    }

    public void add(String name, ContainerAccessPosition pos) {
        positions.put(name, pos);
    }

    public ContainerAccessPosition addFromPlayer(String name) {
        var client = MinecraftClient.getInstance();

        var pos = new ContainerAccessPosition(client.player.getPos(), client.crosshairTarget.getPos());
        this.add(name, pos);
        return pos;
    }

    public ContainerAccessPosition get(String name) {
        return positions.get(name);
    }

    public void remove(String name) {
        positions.remove(name);
    }

    public void clear() {
        positions.clear();
    }

}

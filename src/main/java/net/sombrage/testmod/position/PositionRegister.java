package net.sombrage.testmod.position;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.sombrage.testmod.save.SaveManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PositionRegister {

    private Map<String, ContainerAccessPosition> positions;

    public PositionRegister() {
        positions = new HashMap<>();
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

    public ContainerAccessPosition addFromPlayerFiltered(String name) {
        var client = MinecraftClient.getInstance();
        var pos = new ContainerAccessPosition(client.player.getPos(), client.crosshairTarget.getPos());
        pos.doFilter = true;
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

    public Set<String> getTags() {
        return positions.keySet();
    }

    public void setPositions(Map<String, ContainerAccessPosition> dataMap) {
        positions = dataMap;
    }

    public void saveToCsv() {
        SaveManager.savePositionRegisterAsCsv(this.positions);
    }

    public void loadFromCsv() {
        this.positions = SaveManager.loadPositionRegisterFromCsv();
    }

    public List<ContainerAccessPosition> getPositions() {
        return List.copyOf(positions.values());
    }
}

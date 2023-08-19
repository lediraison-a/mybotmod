package net.sombrage.testmod.save;

import net.minecraft.client.MinecraftClient;
import net.sombrage.testmod.models.ContainerAccessPosition;
import net.sombrage.testmod.save.csv.CSVReaderWriter;

import java.io.File;
import java.util.Map;

public class SaveManager {

    public static void savePositionRegisterAsCsv(Map<String, ContainerAccessPosition> positions) {
        CSVReaderWriter.saveToCSV(getSaveFile("position", "csv"), positions);
    }

    public static Map<String, ContainerAccessPosition> loadPositionRegisterFromCsv() {
        return CSVReaderWriter.loadFromCSV(getSaveFile("position", "csv"));
    }


    private static File getModDirectory() {
        MinecraftClient client = MinecraftClient.getInstance();
        File modDirectory = new File(client.runDirectory, "modData");
        if (!modDirectory.exists()) {
            modDirectory.mkdirs();
        }
        return modDirectory;
    }

    private static File getSaveFile(String saveName, String type) {
        MinecraftClient client = MinecraftClient.getInstance();
        String serverAddress = client.getCurrentServerEntry().address;
        // String playerName = client.player.getEntityName();
        // String fileName = serverAddress + "." + playerName + "." + saveName + "." + type;
        String fileName = serverAddress + "." + saveName + "." + type;
        File serverSpecificDataFile = new File(getModDirectory(), fileName);
        return serverSpecificDataFile;
    }
}

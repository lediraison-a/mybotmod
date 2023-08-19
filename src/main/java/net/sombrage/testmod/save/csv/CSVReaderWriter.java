package net.sombrage.testmod.save.csv;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import net.sombrage.testmod.models.ContainerAccessPosition;
import net.sombrage.testmod.utils.Utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CSVReaderWriter {

    private static final String SEPARATOR = ";";

    public static void saveToCSV(File file, Map<String, ContainerAccessPosition> dataList) {
        try (FileWriter writer = new FileWriter(file)) {

            // Write header
            writer.append("tag,posX,posY,posZ,targetPosX,targetPosY,targetPosZ,doFilter,filterItems\n");

            for (String key : dataList.keySet()){
                ContainerAccessPosition data = dataList.get(key);
                writer.append(key).append(SEPARATOR); // Write the tag
                writer.append(String.valueOf(data.pos.x)).append(SEPARATOR);
                writer.append(String.valueOf(data.pos.y)).append(SEPARATOR);
                writer.append(String.valueOf(data.pos.z)).append(SEPARATOR);
                writer.append(String.valueOf(data.targetPos.x)).append(SEPARATOR);
                writer.append(String.valueOf(data.targetPos.y)).append(SEPARATOR);
                writer.append(String.valueOf(data.targetPos.z)).append(SEPARATOR);
                writer.append(String.valueOf(data.doFilter)).append(SEPARATOR);
                writer.append(Utils.listOfStringToString(
                        data.filterItems.stream()
                                .map(item -> String.valueOf(net.minecraft.item.Item.getRawId(item)))
                                .toList())).append(SEPARATOR);
                writer.append(String.valueOf(data.targetPos.z)).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, ContainerAccessPosition> loadFromCSV(File file) {
        Map<String, ContainerAccessPosition> dataMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            // Skip header
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(SEPARATOR);
                if (values.length >= 7) {
                    String tag = values[0];
                    Vec3d pos = new Vec3d(Double.parseDouble(values[1]),
                            Double.parseDouble(values[2]),
                            Double.parseDouble(values[3]));
                    Vec3d targetPos = new Vec3d(Double.parseDouble(values[4]),
                            Double.parseDouble(values[5]),
                            Double.parseDouble(values[6]));
                    ContainerAccessPosition data = new ContainerAccessPosition(pos, targetPos);
                    if (values.length >= 8) {
                        data.doFilter = Boolean.parseBoolean(values[7]);
                    }
                    if (values.length >= 9) {
                        data.filterItems = Utils.stringToListOfString(values[8]).stream()
                                .map(itemName -> {
                                    try {
                                        return net.minecraft.item.Item.byRawId(Integer.parseInt(itemName));
                                    } catch (Exception e) {
                                        return null;
                                    }
                                })
                                .toList();
                    }
                    dataMap.put(tag, data);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataMap;
    }
}

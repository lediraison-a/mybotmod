package net.sombrage.testmod.save.csv;

import net.minecraft.util.math.Vec3d;
import net.sombrage.testmod.position.ContainerAccessPosition;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CSVReaderWriter {

    public static void saveToCSV(File file, Map<String, ContainerAccessPosition> dataList) {
        try (FileWriter writer = new FileWriter(file)) {

            // Write header
            writer.append("tag,posX,posY,posZ,targetPosX,targetPosY,targetPosZ\n");

            for (String key : dataList.keySet()){
                ContainerAccessPosition data = dataList.get(key);
                writer.append(key).append(","); // Write the tag
                writer.append(String.valueOf(data.pos.x)).append(",");
                writer.append(String.valueOf(data.pos.y)).append(",");
                writer.append(String.valueOf(data.pos.z)).append(",");
                writer.append(String.valueOf(data.targetPos.x)).append(",");
                writer.append(String.valueOf(data.targetPos.y)).append(",");
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
                String[] values = line.split(",");
                if (values.length >= 7) {
                    String tag = values[0];
                    Vec3d pos = new Vec3d(Double.parseDouble(values[1]),
                            Double.parseDouble(values[2]),
                            Double.parseDouble(values[3]));
                    Vec3d targetPos = new Vec3d(Double.parseDouble(values[4]),
                            Double.parseDouble(values[5]),
                            Double.parseDouble(values[6]));
                    ContainerAccessPosition data = new ContainerAccessPosition(pos, targetPos);
                    dataMap.put(tag, data);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataMap;
    }
}

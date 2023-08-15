package net.sombrage.testmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerInteractionManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(ContainerInteractionManager.class);

    private final GenericContainerScreen containerScreen;

    public ContainerInteractionManager(GenericContainerScreen containerScreen) {
        this.containerScreen = containerScreen;

        LOGGER.info("-> " + containerScreen.getScreenHandler().getRows());
    }

    public void printContent() {
        var client = MinecraftClient.getInstance();
        for (Slot slot : containerScreen.getScreenHandler().slots) {
            if (slot.hasStack()) {
                String itemName = slot.getStack().getItem().getTranslationKey();
                int itemCount = slot.getStack().getCount();
                client.player.sendMessage(Text.of("Slot " + slot.id + ": " + itemName + " x" + itemCount), false);
            }
        }
    }

    public int getPlayerInventorySlot(int slotId) {
        return slotId - containerScreen.getScreenHandler().getRows() * 9;
    }

    public int getContainerInventorySlot(int inventorySlotId) {
        return inventorySlotId + containerScreen.getScreenHandler().getRows() * 9;
    }

    public void clickSlot(int slotId, int button, SlotActionType slotActionType) {
        var client = MinecraftClient.getInstance();
        client.interactionManager.clickSlot(
                containerScreen.getScreenHandler().syncId,
                slotId,
                button,
                slotActionType,
                client.player);
    }

    public void test() {
    }

    public void closeContainer() {
        var client = MinecraftClient.getInstance();
        containerScreen.close();
        client.player.closeHandledScreen();
    }
}

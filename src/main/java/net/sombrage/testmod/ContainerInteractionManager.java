package net.sombrage.testmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ContainerInteractionManager {

    public enum TRANSFER_DIRECTION {
        PLAYER_TO_CONTAINER,
        CONTAINER_TO_PLAYER;
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(ContainerInteractionManager.class);

    private final GenericContainerScreen containerScreen;

    public List<Slot> containerSlots;
    public List<Slot> playerSlots;


    private boolean ignorePlayerHandBar = true;

    public ContainerInteractionManager(GenericContainerScreen containerScreen) {
        this.containerScreen = containerScreen;

        containerSlots = containerScreen.getScreenHandler().slots.subList(0, containerScreen.getScreenHandler().getRows() * 9);

        var slotsSize = ignorePlayerHandBar ? containerScreen.getScreenHandler().slots.size() - 9 : containerScreen.getScreenHandler().slots.size();
        playerSlots = containerScreen.getScreenHandler().slots.subList(getContainerInventorySlot(0), slotsSize);

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

    public int getPlayerInventorySlot(int containerSlotId) {
        return containerSlotId - containerScreen.getScreenHandler().getRows() * 9;
    }

    public int getContainerInventorySlot(int playerSlotId) {
        return playerSlotId + containerScreen.getScreenHandler().getRows() * 9;
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

    public boolean isSlotEmpty(int slotId) {
        return !containerScreen.getScreenHandler().getSlot(slotId).hasStack();
    }

    public void pickup(int slotId) {
        clickSlot(slotId, 0, SlotActionType.PICKUP);
    }

    public void closeContainer() {
        var client = MinecraftClient.getInstance();
        containerScreen.close();
        client.player.closeHandledScreen();
    }

    public boolean canInsertAtSlot(int sourceSlotId, int targetSlotId) {
        var canStack = false;
        var targetSlot = containerScreen.getScreenHandler().getSlot(targetSlotId);
        var sourceStack = containerScreen.getScreenHandler().getSlot(sourceSlotId).getStack();
        var slotStack = targetSlot.getStack();
        if (targetSlot.canInsert(sourceStack)) {
            if (!slotStack.isEmpty()) {
                if (ItemStack.canCombine(slotStack, sourceStack) &&
                        (slotStack.getCount() + sourceStack.getCount() <= slotStack.getMaxCount())) {
                    canStack = true;
                }
            } else {
                canStack = true;
            }
        }
        return canStack;
    }

    private List<Slot> getSlotsCanTransfer(Slot sourceSlot, List<Slot> targetSlots) {
        return targetSlots.stream().filter(targetSlot -> canInsertAtSlot(sourceSlot.id, targetSlot.id)).toList();
    }

    private List<Slot> getSlotsOfItem(Item item, List<Slot> slots) {
        return slots.stream().filter(slot -> slot.getStack().getItem().equals(item)).toList();
    }

    public boolean transferAll(TRANSFER_DIRECTION transferDirection) {
        var targetSlots = transferDirection == TRANSFER_DIRECTION.PLAYER_TO_CONTAINER ?
                containerSlots :
                playerSlots;
        var sourceSlots = transferDirection == TRANSFER_DIRECTION.PLAYER_TO_CONTAINER ?
                playerSlots :
                containerSlots;

        return transferSlots(targetSlots, sourceSlots);
    }

    public boolean transferAllOf(List<Item> items, TRANSFER_DIRECTION transferDirection) {
        for (Item item : items) {
            transferAllOf(item, transferDirection);
        }
        return true;
    }

    public boolean transferAllOf(Item item, TRANSFER_DIRECTION transferDirection) {
        var targetSlots = transferDirection == TRANSFER_DIRECTION.PLAYER_TO_CONTAINER ?
                containerSlots :
                playerSlots;
        var sourceSlots = transferDirection == TRANSFER_DIRECTION.PLAYER_TO_CONTAINER ?
                playerSlots :
                containerSlots;

        var sourceSlotsOfItem = getSlotsOfItem(item, sourceSlots);
        return transferSlots(targetSlots, sourceSlotsOfItem);
    }

    public boolean transferAllExcept(Item item, TRANSFER_DIRECTION transferDirection) {
        var targetSlots = transferDirection == TRANSFER_DIRECTION.PLAYER_TO_CONTAINER ?
                containerSlots :
                playerSlots;
        var sourceSlots = transferDirection == TRANSFER_DIRECTION.PLAYER_TO_CONTAINER ?
                playerSlots :
                containerSlots;

        sourceSlots = sourceSlots.stream().filter(slot -> !slot.getStack().getItem().equals(item)).toList();
        return transferSlots(targetSlots, sourceSlots);
    }

    public boolean transferAllExcept(List<Item> items, TRANSFER_DIRECTION transferDirection) {
        for (Item item : items) {
            transferAllExcept(item, transferDirection);
        }
        return true;
    }

    private boolean transferSlots(List<Slot> targetSlots, List<Slot> sourceSlots) {
        for(Slot sourceSlot : sourceSlots) {
            var targetSlotsCanTransfer = getSlotsCanTransfer(sourceSlot, targetSlots);
            if(targetSlotsCanTransfer.isEmpty()) {
                return true;
            }
            pickup(sourceSlot.id);
            pickup(targetSlotsCanTransfer.get(0).id);
        }
        return true;
    }

    public List<Item> getAllItemTypes(List<Slot> slots) {
        return slots.stream().map(slot -> slot.getStack().getItem()).distinct().filter(item -> !item.equals(Items.AIR.asItem())).toList();
    }
}

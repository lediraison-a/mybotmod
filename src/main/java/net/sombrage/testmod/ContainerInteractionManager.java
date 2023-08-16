package net.sombrage.testmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerInteractionManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(ContainerInteractionManager.class);

    private final GenericContainerScreen containerScreen;

    private boolean ignorePlayerHandBar = true;

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

    public int getNbNonEmptyContainer() {
        //return containerScreen.getScreenHandler().slots.stream().filter(slot -> !slot.hasStack()).toArray().length;
        int nbNonEmptyContainer = 0;
        for (int i = 0; i < containerScreen.getScreenHandler().getRows() * 9; i++) {
            if (!isSlotEmpty(i)) {
                nbNonEmptyContainer++;
            }
        }
        return nbNonEmptyContainer;
    }

    public int getNbNonEmptyPlayer() {
        int nbNonEmptyPlayer = 0;
        int nbSlot = ignorePlayerHandBar ?
                containerScreen.getScreenHandler().slots.size() - 9 :
                containerScreen.getScreenHandler().slots.size();
        for (int i = getContainerInventorySlot(0); i < nbSlot ; i++) {
            if (!isSlotEmpty(i)) {
                nbNonEmptyPlayer++;
            }
        }
        return nbNonEmptyPlayer;
    }

    public void pickupAll() {
        int nbNonEmptyContainer = getNbNonEmptyContainer();
        if (nbNonEmptyContainer == 0) {
            return;
        }
        for(int i = 0; i < nbNonEmptyContainer; i++) {
            var containerNonEmptySlot = getFirstContainerNonEmptySlot();
            if(containerNonEmptySlot == -1) {
                return;
            }
            pickup(containerNonEmptySlot);
            var playerEmptySlot = getFirstPlayerEmptySlot();
            if(playerEmptySlot == -1) {
                return;
            }
            pickup(playerEmptySlot);
        }
    }

    public void depositAll() {
        int nbNonEmptyPlayer = getNbNonEmptyPlayer();
        if (nbNonEmptyPlayer == 0) {
            return;
        }
        for(int i = 0; i < nbNonEmptyPlayer; i++) {
            var playerNonEmptySlot = getFirstPlayerNonEmptySlot();
            if(playerNonEmptySlot == -1) {
                return;
            }
            pickup(playerNonEmptySlot);
            var containerEmptySlot = getFirstContainerEmptySlot();
            if(containerEmptySlot == -1) {
                return;
            }
            pickup(containerEmptySlot);
        }
    }

    public int getFirstPlayerEmptySlot() {
        int nbSlot = ignorePlayerHandBar ?
                containerScreen.getScreenHandler().slots.size() - 9 :
                containerScreen.getScreenHandler().slots.size();
        for (int i = getContainerInventorySlot(0); i < nbSlot ; i++) {
            if (isSlotEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    public int getFirstPlayerNonEmptySlot() {
        int nbSlot = ignorePlayerHandBar ?
                containerScreen.getScreenHandler().slots.size() - 9 :
                containerScreen.getScreenHandler().slots.size();
        for (int i = getContainerInventorySlot(0); i < nbSlot ; i++) {
            if (!isSlotEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    public int getFirstContainerEmptySlot() {
        for (int i = 0; i < containerScreen.getScreenHandler().getRows() * 9; i++) {
            if (isSlotEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    public int getFirstContainerNonEmptySlot() {
        for (int i = 0; i < containerScreen.getScreenHandler().getRows() * 9; i++) {
            if (!isSlotEmpty(i)) {
                return i;
            }
        }
        return -1;
    }

    public void closeContainer() {
        var client = MinecraftClient.getInstance();
        containerScreen.close();
        client.player.closeHandledScreen();
    }

    public int getPlayerFirstSlotOf(Item item) {
        int nbSlot = ignorePlayerHandBar ?
                containerScreen.getScreenHandler().slots.size() - 9 :
                containerScreen.getScreenHandler().slots.size();
        for (int i = getContainerInventorySlot(0); i < nbSlot ; i++) {
            if (containerScreen.getScreenHandler().slots.get(i).getStack().getItem().equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public int getContainerFirstSlotOf(Item item) {
        for (int i = 0; i < containerScreen.getScreenHandler().getRows() * 9; i++) {
            if (containerScreen.getScreenHandler().slots.get(i).getStack().getItem().equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public boolean canInsertAtContainerSlot(int playerSlotId, int containerSlotId) {
        var canStack = false;
        var targetSlot = containerScreen.getScreenHandler().getSlot(containerSlotId);
        var playerStack = containerScreen.getScreenHandler().getSlot(playerSlotId).getStack();
        var slotStack = targetSlot.getStack();
        if (targetSlot.canInsert(playerStack)) {
            if (!slotStack.isEmpty()) {
                if (ItemStack.canCombine(slotStack, playerStack) &&
                        (slotStack.getCount() + playerStack.getCount() <= slotStack.getMaxCount())) {
                    canStack = true;
                }
            } else {
                canStack = true;
            }
        }
        return canStack;
    }


    public void TakeAllOf(Item item) {


    }
}

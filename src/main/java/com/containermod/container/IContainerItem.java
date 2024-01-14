package com.containermod.container;

import com.containermod.container.inventory.InventoryItem;
import net.minecraft.item.ItemStack;

public interface IContainerItem {
    InventoryItem getInventory(ItemStack stack);
}

package com.containermod;

import com.containermod.container.IContainerItem;
import com.containermod.container.inventory.InventoryItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemSwordExample extends ItemSword implements IContainerItem {
    public ItemSwordExample(ToolMaterial material) {
        super(material);
        setRegistryName("testSword");
        setUnlocalizedName("testSword");
        setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote)
            return;
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        String maxItemStack = "maxItemStack";
        if (!stack.getTagCompound().hasKey(maxItemStack))
            stack.getTagCompound().setInteger(maxItemStack, 128);

    }

    //Esse método recebe um ItemStack em vez de usarmos campo de objeto nessa classe do tipo InventoryItem porque no Minecraft, em vez de criar várias instancias do mesmo item, eles usam uma única instância para cada tipo. Se tivéssemos um campo comum, todos os testsword no jogo teriam o mesmo Inventory o que não queremos, ao inves disso Recebendo um ItemStack, podemos lidar com cada item individualmente, mantendo suas características únicas, e lembrando essa classe não é um ItemStack que representa os diferentes itens no jogo ela é um Item que como falado ja representa todos os itens desse tipo no jogo.
    @Override
    public InventoryItem getInventory(ItemStack stack) {
        return new InventoryItem(stack);
    }
}

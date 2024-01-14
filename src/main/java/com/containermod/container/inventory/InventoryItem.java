package com.containermod.container.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

//Esta classe representa o inventario do item, usada para manipulação do inventario
public class InventoryItem implements IInventory {
    private ItemStack stack;
    private NonNullList<ItemStack> itemsInInventory = NonNullList.create();

    public InventoryItem(ItemStack stack) {
        if (stack == null)
            throw new IllegalArgumentException("stack passado para o construtor do inventario é null");
        this.stack = stack;
    }

    @Override
    public int getSizeInventory() {
        return 54; //quantidade de slots que definimos para nosso inventario pela generic.png
    }

    //verifica se o inventario esta vazio ou não
    @Override
    public boolean isEmpty() {
        for (ItemStack item : itemsInInventory) {
            if (!item.isEmpty())
                return false;
        }
        return true;
    }

    @Override //usado para obter um item especifico do inventario
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < getSizeInventory() ? itemsInInventory.get(index) : ItemStack.EMPTY;
    }

    @Override
    //acionado quando clicamos na pilha de itens o que retira ela do inventario, o retorno desse item é a quantidade de itens que o player pegou ao clicar na pilha.
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(itemsInInventory, index, count);
    }

    @Override //usado para removermos um item do inventario
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemStack = itemsInInventory.get(index);
        if (itemStack.isEmpty())
            return ItemStack.EMPTY;
        else
            itemsInInventory.set(index, ItemStack.EMPTY);
        return itemStack;
    }

    @Override
    //Acionado sempre que uma pilha de items no inventario sofre modificação, como ficar maior ou menor ou ficando vazia ou ficando cheia.
    public void setInventorySlotContents(int index, ItemStack stack) {
        itemsInInventory.set(index, stack);
        int inventoryStackLimit = this.getInventoryStackLimit();
        if (!stack.isEmpty() && stack.getCount() > inventoryStackLimit) {
            stack.setCount(inventoryStackLimit);
        }
    }

    @Override
    //Este método é usado para definir qual o limite que esse inventario suporta para um ItemStack uma pilha
    public int getInventoryStackLimit() {
        return stack.getTagCompound().getInteger("maxItemStack");
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    //Este método é usado por baus para carregar texturas e modelos mas pode ser usado para outras coisas no nosso caso usaremos ele
    // para colocar os itens do stack para o inventario
    @Override
    public void openInventory(EntityPlayer player) {
        if (stack.isEmpty())
            return;
        itemsInInventory.clear();
        NBTTagCompound nbtOfStack;
        if (stack.hasTagCompound())
            nbtOfStack = stack.getTagCompound();
        else {
            nbtOfStack = new NBTTagCompound();
            stack.setTagCompound(nbtOfStack);
        }
        itemsInInventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        loadAllItems(nbtOfStack, itemsInInventory);
    }

    public static void loadAllItems(NBTTagCompound tag, NonNullList<ItemStack> list) {
        NBTTagList nbttaglist = tag.getTagList("Items", 10);
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            if (j < list.size()) {
                ItemStack stack = new ItemStack(nbttagcompound);
                stack.setCount(nbttagcompound.getInteger("Count"));
                list.set(j, stack);
            }
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (stack.isEmpty())
            return;
        NBTTagCompound nbt;
        if (stack.hasTagCompound()) {
            nbt = stack.getTagCompound();
        } else {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        saveAllItems(nbt, itemsInInventory, false);

    }

    public NBTTagCompound saveAllItems(NBTTagCompound tag, NonNullList<ItemStack> list, boolean saveEmpty) {
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = list.get(i);
            if (!itemstack.isEmpty()) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                itemstack.writeToNBT(nbttagcompound);
                nbttagcompound.removeTag("Count");
                nbttagcompound.setInteger("Count", itemstack.getCount());
                nbttaglist.appendTag(nbttagcompound);
            }
        }
        if (!nbttaglist.hasNoTags() || saveEmpty) {
            tag.setTag("Items", nbttaglist);
        }
        return tag;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        itemsInInventory.clear();
    }

    @Override //usado normalmente para definir o nome do inventario, a String retornada é a chave de tradução.
    public String getName() {
        return "inventoryItem.translate";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(this.getName());
    }
}

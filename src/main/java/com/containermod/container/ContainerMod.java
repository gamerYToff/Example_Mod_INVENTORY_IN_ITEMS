package com.containermod.container;

import com.containermod.container.inventory.InventoryItem;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Set;

/**
 * Se voce tiver duvidas pode usar como base a implementação de classes como ContainerChest, ContainerShulkerBox ou ContainerFurnace
 * Usamos a variavel {@link #slotIndexDoItemContainer} para sabermos qual o index do Item que abriu esse inventario para que no método {@link #slotClick(int, int, ClickType, EntityPlayer)} possamos impedir que o player coloque o proprio item que abriu o container dentro do container o que causaria o sumiço do item.
 * <p></p>
 * Explicação do ClickType: PICKUP_All é o tipo de clique quando o player clica duas vezes seguidas com o botão esquerdo do mouse em cima de um item fazendo todos os itens irem para aquele slot.
 * Quick move é quando usamos o shift+botão esquerdo para transferir rapidamente um item entre o inventario do player e do item
 **/
public class ContainerMod extends net.minecraft.inventory.Container {
    private final ItemStack stackInMainHandOfPlayer;
    private final InventoryItem inventoryItem;
    private int dragEvent;
    private int dragMode = -1;
    private final int slotIndexDoItemContainer;
    private final Set<Slot> dragSlots = Sets.<Slot>newHashSet();


    /**
     * Aqui nós
     *
     * @param player
     */
    public ContainerMod(EntityPlayer player) {
        super();
        stackInMainHandOfPlayer = player.getHeldItemMainhand();
        if (!(stackInMainHandOfPlayer.getItem() instanceof IContainerItem))
            throw new IllegalStateException("voce não esta com um item que suporte inventario na mão, não foi possivel abrir o inventario");
        slotIndexDoItemContainer = (short) player.inventory.currentItem;
        inventoryItem = ((IContainerItem) stackInMainHandOfPlayer.getItem()).getInventory(stackInMainHandOfPlayer);
        inventoryItem.openInventory(player);
        adicionarSlotsDoItemAoContainer();
        adicionarSlotsDoPlayerAoContainer(player);
    }

    //Esta função adiciona os slots ao container, que permitem toda a logica de interação dos itens
    //fazemos dois loops aninhados que adicionam um total de 54 slots compatiavel com a quantidade de slots na imagem
    //Nosso I é 6 porque nosso inventario representado pela imagem generic.png tem 6 slots no eixo Y ou seja verticalmente
    //Ja nosso J que usamos para definir a posição X representa o eixo horizontal que na imagem tem 9 slots então ele terá
    //o valor 9
    private void adicionarSlotsDoItemAoContainer() {
        for (int j = 0; j < 6; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(inventoryItem, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }
    }

    private void adicionarSlotsDoPlayerAoContainer(EntityPlayer player) {
        InventoryPlayer inventory = player.inventory;
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlotToContainer(new Slot(inventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + 36));
            }
        }
        adicionarSlotsDaHotbarDoPlayer(inventory);
    }

    private void adicionarSlotsDaHotbarDoPlayer(InventoryPlayer inventory) {
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(inventory, i1, 8 + i1 * 18, 161 + 36));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return !stackInMainHandOfPlayer.isEmpty();
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        inventoryItem.closeInventory(playerIn);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
    }

    @Override
    //Aqui fazemos @override porque queremos resetar nosso dragEvent e não o dragEvent da classe mãe e sim seria isos que aconteceria porque não estamos usando o mesmo dragEvent pois ele é private.
    protected void resetDrag() {
        this.dragEvent = 0;
        this.dragSlots.clear();
    }

    @Override
    //Este método é usado para transferencia de itens entre inventarios quando um clique do tipo QUICK_MOVE acontece
    //Este método retorna o proprio item do slot que foi clicado
    //o método funciona fazendo as verificações se o slot é diferente de null e se ele tem um ItemStack ou seja se o itemStack do slot não é um ItemStack.EMPTY
    // então após essas verificações ele faz uma cópia do ItemStack isso é feito porque nosso stack original sofrerá alterações por isso é feito uma cópia prévia dele mantendo o conteúdo original do slot e verifica se o index do slot é menor que o tamanho do inventario, isso existe para quando as transferecias são do inventario para o player.
    //Caso isso aconteça então ele verifica se pode mergear os itens o caso aonde ele não pode mergear os itens é quando o inventario do player esta cheio nisso o método retorna um EMPTY o que significa que nada deve acontecer.
    //Agora caso o index seja maior ele faz a verificação se pode mergear se não puder ele retorna EMPTY
    //Se ele puder mergear então o proximo if é acionado, nota após esse else if stack pode ficar com EMPTY isso porque esse stack representa a pilha de itens que foi clicado após o merge uma parte ou ela totalmente foi transferida para outro slot resultando em um decrécimo dela ou ela ficando vazia, no proximo if é verifica se ela esta vazia se estiver então é colocado um ItemStack.EMPTY nela.
    //Se o ItemStack não ficou vazio então é acionado onSlotChanged que marca isso usando markDirty do inventario do item e do player tambem
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int indexDoSlotClickado) {
        ItemStack stackOriginal = ItemStack.EMPTY;
        Slot slotClickado = inventorySlots.get(indexDoSlotClickado);
        if (slotClickado != null && slotClickado.getHasStack()) {
            ItemStack stackQueVaiSerAlterado = slotClickado.getStack();
            stackOriginal = stackQueVaiSerAlterado.copy();
            if (indexDoSlotClickado < inventoryItem.getSizeInventory()) {
                if (!mergeItemStack(stackQueVaiSerAlterado, inventoryItem.getSizeInventory(), inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(stackQueVaiSerAlterado, 0, inventoryItem.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }
            if (stackQueVaiSerAlterado.isEmpty()) {
                slotClickado.putStack(ItemStack.EMPTY);
            } else {
                slotClickado.onSlotChanged();
            }
        }
        return stackOriginal;
    }


    @Override
    //Aqui fizemos @Override por alguns motivos dentre eles.
    // 1- porque o método de Container contem uma logica que da preferencia ao limite de empilhamento do item ao inves do Container em si, por exemplo o inventario do tile entity do bau provavelmente tem limite fixo 64 mas não podemos colocar 64 bolas de neve no bau porque o maximo da bola de neve é 16.
    // 2-
    public ItemStack slotClick(int idDoSLotClickado, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (slotIndexDoItemContainer + 81 == idDoSLotClickado)
            return ItemStack.EMPTY;
        InventoryPlayer inventoryplayer = player.inventory;
        ItemStack itemstack = ItemStack.EMPTY;
        if (clickTypeIn == ClickType.QUICK_CRAFT) {
            quickCraftLogic(idDoSLotClickado, dragType, player, inventoryplayer);
        } else if (this.dragEvent != 0) {
            this.resetDrag();
        } else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)) {
            if (idDoSLotClickado == -999) {
                if (!inventoryplayer.getItemStack().isEmpty()) {
                    if (dragType == 0) {
                        player.dropItem(inventoryplayer.getItemStack(), true);
                        inventoryplayer.setItemStack(ItemStack.EMPTY);
                    }

                    if (dragType == 1) {
                        player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);
                    }
                }
            } else if (clickTypeIn == ClickType.QUICK_MOVE) {
                if (idDoSLotClickado < 0) {
                    return ItemStack.EMPTY;
                }

                Slot slotClickado = this.inventorySlots.get(idDoSLotClickado);
                if (slotClickado == null || !slotClickado.canTakeStack(player)) {
                    return ItemStack.EMPTY;
                }

                for (ItemStack itemstack7 = this.transferStackInSlot(player, idDoSLotClickado); !itemstack7.isEmpty() && ItemStack.areItemsEqual(slotClickado.getStack(), itemstack7); itemstack7 = this.transferStackInSlot(player, idDoSLotClickado)) {
                    itemstack = itemstack7.copy();
                }
            } else {
                if (idDoSLotClickado < 0) {
                    return ItemStack.EMPTY;
                }

                Slot slot6 = this.inventorySlots.get(idDoSLotClickado);

                if (slot6 != null) {
                    ItemStack itemstack8 = slot6.getStack();
                    ItemStack itemstack11 = inventoryplayer.getItemStack();

                    if (!itemstack8.isEmpty()) {
                        itemstack = itemstack8.copy();
                    }

                    if (itemstack8.isEmpty()) {
                        if (!itemstack11.isEmpty() && slot6.isItemValid(itemstack11)) {
                            int i3 = dragType == 0 ? itemstack11.getCount() : 1;

                            if (i3 > slot6.getItemStackLimit(itemstack11)) {
                                i3 = slot6.getItemStackLimit(itemstack11);
                            }

                            slot6.putStack(itemstack11.splitStack(i3));
                        }
                    } else if (slot6.canTakeStack(player)) {
                        if (itemstack11.isEmpty()) {
                            if (itemstack8.isEmpty()) {
                                slot6.putStack(ItemStack.EMPTY);
                                inventoryplayer.setItemStack(ItemStack.EMPTY);
                            } else {
                                int l2 = dragType == 0 ? itemstack8.getCount() : (itemstack8.getCount() + 1) / 2;
                                inventoryplayer.setItemStack(slot6.decrStackSize(l2));

                                if (itemstack8.isEmpty()) {
                                    slot6.putStack(ItemStack.EMPTY);
                                }

                                slot6.onTake(player, inventoryplayer.getItemStack());
                            }
                        } else if (slot6.isItemValid(itemstack11)) {
                            if (itemstack8.getItem() == itemstack11.getItem() && itemstack8.getMetadata() == itemstack11.getMetadata() && ItemStack.areItemStackTagsEqual(itemstack8, itemstack11)) {
                                //Aqui eliminamos a logica do slotClick de dar prioridade ao limite do item
                                int k2 = dragType == 0 ? itemstack11.getCount() : 1;
                                if (k2 > slot6.getItemStackLimit(itemstack11) - itemstack8.getCount()) {
                                    k2 = slot6.getItemStackLimit(itemstack11) - itemstack8.getCount();
                                }
                                itemstack11.shrink(k2);
                                itemstack8.grow(k2);
                            } else if (itemstack11.getCount() <= slot6.getItemStackLimit(itemstack11)) {
                                slot6.putStack(itemstack11);
                                inventoryplayer.setItemStack(itemstack8);
                            }
                        } else if (itemstack8.getItem() == itemstack11.getItem() && itemstack11.getMaxStackSize() > 1 && (!itemstack8.getHasSubtypes() || itemstack8.getMetadata() == itemstack11.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack8, itemstack11) && !itemstack8.isEmpty()) {
                            int j2 = itemstack8.getCount();

                            if (j2 + itemstack11.getCount() <= itemstack11.getMaxStackSize()) {
                                itemstack11.grow(j2);
                                itemstack8 = slot6.decrStackSize(j2);

                                if (itemstack8.isEmpty()) {
                                    slot6.putStack(ItemStack.EMPTY);
                                }

                                slot6.onTake(player, inventoryplayer.getItemStack());
                            }
                        }
                    }

                    slot6.onSlotChanged();
                }
            }
        } else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9) {
            swapLogic(idDoSLotClickado, dragType, player, inventoryplayer);
        } else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryplayer.getItemStack().isEmpty() && idDoSLotClickado >= 0) {
            cloneLogic(idDoSLotClickado, inventoryplayer);
        } else if (clickTypeIn == ClickType.THROW && inventoryplayer.getItemStack().isEmpty() && idDoSLotClickado >= 0) {
            throwLogic(idDoSLotClickado, dragType, player);
        } else if (clickTypeIn == ClickType.PICKUP_ALL && idDoSLotClickado >= 0) {
            pickupAllLogic(idDoSLotClickado, dragType, player, inventoryplayer);
        }

        return itemstack;
    }

    @Override
    //Este método é usado para merge dos itens quando transferStackInSlot é invocado (não é sempre que ele é invocado por transferStackInSlot, para ver quando ele é invocado veja mais informações sobre transferStackInSlot)
    //o While percorre o inventario no sentido horizontal de baixo para cima começando do ultimo slot da hotbar da direita para a esquerda, chamarei a variavel stackOriginalAserAlterado só de stackOriginal e stackAtualDoInventario que representa o stack selecionado atualmente pelo loop de stackLoop

    //Temos um if que verifica se o stackOriginal é stackable ou seja se ele é empilhavel caso sim então as seguinte logica é executada.
    // o if faz uma verificação se o stackLoop não é EMPTY seu item é igual ao item do stackOriginalAserAlterado e se o stackOriginal não tem subtipos ou seu metadado é igual ao do stackLoop e suas tags nbt são iguals
    //Dentro do if é somado a quantidade de itens no stackOriginal e no stackLoop.
    // se a quantidade for menor ou igual ao suportado pelo slot que é a quantidade maxima suportado pelo inventario, então o stackOriginal tem sua quantia reduzida a 0, stackLoop é setado e fica com a quantidade dele proprio + o que stackOriginal tinha anteriomente onSlotChanged é chamado e então a variavel booleana é definida para true indicando que foi possivel fazer o merge é definida para true
    //No else é verificado se o itemStack a pilha ao qual os itens serão transferidas não esta no limite que o slot suporta se não estiver então a quantidade de itens no stackOriginal é diminuida pela quantidade maxima que o slot suporta menos o que ele ja tem ou seja a quantidade que stackLoop ja tem então o stackLoop é setado e fica com a quantidade maxima que o slot suporta, onSlotChanged é chamado e então a variavel booleana é definida para true indicando que foi possivel fazer o merge é definida para true

    //Caso o stackOriginal não seja stackable executamos uma lógica propria que permite empilha qualquer item.
    //Um If verifica se o ItemStack não é EMPTY
    protected boolean mergeItemStack(ItemStack stackOriginalAserAlterado, int startIndex, int endIndex, boolean reverseDirection) {
        boolean mergeFoiPossivel = false;
        int i = startIndex;

        if (reverseDirection) {
            i = endIndex - 1;
        }
        while (!stackOriginalAserAlterado.isEmpty()) {
            if (reverseDirection) {
                if (i < startIndex) {
                    break;
                }
            } else if (i >= endIndex) {
                break;
            }
            Slot slot = this.inventorySlots.get(i);
            ItemStack stackAtualDoInventario = slot.getStack();
            if (!stackAtualDoInventario.isEmpty() && stackAtualDoInventario.getItem() == stackOriginalAserAlterado.getItem() && (!stackOriginalAserAlterado.getHasSubtypes() || stackOriginalAserAlterado.getMetadata() == stackAtualDoInventario.getMetadata()) && ItemStack.areItemStackTagsEqual(stackOriginalAserAlterado, stackAtualDoInventario)) {
                int j = stackAtualDoInventario.getCount() + stackOriginalAserAlterado.getCount();
                int maxSize = slot.getSlotStackLimit();
                if (j <= maxSize) {
                    stackOriginalAserAlterado.setCount(0);
                    stackAtualDoInventario.setCount(j);
                    slot.onSlotChanged();
                    mergeFoiPossivel = true;
                } else if (stackAtualDoInventario.getCount() < maxSize) {
                    stackOriginalAserAlterado.shrink(maxSize - stackAtualDoInventario.getCount());
                    stackAtualDoInventario.setCount(maxSize);
                    slot.onSlotChanged();
                    mergeFoiPossivel = true;
                }
            }
            if (reverseDirection) {
                --i;
            } else {
                ++i;
            }
        }
        if (!stackOriginalAserAlterado.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }
            while (true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }
                Slot slot1 = this.inventorySlots.get(i);
                ItemStack stackAtualDoInventario = slot1.getStack();
                if (stackAtualDoInventario.isEmpty() && slot1.isItemValid(stackOriginalAserAlterado)) {
                    if (stackOriginalAserAlterado.getCount() > slot1.getSlotStackLimit()) {
                        slot1.putStack(stackOriginalAserAlterado.splitStack(slot1.getSlotStackLimit()));
                    } else {
                        slot1.putStack(stackOriginalAserAlterado.splitStack(stackOriginalAserAlterado.getCount()));
                    }

                    slot1.onSlotChanged();
                    mergeFoiPossivel = true;
                    break;
                }
                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        return mergeFoiPossivel;
    }

    private void swapLogic(int slotId, int dragType, EntityPlayer player, InventoryPlayer inventoryplayer) {
        Slot slot4 = this.inventorySlots.get(slotId);
        ItemStack itemstack6 = inventoryplayer.getStackInSlot(dragType);
        ItemStack itemstack10 = slot4.getStack();

        if (!itemstack6.isEmpty() || !itemstack10.isEmpty()) {
            if (itemstack6.isEmpty()) {
                if (slot4.canTakeStack(player)) {
                    inventoryplayer.setInventorySlotContents(dragType, itemstack10);
                    slot4.putStack(ItemStack.EMPTY);
                    slot4.onTake(player, itemstack10);
                }
            } else if (itemstack10.isEmpty()) {
                if (slot4.isItemValid(itemstack6)) {
                    int l1 = slot4.getItemStackLimit(itemstack6);

                    if (itemstack6.getCount() > l1) {
                        slot4.putStack(itemstack6.splitStack(l1));
                    } else {
                        slot4.putStack(itemstack6);
                        inventoryplayer.setInventorySlotContents(dragType, ItemStack.EMPTY);
                    }
                }
            } else if (slot4.canTakeStack(player) && slot4.isItemValid(itemstack6)) {
                int i2 = slot4.getItemStackLimit(itemstack6);

                if (itemstack6.getCount() > i2) {
                    slot4.putStack(itemstack6.splitStack(i2));
                    slot4.onTake(player, itemstack10);

                    if (!inventoryplayer.addItemStackToInventory(itemstack10)) {
                        player.dropItem(itemstack10, true);
                    }
                } else {
                    slot4.putStack(itemstack6);
                    inventoryplayer.setInventorySlotContents(dragType, itemstack10);
                    slot4.onTake(player, itemstack10);
                }
            }
        }
    }

    private void cloneLogic(int slotId, InventoryPlayer inventoryplayer) {
        Slot slot3 = this.inventorySlots.get(slotId);

        if (slot3 != null && slot3.getHasStack()) {
            ItemStack itemstack5 = slot3.getStack().copy();
            itemstack5.setCount(itemstack5.getMaxStackSize());
            inventoryplayer.setItemStack(itemstack5);
        }
    }

    private void throwLogic(int slotId, int dragType, EntityPlayer player) {
        Slot slot2 = this.inventorySlots.get(slotId);

        if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(player)) {
            ItemStack itemstack4 = slot2.decrStackSize(dragType == 0 ? 1 : slot2.getStack().getCount());
            slot2.onTake(player, itemstack4);
            player.dropItem(itemstack4, true);
        }
    }

    private void pickupAllLogic(int slotId, int dragType, EntityPlayer player, InventoryPlayer inventoryplayer) {
        Slot slot = this.inventorySlots.get(slotId);
        ItemStack itemstack1 = inventoryplayer.getItemStack();

        if (!itemstack1.isEmpty() && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player))) {
            int i = dragType == 0 ? 0 : this.inventorySlots.size() - 1;
            int j = dragType == 0 ? 1 : -1;

            //nesse loop fazemos duas mudanças para permiti que o clique duplo puxe todos os itens e não só 64, substituimos as chamadas itemstack1.getMaxStackSize() por inventoryItem.getInventoryStackLimit();
            int inventoryStackLimit = inventoryItem.getInventoryStackLimit();
            for (int k = 0; k < 2; ++k) {
                for (int l = i; l >= 0 && l < this.inventorySlots.size() && itemstack1.getCount() < inventoryStackLimit; l += j) {
                    Slot slot1 = this.inventorySlots.get(l);

                    if (slot1.getHasStack() && canAddItemToSlot(slot1, itemstack1, true) && slot1.canTakeStack(player) && this.canMergeSlot(itemstack1, slot1)) {
                        ItemStack itemstack2 = slot1.getStack();

                        if (k != 0 || itemstack2.getCount() != itemstack2.getMaxStackSize()) {
                            int i1 = Math.min(inventoryStackLimit - itemstack1.getCount(), itemstack2.getCount());
                            ItemStack itemstack3 = slot1.decrStackSize(i1);
                            itemstack1.grow(i1);

                            if (itemstack3.isEmpty()) {
                                slot1.putStack(ItemStack.EMPTY);
                            }

                            slot1.onTake(player, itemstack3);
                        }
                    }
                }
            }
        }

        this.detectAndSendChanges();
    }

    private void quickCraftLogic(int slotId, int dragType, EntityPlayer player, InventoryPlayer inventoryplayer) {
        int j1 = this.dragEvent;
        this.dragEvent = getDragEvent(dragType);

        if ((j1 != 1 || this.dragEvent != 2) && j1 != this.dragEvent) {
            this.resetDrag();
        } else if (inventoryplayer.getItemStack().isEmpty()) {
            this.resetDrag();
        } else if (this.dragEvent == 0) {
            this.dragMode = extractDragMode(dragType);

            if (isValidDragMode(this.dragMode, player)) {
                this.dragEvent = 1;
                this.dragSlots.clear();
            } else {
                this.resetDrag();
            }
        } else if (this.dragEvent == 1) {
            Slot slot7 = this.inventorySlots.get(slotId);
            ItemStack itemstack12 = inventoryplayer.getItemStack();

            if (slot7 != null && canAddItemToSlot(slot7, itemstack12, true) && slot7.isItemValid(itemstack12) && (this.dragMode == 2 || itemstack12.getCount() > this.dragSlots.size()) && this.canDragIntoSlot(slot7)) {
                this.dragSlots.add(slot7);
            }
        } else if (this.dragEvent == 2) {
            if (!this.dragSlots.isEmpty()) {
                ItemStack itemstack9 = inventoryplayer.getItemStack().copy();
                int k1 = inventoryplayer.getItemStack().getCount();

                for (Slot slot8 : this.dragSlots) {
                    ItemStack itemstack13 = inventoryplayer.getItemStack();

                    if (slot8 != null && canAddItemToSlot(slot8, itemstack13, true) && slot8.isItemValid(itemstack13) && (this.dragMode == 2 || itemstack13.getCount() >= this.dragSlots.size()) && this.canDragIntoSlot(slot8)) {
                        ItemStack itemstack14 = itemstack9.copy();
                        int j3 = slot8.getHasStack() ? slot8.getStack().getCount() : 0;
                        computeStackSize(this.dragSlots, this.dragMode, itemstack14, j3);
                        int k3 = Math.min(itemstack14.getMaxStackSize(), slot8.getItemStackLimit(itemstack14));

                        if (itemstack14.getCount() > k3) {
                            itemstack14.setCount(k3);
                        }

                        k1 -= itemstack14.getCount() - j3;
                        slot8.putStack(itemstack14);
                    }
                }

                itemstack9.setCount(k1);
                inventoryplayer.setItemStack(itemstack9);
            }

            this.resetDrag();
        } else {
            this.resetDrag();
        }
    }
}

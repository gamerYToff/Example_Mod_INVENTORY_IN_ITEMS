package com.containermod.network;

import com.containermod.ContainerModMain;
import com.containermod.container.IContainerItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OpenContainerModPacket implements IMessage {
    private int id;

    public OpenContainerModPacket() {
    }

    public OpenContainerModPacket(int id) {
        this.id = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
    }

    public int getId() {
        return id;
    }

    public static class MessageHandler implements IMessageHandler<OpenContainerModPacket, IMessage> {

        @Override
        public IMessage onMessage(OpenContainerModPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (!player.getServer().isCallingFromMinecraftThread()) {
                player.getServer().addScheduledTask(() -> this.onMessage(message, ctx));
            } else {
                ItemStack stack = player.getHeldItemMainhand();
                boolean mainhand = true;
                if (stack.isEmpty() || !(stack.getItem() instanceof IContainerItem)) {
                    stack = ctx.getServerHandler().player.getHeldItemOffhand();
                    mainhand = false;
                }
                if (!stack.isEmpty() && stack.getItem() instanceof IContainerItem) {
                    player.openGui(ContainerModMain.instance, 0, player.world, 0, 0, 0);
                }
            }
            return null;
        }
    }
}
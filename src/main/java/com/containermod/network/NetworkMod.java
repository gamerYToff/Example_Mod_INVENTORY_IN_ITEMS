package com.containermod.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import static com.containermod.ContainerModMain.MODID;

public class NetworkMod {
    public static final SimpleNetworkWrapper INSTANCE_MOD = NetworkRegistry.INSTANCE.newSimpleChannel(MODID.toLowerCase());
    private static int index = -1;

    public static void init() {
        INSTANCE_MOD.registerMessage(OpenContainerModPacket.MessageHandler.class, OpenContainerModPacket.class, ++index, Side.SERVER);
    }
}

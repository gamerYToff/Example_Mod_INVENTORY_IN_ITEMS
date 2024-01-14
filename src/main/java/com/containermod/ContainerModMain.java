package com.containermod;

import com.containermod.network.GuiHandler;
import com.containermod.network.NetworkMod;
import com.containermod.network.OpenContainerModPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.lwjgl.input.Keyboard;

@Mod(modid = ContainerModMain.MODID, name = ContainerModMain.NAME, version = ContainerModMain.VERSION)
public class ContainerModMain {
    public static final String MODID = "containermod";
    public static final String NAME = "Container mod";
    public static final String VERSION = "1.0";
    private static KeyBinding keyBinding = new KeyBinding("KeyBinding que abri o inventario", KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_P, "Container Mod Example");
    @Mod.Instance
    public static ContainerModMain instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(instance);
        NetworkRegistry.INSTANCE.registerGuiHandler(MODID, new GuiHandler());
        NetworkMod.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(keyBinding);
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemSwordExample(Item.ToolMaterial.DIAMOND));
    }

    @SubscribeEvent
    public void openGUi(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (!(player.getHeldItemMainhand().getItem() instanceof ItemSwordExample))
            return;
        if (keyBinding.isKeyDown())
            NetworkMod.INSTANCE_MOD.sendToServer(new OpenContainerModPacket());
    }
}


package com.lumberjacksparrow.leveluprpg;

import com.lumberjacksparrow.leveluprpg.item.ItemFullRespecBook;
import com.lumberjacksparrow.leveluprpg.item.ItemRespecBook;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = LevelUpRPG.ID)
public class LevelUpRegistry {
    public static ItemRespecBook respecBook;
    public static ItemFullRespecBook respecBookFull;

    public static void init() {
        respecBook = new ItemRespecBook();
        respecBookFull = new ItemFullRespecBook();
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> evt) {
        System.out.println("DEBUG: LevelUpRPG, registering items");
        evt.getRegistry().register(respecBook);
        evt.getRegistry().register(respecBookFull);
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        registerRender(respecBook);
        registerRender(respecBookFull);
    }

    private static void registerRender(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
    }
}

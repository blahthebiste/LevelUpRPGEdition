package com.lumberjacksparrow.leveluprpg;

import com.lumberjacksparrow.leveluprpg.item.ItemClericBook;
import com.lumberjacksparrow.leveluprpg.item.ItemFullRespecBook;
import com.lumberjacksparrow.leveluprpg.item.ItemRespecBook;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

import static com.lumberjacksparrow.leveluprpg.LevelUpRPG.bookOfBenedictionEnabled;

@Mod.EventBusSubscriber(modid = LevelUpRPG.ID)
public class LevelUpRegistry {
    public static ItemRespecBook respecBook;
    public static ItemFullRespecBook respecBookFull;
    public static ItemClericBook clericBook;

    public static void init() {
        respecBook = new ItemRespecBook();
        // Only register this item if classes are enabled
        if(LevelUpRPG.allowClasses) {
            respecBookFull = new ItemFullRespecBook();
        }
        // Can be disabled in the config
        if(bookOfBenedictionEnabled) {
            clericBook = new ItemClericBook();
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> evt) {
        System.out.println("DEBUG: LevelUpRPG, registering items");
        evt.getRegistry().register(respecBook);
        // Only register this item if classes are enabled
        if(LevelUpRPG.allowClasses) {
            evt.getRegistry().register(respecBookFull);
        }
        // Can be disabled in the config
        if(bookOfBenedictionEnabled) {
            evt.getRegistry().register(clericBook);
        }
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        registerRender(respecBook);
        // Only register this item if classes are enabled
        if(LevelUpRPG.allowClasses) {
            registerRender(respecBookFull);
        }
        // Can be disabled in the config
        if(bookOfBenedictionEnabled) {
            registerRender(clericBook);
        }
    }

    private static void registerRender(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
    }
}

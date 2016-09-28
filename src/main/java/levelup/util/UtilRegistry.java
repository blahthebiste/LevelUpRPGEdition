package levelup.util;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.oredict.OreDictionary;

public class UtilRegistry {
    public static void init() {
        LootFunctionManager.registerFunction(new FortuneEnchantBonus.Serializer());
        initBlacklist();
    }

    private static void initBlacklist() {
        getOreLists();
        CraftingBlacklist.addItem(Blocks.HAY_BLOCK);
        CraftingBlacklist.addItem(Blocks.field_189878_dg);
        CraftingBlacklist.addItem(Blocks.field_189880_di);
    }

    private static void getOreLists() {
        for(String name : OreDictionary.getOreNames()) {
            if(name.startsWith("block")) {
                for(ItemStack stack : OreDictionary.getOres(name))
                    CraftingBlacklist.addItem(stack.copy());
            }
        }
    }
}

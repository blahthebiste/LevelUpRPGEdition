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
        CraftingBlacklist.addItem(Blocks.NETHER_WART_BLOCK);
        CraftingBlacklist.addItem(Blocks.BONE_BLOCK);
    }

    private static void getOreLists() {
        for(String name : OreDictionary.getOreNames()) {
            if(name.startsWith("block")) {
                for(ItemStack stack : OreDictionary.getOres(name))
                    CraftingBlacklist.addItem(stack.copy());
            }
            else if(name.startsWith("dust")) {
                for(ItemStack stack : OreDictionary.getOres(name))
                    SmeltingBlacklist.addItem(stack.copy());
            }
        }
    }
}

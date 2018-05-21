package levelup.util;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Iterator;

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

    public static void initPlankCache() {
        PlankCache.refresh();
        for (ItemStack log : OreDictionary.getOres("logWood")) {
            if (log.getItem() instanceof ItemBlock) {
                if (log.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                    for (int i = 0; i < 4; i++) {
                        ItemStack logTest = new ItemStack(log.getItem(), 1, i);
                        ItemStack plank = getPlankOutput(logTest);
                        if (!plank.isEmpty()) {
                            registerLog(logTest, plank);
                        }
                    }
                }
                else {
                    ItemStack plank = getPlankOutput(log);
                    if (!plank.isEmpty()) {
                        registerLog(log, plank);
                    }
                }
            }
        }
    }

    private static void registerLog(ItemStack log, ItemStack plank) {
        Block block = ((ItemBlock)log.getItem()).getBlock();
        ItemStack plankCopy = plank.copy();
        plankCopy.setCount(plank.getCount() / 2);
        PlankCache.addBlock(block, log.getMetadata(), plankCopy);
    }

    private static ItemStack getPlankOutput(ItemStack log) {
        Iterator<IRecipe> it = CraftingManager.REGISTRY.iterator();
        ItemStack stack = ItemStack.EMPTY;
        while (it.hasNext() && stack.isEmpty()) {
            IRecipe recipe = it.next();
            if (recipe.getGroup().equals("planks")) {
                NonNullList<Ingredient> ing = recipe.getIngredients();
                if (isPlank(recipe.getRecipeOutput())) {
                    for (Ingredient in : ing) {
                        for (ItemStack check : in.getMatchingStacks()) {
                            if (check.isItemEqual(log)) {
                                stack = recipe.getRecipeOutput().copy();
                            }
                        }
                    }
                }
            }
        }
        return stack;
    }

    private static boolean isPlank(ItemStack output) {
        for (ItemStack plank : OreDictionary.getOres("plankWood")) {
            if (plank.getMetadata() == OreDictionary.WILDCARD_VALUE)
                return output.getItem() == plank.getItem();
            else if (plank.isItemEqual(output))
                return true;
        }
        return false;
    }
}

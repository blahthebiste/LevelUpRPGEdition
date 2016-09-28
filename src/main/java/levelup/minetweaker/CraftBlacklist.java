package levelup.minetweaker;

import levelup.minetweaker.utils.BaseListAddition;
import levelup.minetweaker.utils.LogHelper;
import levelup.util.CraftingBlacklist;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.oredict.IOreDictEntry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;

@ZenClass("mods.levelup.Blacklist")
public class CraftBlacklist {
    private final static List<ItemStack> blacklist = CraftingBlacklist.getBlacklist();

    @ZenMethod
    public static void add(IIngredient input) {
        if(input instanceof IOreDictEntry) {
            for(ItemStack stack : OreDictionary.getOres(((IOreDictEntry)input).getName()))
                MineTweakerAPI.apply(new Add(stack));
        }
        else if(input instanceof IItemStack) {
            ItemStack stack = toStack((IItemStack)input);
            MineTweakerAPI.apply(new Add(stack));
        }
    }

    public static class Add extends BaseListAddition<ItemStack> {
        protected Add(ItemStack stack) {
            super("blacklist", CraftBlacklist.blacklist);
            recipes.add(new ItemStack(stack.getItem(), 1, stack.getMetadata()));
        }

        @Override
        protected String getRecipeInfo(ItemStack stack) {
            return LogHelper.getStackDescription(stack);
        }
    }

    public static ItemStack toStack(IItemStack iStack) {
        if (iStack == null) {
            return null;
        } else {
            Object internal = iStack.getInternal();
            if (!(internal instanceof ItemStack)) {
                LogHelper.logError("Not a valid item stack: " + iStack);
            }

            return (ItemStack) internal;
        }
    }
}

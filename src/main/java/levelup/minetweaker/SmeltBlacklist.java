package levelup.minetweaker;

import levelup.minetweaker.utils.BaseListAddition;
import levelup.minetweaker.utils.LogHelper;
import levelup.util.SmeltingBlacklist;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.oredict.IOreDictEntry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;

import static levelup.minetweaker.CraftBlacklist.toStack;

@ZenClass("mods.levelup.SmeltBlacklist")
public class SmeltBlacklist {
    private final static List<ItemStack> blacklist = SmeltingBlacklist.getBlacklist();

    @ZenMethod
    public static void add(IIngredient input) {
        if(input instanceof IOreDictEntry) {
            for(ItemStack stack : OreDictionary.getOres(((IOreDictEntry)input).getName()))
                MineTweakerAPI.apply(new Add(stack.copy()));
        }
        else if(input instanceof IItemStack) {
            ItemStack stack = toStack((IItemStack)input);
            MineTweakerAPI.apply(new Add(stack.copy()));
        }
    }

    public static class Add extends BaseListAddition<ItemStack> {
        protected Add(ItemStack stack) {
            super("crafting blacklist", SmeltBlacklist.blacklist);
            recipes.add(stack.copy());
        }

        @Override
        protected String getRecipeInfo(ItemStack stack) {
            return LogHelper.getStackDescription(stack);
        }
    }
}

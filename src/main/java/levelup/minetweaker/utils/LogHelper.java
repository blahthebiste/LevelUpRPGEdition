package levelup.minetweaker.utils;

import minetweaker.MineTweakerAPI;
import minetweaker.mc1102.item.MCItemStack;
import net.minecraft.item.ItemStack;

public class LogHelper
{
    public static void logError(String message) {
        MineTweakerAPI.logError("[ModTweaker] " + message);
    }

    public static void logError(String message, Throwable exception) {
        MineTweakerAPI.logError("[ModTweaker] " + message, exception);
    }

    public static void logWarning(String message) {
        MineTweakerAPI.logWarning("[ModTweaker] " + message);
    }

    public static String getStackDescription(ItemStack stack) {
        return new MCItemStack(stack).toString();
    }
}

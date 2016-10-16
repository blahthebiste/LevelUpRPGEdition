package levelup.minetweaker;

import minetweaker.MineTweakerAPI;

public class MineTweaker
{
    public static void init() {
        MineTweakerAPI.registerClass(CraftBlacklist.class);
        MineTweakerAPI.registerClass(SmeltBlacklist.class);
    }
}

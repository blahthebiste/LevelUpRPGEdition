package levelup;

import levelup.api.IProcessor;
import levelup.api.LevelUpAPI;
import levelup.capabilities.LevelUpCapability;
import levelup.event.BowEventHandler;
import levelup.event.FMLEventHandler;
import levelup.event.FightEventHandler;
import levelup.event.PlayerEventHandler;
import levelup.item.ItemRespecBook;
import levelup.player.IPlayerClass;
import levelup.player.PlayerExtendedProperties;
import levelup.proxy.SkillProxy;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.*;
import java.util.logging.Logger;

@Mod(modid = LevelUp.ID, name = "${mod_id}", version = "${version}", guiFactory = "levelup.ConfigLevelUp")
public final class LevelUp {
    public final static String ID = "levelup";
    @Instance(value = ID)
    public static LevelUp instance;
    @SidedProxy(clientSide = "levelup.proxy.SkillClientProxy", serverSide = "levelup.proxy.SkillProxy")
    public static SkillProxy proxy;
    private Property[] clientProperties;
    private Property[] serverProperties;
    public static Item xpTalisman = new Item().setCreativeTab(CreativeTabs.TOOLS).setRegistryName("levelup:xp_talisman"),
            respecBook = new ItemRespecBook().setCreativeTab(CreativeTabs.TOOLS).setRegistryName("levelup:respec_book");
    private static Map<Object, Integer> towItems;
    private static List[] tiers;
    private static Configuration config;
    public static boolean allowHUD = true, renderTopLeft = true, renderExpBar = true, changeFOV = true, oreNoPlace = true;
    private static boolean bonusMiningXP = true, bonusCraftingXP = true, bonusFightingXP = true, oreMiningXP = true;
    public static FMLEventChannel initChannel, skillChannel, classChannel, configChannel;

    @EventHandler
    public void load(FMLInitializationEvent event) {
        Logger logger = Logger.getLogger("levelup");
        logger.info("[Level Up] registering events");
        MinecraftForge.EVENT_BUS.register(BowEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FightEventHandler.INSTANCE);
        logger.info("[Level Up] registering packets");
        SkillPacketHandler sk = new SkillPacketHandler();
        initChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[0]);
        initChannel.register(sk);
        classChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[1]);
        classChannel.register(sk);
        skillChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[2]);
        skillChannel.register(sk);
        configChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[3]);
        configChannel.register(sk);
        logger.info("[Level Up] registering clientside stuff");
        proxy.registerGui();
        if(xpTalisman!=null)
            proxy.register(xpTalisman, "levelup:xp_talisman");
        if(respecBook!=null)
            proxy.register(respecBook, "levelup:respec_book");
    }

    private static ItemStack getRecipeOutput(ItemStack input) {
        Iterator<IRecipe> recipe = CraftingManager.REGISTRY.iterator();
        ItemStack output = ItemStack.EMPTY;
        while (recipe.hasNext() && output.isEmpty()) {
            if (recipe instanceof ShapelessRecipes) {
                ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
                if (shapeless.recipeItems.size() == 1 && shapeless.recipeItems.get(0).test(input)) {
                    output = shapeless.getRecipeOutput();
                }
            } else if (recipe instanceof ShapelessOreRecipe) {
                ShapelessOreRecipe shapeless = (ShapelessOreRecipe) recipe;
                if(shapeless.getIngredients().size() == 1) {
                    if(shapeless.getIngredients().get(0).test(input)) {
                        output = shapeless.getRecipeOutput();
                    }
                }
            }
        }
        return output;
    }

    @EventHandler
    public void load(FMLPreInitializationEvent event) {
        LevelUpRegistry.init();
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.addCustomCategoryComment("HUD", "Entirely client side. No need to sync.");
        initClientProperties();
        config.addCustomCategoryComment("Items", "Need to be manually synced to the client on a dedicated server");
        config.addCustomCategoryComment("Cheats", "Will be automatically synced to the client on a dedicated server");
        initServerProperties();
        useServerProperties();
        CapabilityManager.INSTANCE.register(IPlayerClass.class, new LevelUpCapability.CapabilityPlayerClass<IPlayerClass>(), PlayerExtendedProperties.class);
        CapabilityManager.INSTANCE.register(IProcessor.class, new LevelUpCapability.CapabilityProcessorClass<IProcessor>(), LevelUpCapability.CapabilityProcessorDefault.class);
        if (config.hasChanged())
            config.save();
        if (event.getSourceFile().getName().endsWith(".jar")) {
            proxy.tryUseMUD();
        }
        MinecraftForge.EVENT_BUS.register(FMLEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
    }

//    protected static void initToW(RegistryEvent.Register<IRecipe> evt) {
//        towItems = new HashMap<Object, Integer>();
//        initTalismanProperty(evt, "logWood", 2, "log");
//        initTalismanProperty(evt, Items.COAL, 2, "coal");
//        initTalismanProperty(evt, "ingotBrick", 4, "brick");
//        initTalismanProperty(evt, Items.BOOK, 4, "book");
//        initTalismanProperty(evt, "oreIron", 8, "iron_ore");
//        initTalismanProperty(evt, "gemLapis", 8, "lapis");
//        initTalismanProperty(evt, "dustRedstone", 8, "redstone");
//        initTalismanProperty(evt, Items.BREAD, 10, "bread");
//        initTalismanProperty(evt, Items.MELON, 10, "melon");
//        initTalismanProperty(evt, Item.getItemFromBlock(Blocks.PUMPKIN), 10, "pumpkin");
//        initTalismanProperty(evt, Items.COOKED_PORKCHOP, 12, "porkchop");
//        initTalismanProperty(evt, Items.COOKED_BEEF, 12, "beef");
//        initTalismanProperty(evt, Items.COOKED_CHICKEN, 12, "chicken");
//        initTalismanProperty(evt, Items.COOKED_FISH, 12, "fish");
//        initTalismanProperty(evt, Items.COOKED_MUTTON, 12, "mutton");
//        initTalismanProperty(evt, Items.COOKED_RABBIT, 12, "rabbit");
//        initTalismanProperty(evt, "ingotIron", 16, "iron_ingot");
//        initTalismanProperty(evt, "oreGold", 20, "gold_ore");
//        initTalismanProperty(evt, "ingotGold", 24, "gold_ingot");
//        initTalismanProperty(evt, "gemDiamond", 40, "diamond");
//    }

    private static void initTalismanProperty(RegistryEvent.Register<IRecipe> evt, Object item, int value, String name) {
        towItems.put(item, value);
        evt.getRegistry().register(new ShapelessOreRecipe(new ResourceLocation("levelup:talisman"), xpTalisman, xpTalisman, item).setRegistryName(new ResourceLocation("levelup:talisman_" + name)));
    }

    private void initClientProperties() {
        clientProperties = new Property[]{
                config.get("HUD", "allow HUD", allowHUD, "If anything should be rendered on screen at all.").setRequiresMcRestart(true),
                config.get("HUD", "render HUD on Top Left", renderTopLeft, "Should the player class be displayed in the top left corner."),
                config.get("HUD", "render HUD on Exp Bar", renderExpBar, "Should available skill points be displayed on the experience bar."),
                config.get("FOV", "speed based", changeFOV, "Should FOV change based on player speed from athletics / sneak skills." )};
        allowHUD = clientProperties[0].getBoolean();
        renderTopLeft = clientProperties[1].getBoolean();
        renderExpBar = clientProperties[2].getBoolean();
        changeFOV = clientProperties[3].getBoolean();
    }

    private void initServerProperties() {
        String cat = "Cheats";
        String limitedBonus = "This is a bonus related to a few classes";
        serverProperties = new Property[]{
                config.get(cat, "Max points per skill", ClassBonus.getMaxSkillPoints(), "Minimum is 1"),
                config.get(cat, "Bonus points for classes", ClassBonus.getBonusPoints(), "Points given when choosing a class, allocated automatically.\n Minimum is 0, Maximum is max points per skill times 2"),
                config.get(cat, "Skill Points gained per level", PlayerEventHandler.skillPointsPerLevel, "Minimum is 0"),
                config.get(cat, "Skill points lost on death", (int) PlayerEventHandler.resetSkillOnDeath * 100, "How much skill points are lost on death, in percent.").setMinValue(0).setMaxValue(100),
                config.get(cat, "Use old speed for dirt and gravel digging", PlayerEventHandler.oldSpeedDigging),
                config.get(cat, "Use old speed for redstone breaking", PlayerEventHandler.oldSpeedRedstone, "Makes the redstone ore mining efficient"),
                config.get(cat, "Reset player class on death", PlayerEventHandler.resetClassOnDeath, "Do the player lose the class he choose on death ?"),
                config.get(cat, "Prevent duplicated ores placing", PlayerEventHandler.noPlaceDuplicate, "Some skill duplicate ores, this prevent infinite duplication by replacing"),
                config.get(cat, "Add Bonus XP on Craft", bonusCraftingXP, limitedBonus),
                config.get(cat, "Add Bonus XP on Mining", bonusMiningXP, limitedBonus),
                config.get(cat, "Add XP on Crafting some items", true, "This is a global bonus, limited to a few craftable items"),
                config.get(cat, "Add XP on Mining some ore", oreMiningXP, "This is a global bonus, limited to a few ores"),
                config.get(cat, "Add Bonus XP on Fighting", bonusFightingXP, limitedBonus),
                config.get(cat, "Furnace ejects smelting bonus", LevelUpAPI.furnaceEjection, "Disabling this will cause doubled furnace items to be added to the result slot instead of being ejected"),
                config.get(cat, "Ore blocks harvested cannot be placed", oreNoPlace, "Disabling this will stop ore blocks from having a No Place tag added if not duplicated.")};
    }

    public void useServerProperties() {
        ClassBonus.setSkillMax(serverProperties[0].getInt());
        ClassBonus.setBonusPoints(serverProperties[1].getInt());
        double opt = serverProperties[2].getDouble();
        if (opt >= 0.0D)
            PlayerEventHandler.skillPointsPerLevel = opt <= ClassBonus.getMaxSkillPoints() ? opt : ClassBonus.getMaxSkillPoints();
        PlayerEventHandler.resetSkillOnDeath = (float) serverProperties[3].getInt() / 100.00F;
        PlayerEventHandler.oldSpeedDigging = serverProperties[4].getBoolean();
        PlayerEventHandler.oldSpeedRedstone = serverProperties[5].getBoolean();
        PlayerEventHandler.resetClassOnDeath = serverProperties[6].getBoolean();
        PlayerEventHandler.noPlaceDuplicate = serverProperties[7].getBoolean();
        bonusCraftingXP = serverProperties[8].getBoolean();
        bonusMiningXP = serverProperties[9].getBoolean();
        oreMiningXP = serverProperties[11].getBoolean();
        bonusFightingXP = serverProperties[12].getBoolean();
        LevelUpAPI.furnaceEjection = serverProperties[13].getBoolean();
        oreNoPlace = serverProperties[14].getBoolean();
        if (serverProperties[10].getBoolean()) {
            List<Item> ingrTier1, ingrTier2, ingrTier3, ingrTier4;
            ingrTier1 = Arrays.asList(Items.STICK, Items.LEATHER, Item.getItemFromBlock(Blocks.STONE));
            ingrTier2 = Arrays.asList(Items.IRON_INGOT, Items.GOLD_INGOT, Items.PAPER, Items.SLIME_BALL);
            ingrTier3 = Arrays.asList(Items.REDSTONE, Items.GLOWSTONE_DUST, Items.ENDER_PEARL);
            ingrTier4 = Arrays.asList(Items.DIAMOND);
            tiers = new List[]{ingrTier1, ingrTier2, ingrTier3, ingrTier4};
        }
    }

    public Property[] getServerProperties() {
        return serverProperties;
    }

    public boolean[] getClientProperties() {
        boolean[] result = new boolean[clientProperties.length];
        for (int i = 0; i < clientProperties.length; i++) {
            result[i] = clientProperties[i].getBoolean();
        }
        return result;
    }

    public void refreshValues(boolean[] values) {
        if (values.length == clientProperties.length) {
            LevelUp.allowHUD = values[0];
            LevelUp.renderTopLeft = values[1];
            LevelUp.renderExpBar = values[2];
            LevelUp.changeFOV = values[3];
            for (int i = 0; i < values.length; i++) {
                clientProperties[i].set(values[i]);
            }
            config.save();
        }
    }

    public static boolean isTalismanRecipe(IInventory iinventory) {
        if (xpTalisman != null)
            for (int i = 0; i < iinventory.getSizeInventory(); i++) {
                if (iinventory.getStackInSlot(i) != ItemStack.EMPTY && iinventory.getStackInSlot(i).getItem() == xpTalisman) {
                    return true;
                }
            }
        return false;
    }

    /*
    public static void takenFromCrafting(EntityPlayer player, ItemStack itemstack, IInventory iinventory) {
        if (isTalismanRecipe(iinventory)) {
            for (int i = 0; i < iinventory.getSizeInventory(); i++) {
                ItemStack itemstack1 = iinventory.getStackInSlot(i);
                if (itemstack1 != ItemStack.EMPTY) {
                    String oreDict = containsOreDictEntry(itemstack1);
                    if(oreDict != null) {
                        player.addExperience((int)Math.floor(itemstack1.getCount() * towItems.get(containsOreDictEntry(itemstack1)) / 4D));
                        iinventory.getStackInSlot(i).setCount(0);
                    }
                    else if (towItems.containsKey(itemstack1.getItem())) {
                        player.addExperience((int) Math.floor(itemstack1.getCount() * towItems.get(itemstack1.getItem()) / 4D));
                        iinventory.getStackInSlot(i).setCount(0);
                    }
                }
            }
        } else {
            for (int j = 0; j < iinventory.getSizeInventory(); j++) {
                ItemStack itemstack2 = iinventory.getStackInSlot(j);
                if (itemstack2 != ItemStack.EMPTY && !isUncraftable(itemstack)) {
                    giveCraftingXP(player, itemstack2);
                    giveBonusCraftingXP(player);
                }
            }
        }
    }*/

//    private static String containsOreDictEntry(ItemStack stack) {
//        String[] toCheck = {"logWood", "ingotIron", "ingotGold", "ingotBrick", "gemDiamond", "oreIron", "oreGold", "dustRedstone", "gemLapis"};
//        for(String entry : toCheck) {
//            if(oreDictMatches(stack, OreDictionary.getOres(entry)))
//                return entry;
//        }
//        return null;
//    }

    // Could be useful later...
    private static boolean oreDictMatches(ItemStack stack, List<ItemStack> oreDict) {
        for(ItemStack ore : oreDict) {
            if(ore.getItemDamage() == OreDictionary.WILDCARD_VALUE && ore.getItem() == stack.getItem())
                return true;
            else if(ItemStack.areItemsEqual(stack, ore))
                return true;

        }
        return false;
    }

    public static int getVitality(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 0);
    }

    public static int getMight(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 1);
    }

    public static int getFinesse(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 2);
    }

    public static int getFocus(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 3);
    }

    public static int getStealth(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 4);
    }

    public static int getLuck(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 6);
    }

}

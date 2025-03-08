package com.lumberjacksparrow.leveluprpg.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.entity.projectile.EntityArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityArrow.class)
public interface ArrowStackAccessMixin {

    @Invoker("getArrowStack")
    public ItemStack invokeGetArrowStack();

}

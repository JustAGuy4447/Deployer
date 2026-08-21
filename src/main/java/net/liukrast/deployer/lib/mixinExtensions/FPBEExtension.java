package net.liukrast.deployer.lib.mixinExtensions;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface FPBEExtension {
    @Deprecated
    default List<ItemStack> deployer$getExtraDrops() {
        return List.of();
    }
}

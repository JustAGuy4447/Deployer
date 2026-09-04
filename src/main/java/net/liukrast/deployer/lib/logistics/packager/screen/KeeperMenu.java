package net.liukrast.deployer.lib.logistics.packager.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public interface KeeperMenu<T> {
    Player getPlayer();
    Inventory getPlayerInventory();
    Object getScreenReference();
    T getContentHolder();
}

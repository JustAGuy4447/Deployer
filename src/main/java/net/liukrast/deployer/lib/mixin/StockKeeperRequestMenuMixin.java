package net.liukrast.deployer.lib.mixin;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import net.liukrast.deployer.lib.logistics.packager.screen.KeeperMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StockKeeperRequestMenu.class)
public abstract class StockKeeperRequestMenuMixin extends MenuBase<StockTickerBlockEntity> implements KeeperMenu<StockTickerBlockEntity> {
    
    @Shadow public Object screenReference;

    protected StockKeeperRequestMenuMixin(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory getPlayerInventory() {
        return playerInventory;
    }

    @Override
    public Object getScreenReference() {
        return screenReference;
    }

    @Override
    public StockTickerBlockEntity getContentHolder() {
        return contentHolder;
    }
}

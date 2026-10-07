package net.liukrast.deployer.lib.blockEntity.behaviour.advancedValueSettings;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Function;

public interface AdvancedValueSettingsBehaviour extends ValueSettingsBehaviour {
    default boolean valueEditBoxEnabled() {
        return true;
    }
    default Function<ValueSettings, Pair<Integer, String>> getFormatter() {
        return vs -> Pair.of(vs.row(), Integer.toString(vs.value()));
    }
    List<RowData> getRowData();
    void setValueSettings(Player player, int row, double value, boolean ctrlDown);
}
package net.liukrast.deployer.lib.blockEntity.behaviour.advancedValueSettings;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import net.minecraft.network.chat.Component;

import java.util.List;

public record RowData(Component component, int minValue, int maxValue, boolean acceptsDecimalValue) {
    public RowData(Component component, int minValue, int maxValue) {
        this(component, minValue, maxValue, false);
    }

    public static List<RowData> fromBoard(ValueSettingsBoard board, int minValue) {
        return board.rows().stream().map(c -> new RowData(c, minValue, board.maxValue())).toList();
    }
}

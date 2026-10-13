package net.liukrast.deployer.lib.blockEntity.behaviour.advancedValueSettings;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiFunction;

public class AdvancedScrollValueBehaviour extends ScrollValueBehaviour implements AdvancedValueSettingsBehaviour {

    private BiFunction<Integer, Double, Integer> formatter;

    public AdvancedScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
        formatter = (r, v) -> v.intValue();
    }

    public ScrollValueBehaviour withValueFormatter(BiFunction<Integer, Double, Integer> formatter) {
        this.formatter = formatter;
        return this;
    }

    @Override
    public List<RowData> getRowData() {
        return RowData.fromBoard(createBoard(null, null), 0);
    }

    public int formatValue(int row, double value) {
        return formatter.apply(row, value);
    }

    @Override
    public void setValueSettings(Player player, int row, double value, boolean ctrlDown) {
        int formatted = formatValue(row, value);
        if (this.value == formatted) return;
        setValue(formatted);
        playFeedbackSound(this);
    }
}

package net.liukrast.deployer.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsClient;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen;
import net.liukrast.deployer.lib.blockEntity.behaviour.advancedValueSettings.AdvancedValueSettingsBehaviour;
import net.liukrast.deployer.lib.mixinExtensions.VSSExtension;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(ValueSettingsClient.class)
public class ValueSettingsClientMixin {

    @WrapOperation(method = "tick", at = @At(value = "NEW", target = "(Lnet/minecraft/core/BlockPos;Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsBoard;Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsBehaviour$ValueSettings;Ljava/util/function/Consumer;I)Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsScreen;"))
    private ValueSettingsScreen tick(
            BlockPos pos,
            ValueSettingsBoard board,
            ValueSettingsBehaviour.ValueSettings valueSettings,
            Consumer<ValueSettingsBehaviour.ValueSettings> onHover,
            int netId,
            Operation<ValueSettingsScreen> original,
            @Local(name = "valueSettingBehaviour") ValueSettingsBehaviour valueSettingBehaviour
    ) {
        if(board == null) return null;
        ValueSettingsScreen screen = original.call(pos, board, valueSettings, onHover, netId);
        if (valueSettingBehaviour instanceof AdvancedValueSettingsBehaviour avsb && avsb.valueEditBoxEnabled()) {
            ((VSSExtension) screen).deployer$setRowData(avsb.getRowData());
            ((VSSExtension) screen).deployer$setFormatter(avsb.getFormatter());
        }
        return screen;
    }
}

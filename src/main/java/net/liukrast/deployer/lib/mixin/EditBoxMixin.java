package net.liukrast.deployer.lib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.liukrast.deployer.lib.blockEntity.behaviour.advancedValueSettings.ValueEditBox;
import net.minecraft.client.gui.components.EditBox;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EditBox.class)
public class EditBoxMixin {
    @ModifyExpressionValue(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;getX()I"))
    private int getTextX(int original){
        return EditBox.class.cast(this) instanceof ValueEditBox ? original + 5 : original;
    }

    @ModifyExpressionValue(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;getY()I"))
    private int getTextY(int original){
        return EditBox.class.cast(this) instanceof ValueEditBox ? original + 1 : original;
    }

    @ModifyArg(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"), index = 4)
    private int getCursorMaxY(int maxY) {
        return EditBox.class.cast(this) instanceof ValueEditBox ? maxY - 1 : maxY;
    }

    @ModifyArg(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"), index = 5)
    private int getCursorColor(int cursorColor, @Local(name = "l1") int l1) {
        int color = l1 | 0xFF000000;
        return EditBox.class.cast(this) instanceof ValueEditBox ? color : cursorColor;
    }

    @ModifyArg(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;renderHighlight(Lnet/minecraft/client/gui/GuiGraphics;IIII)V"), index = 4)
    private int getHighlightMaxY(int maxY) {
        return EditBox.class.cast(this) instanceof ValueEditBox ? maxY - 1 : maxY;
    }

    @ModifyExpressionValue(method = "renderHighlight", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;width:I", opcode = Opcodes.GETFIELD))
    private int getHighlightWidth(int original) {
        return EditBox.class.cast(this) instanceof ValueEditBox ? original - 5 : original;
    }
}

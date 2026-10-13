package net.liukrast.deployer.lib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.liukrast.deployer.lib.blockEntity.behaviour.advancedValueSettings.AdvancedValueSettingsPacket;
import net.liukrast.deployer.lib.blockEntity.behaviour.advancedValueSettings.RowData;
import net.liukrast.deployer.lib.blockEntity.behaviour.advancedValueSettings.ValueEditBox;
import net.liukrast.deployer.lib.mixin.accessors.ValueSettingsPacketAccessor;
import net.liukrast.deployer.lib.mixinExtensions.VSSExtension;
import net.liukrast.deployer.lib.registry.DeployerGuiTextures;
import net.liukrast.deployer.lib.registry.DeployerKeys;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(ValueSettingsScreen.class)
public abstract class ValueSettingsScreenMixin extends AbstractSimiScreen implements VSSExtension {

    @Unique private ValueEditBox deployer$valueEditBox;
    @Unique private IconButton deployer$closeButton;

    @Unique private boolean deployer$active = false;
    @Unique private final List<RowData> deployer$rowData = new ArrayList<>();
    @Unique private Function<ValueSettings, Pair<Integer, String>> deployer$formatter;
    @Unique private int deployer$lastRow;
    @Unique private final Component deployer$tip = Component.translatable("deployer.gui.value_settings.press_to_open_value_box",
            DeployerKeys.VALUE_EDIT_BOX.getKeyMapping().getTranslatedKeyMessage()
    );

    @Shadow private int maxLabelWidth;
    @Shadow private int valueBarWidth;
    @Shadow private BlockPos pos;
    @Shadow private boolean iconMode;
    @Shadow private int netId;

    @Shadow protected abstract void saveAndClose(double pMouseX, double pMouseY);
    @Shadow public abstract ValueSettings getClosestCoordinate(int mouseX, int mouseY);

    protected ValueSettingsScreenMixin(Component title) {
        super(title);
    }

    @Override
    public void deployer$setRowData(List<RowData> rowData) {
        deployer$rowData.addAll(rowData);
    }

    @Override
    public void deployer$setFormatter(Function<ValueSettings, Pair<Integer, String>> formatter) {
        deployer$formatter = formatter;
    }

    @Unique
    private <Out> List<Out> deployer$mapRowData(Function<RowData, Out> func) {
        return deployer$rowData.stream().map(func).toList();
    }

    @Unique
    private void deployer$activateValueEditBox() {
        if (deployer$rowData.isEmpty()) return;
        deployer$active = true;
        iconMode = false;

        assert minecraft != null;
        Window window = minecraft.getWindow();
        int x = (int) (minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth());
        int y = (int) (minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());
        ValueSettings closest = getClosestCoordinate(x, y);

        maxLabelWidth = 0;
        for (Component component : deployer$mapRowData(RowData::component))
            maxLabelWidth = Math.max(maxLabelWidth, font.width(component));
        valueBarWidth = 128 + 1 + 10;

        int width = (maxLabelWidth + 14) + (valueBarWidth + 10);
        int height = (deployer$rowData.size() * 11);
        setWindowSize(width, height);
        super.init();

        Pair<Integer, String> formatted = deployer$formatter.apply(closest);
        deployer$lastRow = formatted.getFirst();

        deployer$valueEditBox = new ValueEditBox(new NoShadowFontWrapper(font), guiLeft + maxLabelWidth + 14 + 2, valueBarWidth + 4, deployer$rowData, () -> guiTop);
        deployer$valueEditBox.setInitialValueSettings(formatted.getFirst(), formatted.getSecond());
        deployer$valueEditBox.setSubResponder(isValid -> {
            deployer$closeButton.setIcon(isValid ? AllIcons.I_CONFIRM : AllIcons.I_DISABLE);
            deployer$closeButton.setToolTip(isValid
                    ? Component.translatable("deployer.gui.value_settings.save_and_close")
                    : Component.translatable("deployer.gui.value_settings.close")
            );
        });
        setFocused(deployer$valueEditBox);
        addRenderableWidget(deployer$valueEditBox);

        deployer$closeButton = new IconButton(0, 0, 22, 20, AllIcons.I_CONFIRM) {
            @Override
            public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                if (visible) {
                    drawBg(graphics, AllGuiTextures.VALUE_SETTINGS_CURSOR_ICON);

                    RenderSystem.setShaderColor(0.265625f, 0.125f, 0, 1);
                    icon.render(graphics, getX() + 3, getY() + 2);
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                }
            }
        };
        deployer$closeButton.withCallback(this::saveAndClose);
        deployer$closeButton.setToolTip(Component.translatable("deployer.gui.value_settings.save_and_close"));
        addWidget(deployer$closeButton);
    }

    @Unique
    private boolean deployer$valueBoxReady() {
        return !deployer$rowData.isEmpty() && !deployer$active;
    }

    /* RENDERING */
    @ModifyVariable(method = "renderWindow", at = @At(value = "STORE"), name = "milestoneCount")
    private int getMilestoneCount(int milestoneCount) {
        return deployer$active ? 0 : milestoneCount;
    }

    @ModifyExpressionValue(method = "renderWindow", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 0))
    private int getFattestLabel(int original, @Local(name = "title") Component title) {
        return deployer$valueBoxReady() ? Math.max(original, font.width(deployer$tip)) : font.width(title);
    }

    // Redundant
    @ModifyExpressionValue(
            method = "renderWindow",
            at = @At(value = "FIELD", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsScreen;iconMode:Z", opcode = Opcodes.GETFIELD)
    )
    private boolean modifyIconModeVal(boolean original) {
        return original && !deployer$active;
    }

    @ModifyVariable(method = "renderWindow", at = @At(value = "STORE"), name = "additionalHeight")
    private int getAdditionalHeight(int additionalHeight) {
        return deployer$valueBoxReady() ? 46 : (deployer$active ? 44 : additionalHeight);
    }

    @WrapOperation(
            method = "renderWindow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", ordinal = 1)
    )
    private int drawString(
            GuiGraphics instance,
            Font font,
            Component text,
            int x, int y,
            int color,
            boolean dropShadow,
            Operation<Integer> original,
            @Local(name = "textX") int textX,
            @Local(name = "y") int y2
    ) {
        if (!deployer$active) {
            if (!deployer$rowData.isEmpty())
                instance.drawString(font, deployer$tip, textX - font.width(deployer$tip) / 2, y2 + windowHeight + 33, 0xdddddd, false);
            return original.call(instance, font, text, x, y, color, dropShadow);
        }
        return 0;
    }

    @ModifyExpressionValue(
            method = "renderWindow",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsBoard;rows()Ljava/util/List;")
    )
    private List<Component> rows(List<Component> original) {
        return deployer$active ? deployer$mapRowData(RowData::component) : original;
    }

    @Inject(
            method = "renderWindow",
            at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/gui/UIRenderHelper;drawCropped(Lnet/minecraft/client/gui/GuiGraphics;IIIIILnet/createmod/catnip/gui/TextureSheetSegment;)V", ordinal = 0)
    )
    private void renderValueBarSides(
            GuiGraphics graphics,
            int mouseX, int mouseY,
            float partialTicks,
            CallbackInfo ci,
            @Local(name = "y") int y,
            @Local(name = "valueBarX") int valueBarX
    ) {
        if (deployer$active) {
            DeployerGuiTextures.VALUE_SETTINGS_SMOOTH_BAR_L.render(graphics, valueBarX, y + 1);
            DeployerGuiTextures.VALUE_SETTINGS_SMOOTH_BAR_R.render(graphics, valueBarX + valueBarWidth - 2, y + 1);
        }
    }

    @ModifyExpressionValue(
            method = "renderWindow",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/createmod/catnip/gui/UIRenderHelper;drawCropped(Lnet/minecraft/client/gui/GuiGraphics;IIIIILnet/createmod/catnip/gui/TextureSheetSegment;)V", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", ordinal = 2)
            ),
            at = @At(value = "FIELD", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsScreen;valueBarWidth:I", opcode = Opcodes.GETFIELD)
    )
    private int getValueBarWidth(int original) {
        return deployer$active ? original - 4 : original;
    }

    @ModifyArg(
            method = "renderWindow",
            at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/gui/UIRenderHelper;drawCropped(Lnet/minecraft/client/gui/GuiGraphics;IIIIILnet/createmod/catnip/gui/TextureSheetSegment;)V", ordinal = 1),
            index = 1
    )
    private int getX(int x) {
        return deployer$active ? x + 2 : x;
    }

    @ModifyExpressionValue(
            method = "renderWindow",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/gui/AllGuiTextures;getWidth()I", ordinal = 1)
    )
    private int getWidth(int original) {
        return deployer$active ? DeployerGuiTextures.VALUE_SETTINGS_SMOOTH_BAR.getWidth() : original;
    }

    @WrapOperation(
            method = "renderWindow",
            at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/gui/UIRenderHelper;drawCropped(Lnet/minecraft/client/gui/GuiGraphics;IIIIILnet/createmod/catnip/gui/TextureSheetSegment;)V", ordinal = 1)
    )
    private void renderValueBar(GuiGraphics graphics, int left, int top, int w, int h, int z, TextureSheetSegment tex, Operation<Void> original) {
        if (deployer$active) {
            UIRenderHelper.drawCropped(graphics, left, top, w, h, z, DeployerGuiTextures.VALUE_SETTINGS_SMOOTH_BAR);
            return;
        }
        original.call(graphics, left, top, w, h, z, tex);
    }

    @Inject(
            method = "renderWindow",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsScreen;renderBrassFrame(Lnet/minecraft/client/gui/GuiGraphics;IIII)V", ordinal = 1)
    )
    private void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci, @Local(name = "y") int y, @Local(name = "bgWidth") int bgWidth) {
        if (deployer$active) {
            deployer$closeButton.setX(guiLeft + (bgWidth / 2) - (deployer$closeButton.getWidth() / 2));
            deployer$closeButton.setY(y + 4);
            deployer$closeButton.render(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @ModifyExpressionValue(
            method = "renderWindow",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsBehaviour$ValueSettings;equals(Ljava/lang/Object;)Z")
    )
    private boolean shouldCancelSound(boolean original) {
        if (deployer$active) {
            if (deployer$valueEditBox.getRow() != deployer$lastRow) {
                deployer$lastRow = deployer$valueEditBox.getRow();
                return false;
            }
            return true;
        }
        return original;
    }

    @Inject(
            method = "renderWindow",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsScreen;getCoordinateOfValue(II)Lnet/minecraft/world/phys/Vec2;"),
            cancellable = true)
    private void cancel(CallbackInfo ci) {
        if (deployer$active) ci.cancel();
    }

    /* INTERACTION */
    @Unique
    private boolean deployer$validValueBarInteraction(double mouseX, double mouseY, int button) {
        return deployer$active && button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && mouseX >= guiLeft + 14 && mouseX <= guiLeft + 14 + valueBarWidth + 8
                && mouseY >= guiTop - 3 && mouseY <= guiTop - 3 + (deployer$rowData.size() * 11 + 5);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (deployer$active && deployer$validValueBarInteraction(mouseX, mouseY, button)) {
            deployer$valueEditBox.setDraggingAndRow(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Inject(method = "mouseReleased", at = @At(value = "HEAD"), cancellable = true)
    private void mouseReleased(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
        if (deployer$valueBoxReady() && DeployerKeys.VALUE_EDIT_BOX.getKeyMapping().matchesMouse(pButton)) {
            deployer$activateValueEditBox();
            cir.setReturnValue(true);
        } else if (deployer$validValueBarInteraction(pMouseX, pMouseY, pButton)) {
            setFocused(deployer$valueEditBox);
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;matchesMouse(I)Z"))
    private boolean shouldClose$1(boolean original) {
        return !deployer$active && original;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (deployer$validValueBarInteraction(mouseX, mouseY, button)) {
            setFocused(null);
            deployer$valueEditBox.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Inject(method = "keyReleased", at = @At(value = "HEAD"), cancellable = true)
    private void keyReleased(int pKeyCode, int pScanCode, int pModifiers, CallbackInfoReturnable<Boolean> cir) {
        if (deployer$valueBoxReady() && DeployerKeys.VALUE_EDIT_BOX.getKeyMapping().matches(pKeyCode, pScanCode)) {
            deployer$activateValueEditBox();
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(method = "keyReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;matches(II)Z"))
    private boolean shouldClose(boolean original) {
        return !deployer$active && original;
    }

    @WrapOperation(
            method = "saveAndClose",
            at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/platform/services/NetworkHelper;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V")
    )
    private void setValue(NetworkHelper instance, CustomPacketPayload packet, Operation<Void> original) {
        if (!deployer$active) {
            original.call(instance, packet);
            return;
        }
        CatnipServices.NETWORK.sendToServer(
                new AdvancedValueSettingsPacket(pos, deployer$valueEditBox.getRow(), deployer$valueEditBox.getParsedValue(), ((ValueSettingsPacketAccessor) packet), netId)
        );
    }
}

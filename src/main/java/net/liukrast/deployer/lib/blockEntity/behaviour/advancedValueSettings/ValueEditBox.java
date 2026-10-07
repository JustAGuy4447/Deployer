package net.liukrast.deployer.lib.blockEntity.behaviour.advancedValueSettings;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.createmod.catnip.gui.UIRenderHelper;
import net.liukrast.deployer.lib.registry.DeployerGuiTextures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

@ApiStatus.Internal
public class ValueEditBox extends EditBox {

    private final IntSupplier guiTop;
    private final List<RowData> rowData;
    private boolean isDragging = false;
    private double value = 0;
    private int row;

    private final Consumer<String> mainResponder;
    private Consumer<Boolean> subResponder;

    public ValueEditBox(Font font, int x, int width, List<RowData> rowData, IntSupplier guiTop) {
        super(font, x, 0, width, 10, Component.empty());
        this.rowData = rowData;
        this.guiTop = guiTop;
        this.mainResponder = this::mainResponder;
        super.setBordered(false);
        super.setTextColor(0x442000);
        super.setResponder(mainResponder);
    }


    /* ROW MANAGEMENT */
    public void setDraggingAndRow(double mouseY) {
        isDragging = true;
        setRowFromMousePos(mouseY);
    }

    public void setInitialValueSettings(int row, String value) {
        setRow(row);
        setValue(value);
    }

    public void setRowFromMousePos(double mouseY) {
        int row = 0;
        double bestDiff = Double.MAX_VALUE;

        for (; row < rowData.size(); row++) {
            float rowPos = getCoordinateOfRow(row);
            double diff = Math.abs(rowPos - mouseY);
            if (bestDiff < diff)
                break;
            bestDiff = diff;
        }
        row -= 1;

        setRow(row);
    }

    public void setRow(int row) {
        setRow(row, (int) getCoordinateOfRow(row));
    }

    private float getCoordinateOfRow(int row) {
        return guiTop.getAsInt() + (row + .5f) * 11 - .5f;
    }

    public void setRow(int row, int rowPos) {
        this.row = row;
        setY(rowPos - 5);
        onRowChange();
    }

    private void onRowChange() {
        mainResponder.accept(getValue());
    }


    /* VALUE GETTERS */
    public int getRow() {
        return row;
    }

    public double getParsedValue() {
        return value;
    }


    /* INTERACTION */
    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (isDragging) setRowFromMousePos(mouseY);
    }


    /* RENDERING */
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(this.isVisible()) {
            UIRenderHelper.drawCropped(guiGraphics, getX(), getY(), getWidth(), getHeight(), 0, DeployerGuiTextures.VALUE_EDIT_BOX);
            renderFrame(guiGraphics, getX() - 3, getY() - 3, getWidth() + 6, getHeight() + 6);
        }
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderFrame(GuiGraphics graphics, int x, int y, int w, int h) {
        AllGuiTextures.BRASS_FRAME_TL.render(graphics, x, y);
        AllGuiTextures.BRASS_FRAME_TR.render(graphics, x + w - 4, y);
        AllGuiTextures.BRASS_FRAME_BL.render(graphics, x, y + h - 4);
        AllGuiTextures.BRASS_FRAME_BR.render(graphics, x + w - 4, y + h - 4);

        int zLevel = 0;
        if (h > 8) {
            UIRenderHelper.drawStretched(graphics, x, y + 4, 3, h - 8, zLevel, AllGuiTextures.BRASS_FRAME_LEFT);
            UIRenderHelper.drawStretched(graphics, x + w - 3, y + 4, 3, h - 8, zLevel, AllGuiTextures.BRASS_FRAME_RIGHT);
        }
        if (w > 8) {
            UIRenderHelper.drawCropped(graphics, x + 4, y, w - 8, 3, zLevel, AllGuiTextures.BRASS_FRAME_TOP);
            UIRenderHelper.drawCropped(graphics, x + 4, y + h - 3, w - 8, 3, zLevel, AllGuiTextures.BRASS_FRAME_BOTTOM);
        }

        int cursorY = y + (h / 2) - 2;
        DeployerGuiTextures.BRASS_FRAME_CURSOR_LEFT.render(graphics, x - 1, cursorY);
        DeployerGuiTextures.BRASS_FRAME_CURSOR_RIGHT.render(graphics, x + w - 3, cursorY);
    }

    @Override
    public int getInnerWidth() {
        return getWidth() - 10;
    }

    /* RESPONDERS */
    private void mainResponder(String str) {
        try {
            RowData rowData = this.rowData.get(row);
            value = rowData.acceptsDecimalValue()
                    ? Double.parseDouble(str)
                    : Integer.parseInt(str);

            if (value < rowData.minValue() || value > rowData.maxValue()) {
                value = Math.clamp(value, rowData.minValue(), rowData.maxValue());
                setTextColor(0x800000);
                if (subResponder != null)
                    subResponder.accept(false);
                return;
            }

            setTextColor(0x442000);
            if (subResponder != null)
                subResponder.accept(true);

        } catch (NumberFormatException e) {
            value = 0;
            setTextColor(0x800000);
            if (subResponder != null)
                subResponder.accept(false);
        }
    }

    @Override
    public void setResponder(Consumer<String> responder) {
        super.setResponder(mainResponder.andThen(responder));
    }

    public void setSubResponder(Consumer<Boolean> subResponder) {
        this.subResponder = subResponder;
    }

}

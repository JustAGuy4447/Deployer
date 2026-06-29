package net.liukrast.deployer.lib.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.FactoryGaugeDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ValueListDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayLayout;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.IntAttached;
import net.liukrast.deployer.lib.logistics.board.AbstractPanelBehaviour;
import net.liukrast.deployer.lib.mixin.accessors.ValueListDisplaySourceAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin(FactoryGaugeDisplaySource.class)
public abstract class FactoryGaugeDisplaySourceMixin extends ValueListDisplaySource {
    /*
    * Abstract panel behaviors will ignore the default display link
    * */
    @Inject(
            method = "createEntry",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;getFilter()Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true
    )
    private void createEntry(Level level, FactoryPanelPosition pos, CallbackInfoReturnable<IntAttached<MutableComponent>> cir, @Local FactoryPanelBehaviour panel) {
        if(panel instanceof AbstractPanelBehaviour) cir.setReturnValue(null);
    }

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        var list = super.provideText(context, stats);
        var list1 = context.blockEntity().factoryPanelSupport.getLinkedPanels().stream().map(pos -> {
            var panel = FactoryPanelBehaviour.at(context.level(), pos);
            if(panel instanceof AbstractPanelBehaviour ab) return ab.getDisplayLinkComponent(shortenNumbers(context));
            return null;
        }).filter(Objects::nonNull).limit(stats.maxRows());
        return Stream.concat(list.stream(), list1).toList();
    }

    @Override
    public List<List<MutableComponent>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
        MutableInt highest = new MutableInt(0);
        context.flapDisplayContext = highest;
        var list = provideEntries(context, stats.maxRows()).map(e -> {
                    highest.setValue(Math.max(highest.getValue(), e.getFirst()));
                    return deployer$createComponentsFromEntry(context, e);
        });
        var list1 = context.blockEntity().factoryPanelSupport.getLinkedPanels().stream().map(pos -> {
            var panel = FactoryPanelBehaviour.at(context.level(), pos);
            if(panel instanceof AbstractPanelBehaviour ab) return ab.getDisplayLinkComponent(shortenNumbers(context));
            return null;
        }).filter(Objects::nonNull).limit(stats.maxRows()).map(Arrays::asList);
        return Stream.concat(list, list1).toList();
    }

    @Unique
    private List<MutableComponent> deployer$createComponentsFromEntry(DisplayLinkContext context, IntAttached<MutableComponent> entry) {
        int number = entry.getFirst();
        MutableComponent name = entry.getSecond().append(WHITESPACE);
        if (shortenNumbers(context)) {
            Couple<MutableComponent> shortened = ((ValueListDisplaySourceAccessor)this).deployer$shorten(number);
            return List.of(shortened.getFirst().append(shortened.getSecond()).append(name));
        }
        MutableComponent formattedNumber = Component.literal(String.valueOf(number)).append(WHITESPACE);
        return List.of(formattedNumber.append(name));
    }

    @Override
    public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout) {
        if (!layout.isLayout("Default"))
            layout.loadDefault(flapDisplay.getMaxCharCount());
    }
}

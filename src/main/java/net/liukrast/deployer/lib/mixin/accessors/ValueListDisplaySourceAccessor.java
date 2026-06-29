package net.liukrast.deployer.lib.mixin.accessors;

import com.simibubi.create.content.redstone.displayLink.source.ValueListDisplaySource;
import net.createmod.catnip.data.Couple;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ValueListDisplaySource.class)
public interface ValueListDisplaySourceAccessor {

    @Invoker("shorten")
    Couple<MutableComponent> deployer$shorten(int number);
}

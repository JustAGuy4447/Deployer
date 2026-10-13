package net.liukrast.deployer.lib.mixin.accessors;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsPacket;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ValueSettingsPacket.class)
public interface ValueSettingsPacketAccessor {
    @Accessor("interactHand")
    InteractionHand getInteractHand();

    @Accessor("side")
    Direction getSide();

    @Accessor("hitResult")
    BlockHitResult getHitResult();
}

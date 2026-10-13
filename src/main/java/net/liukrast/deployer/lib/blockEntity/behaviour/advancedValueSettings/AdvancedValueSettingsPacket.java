package net.liukrast.deployer.lib.blockEntity.behaviour.advancedValueSettings;

import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipLargerStreamCodecs;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.liukrast.deployer.lib.mixin.accessors.ValueSettingsPacketAccessor;
import net.liukrast.deployer.lib.registry.DeployerPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AdvancedValueSettingsPacket extends BlockEntityConfigurationPacket<SmartBlockEntity> {
    public static final StreamCodec<ByteBuf, AdvancedValueSettingsPacket> STREAM_CODEC = CatnipLargerStreamCodecs.composite(
            BlockPos.STREAM_CODEC, p -> p.pos,
            ByteBufCodecs.VAR_INT, p -> p.row,
            ByteBufCodecs.DOUBLE, p -> p.value,
            CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.HAND), p -> p.interactHand,
            CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.BLOCK_HIT_RESULT), p -> p.hitResult,
            Direction.STREAM_CODEC, p -> p.side,
            ByteBufCodecs.BOOL, p -> p.ctrlDown,
            ByteBufCodecs.VAR_INT, p -> p.behaviourIndex,
            AdvancedValueSettingsPacket::new
    );

    private final int row;
    private final double value;
    private final InteractionHand interactHand;
    private final Direction side;
    private final boolean ctrlDown;
    private final int behaviourIndex;
    private final BlockHitResult hitResult;

    public AdvancedValueSettingsPacket(BlockPos pos, int row, double value, @Nullable InteractionHand interactHand,
                                       @Nullable BlockHitResult hitResult, Direction side, boolean ctrlDown, int behaviourIndex) {
        super(pos);
        this.row = row;
        this.value = value;
        this.interactHand = interactHand;
        this.hitResult = hitResult;
        this.side = side;
        this.ctrlDown = ctrlDown;
        this.behaviourIndex = behaviourIndex;
    }

    public AdvancedValueSettingsPacket(BlockPos pos, int row, double value, ValueSettingsPacketAccessor packet, int behaviourIndex) {
        this(pos, row, value, packet.getInteractHand(), packet.getHitResult(), packet.getSide(), AllKeys.ctrlDown(), behaviourIndex);
    }

    @Override
    protected void applySettings(ServerPlayer player, SmartBlockEntity be) {
        for (BlockEntityBehaviour behaviour : be.getAllBehaviours()) {
            if (!(behaviour instanceof AdvancedValueSettingsBehaviour avsb))
                continue;
            if (!avsb.acceptsValueSettings())
                continue;
            if (behaviourIndex != avsb.netId())
                continue;
            if (interactHand != null) {
                avsb.onShortInteract(player, interactHand, side, hitResult);
                return;
            }
            avsb.setValueSettings(player, row, value, ctrlDown);
            return;
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DeployerPackets.ADVANCED_VALUE_SETTINGS;
    }
}

package net.liukrast.deployer.lib.mixin.accessors;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FactoryPanelSupportBehaviour.class)
public interface FactoryPanelSupportAccessor {
    @Accessor("changed")
    void deployer$setChanged(boolean changed);
}

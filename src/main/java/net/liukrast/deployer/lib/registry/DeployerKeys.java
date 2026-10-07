package net.liukrast.deployer.lib.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public enum DeployerKeys {
    VALUE_EDIT_BOX(GLFW.GLFW_MOUSE_BUTTON_MIDDLE, InputConstants.Type.MOUSE, "value_edit_box"),
    ;

    private final KeyMapping keyMapping;

    DeployerKeys(int defaultKey, InputConstants.Type defaultType, String descTranslationKey) {
        this.keyMapping = new KeyMapping("deployer.key." + descTranslationKey, defaultType, defaultKey, "Deployer");
    }

    public KeyMapping getKeyMapping() {
        return keyMapping;
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        for (DeployerKeys key : values()) {
            event.register(key.keyMapping);
        }
    }
}

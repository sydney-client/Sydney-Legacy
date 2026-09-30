package me.aidan.sydney.mixins;

import me.aidan.sydney.Sydney;
import me.aidan.sydney.modules.impl.miscellaneous.UnfocusedFPSModule;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class InactivityFpsLimiterMixin {
    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void getFramerateLimit(CallbackInfoReturnable<Integer> info) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        UnfocusedFPSModule module = Sydney.MODULE_MANAGER.getModule(UnfocusedFPSModule.class);
        if (module.isToggled() && !client.isWindowFocused()) {
            info.setReturnValue(module.limit.getValue().intValue());
        }
    }
}

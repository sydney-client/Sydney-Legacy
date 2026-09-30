package me.aidan.sydney.mixins;

import me.aidan.sydney.Sydney;
import me.aidan.sydney.modules.impl.visuals.AtmosphereModule;
import me.aidan.sydney.modules.impl.visuals.NoRenderModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @ModifyArg(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"), index = 0)
    private static float applyFogStart(float fogStart, Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickenFog, float tickDelta) {
        if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN && Sydney.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(NoRenderModule.class).fog.getValue()) {
            return viewDistance * 4.0f;
        }

        AtmosphereModule module = Sydney.MODULE_MANAGER.getModule(AtmosphereModule.class);
        return module.isToggled() && module.modifyFog.getValue() ? module.fogStart.getValue().floatValue() : fogStart;
    }

    @ModifyArg(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V"), index = 0)
    private static float applyFogEnd(float fogEnd, Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickenFog, float tickDelta) {
        if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN && Sydney.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(NoRenderModule.class).fog.getValue()) {
            return viewDistance * 4.25f;
        }

        AtmosphereModule module = Sydney.MODULE_MANAGER.getModule(AtmosphereModule.class);
        return module.isToggled() && module.modifyFog.getValue() ? module.fogEnd.getValue().floatValue() : fogEnd;
    }

    @ModifyArgs(method = "applyFogColor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogColor(FFF)V"))
    private static void applyFogColor(Args args) {
        AtmosphereModule module = Sydney.MODULE_MANAGER.getModule(AtmosphereModule.class);
        if (!module.isToggled() || !module.modifyFog.getValue()) return;

        java.awt.Color color = module.fogColor.getColor();
        args.set(0, color.getRed() / 255.0f);
        args.set(1, color.getGreen() / 255.0f);
        args.set(2, color.getBlue() / 255.0f);
    }

    @Inject(method = "getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;", at = @At("HEAD"), cancellable = true)
    private static void getFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<BackgroundRenderer.StatusEffectFogModifier> info) {
        if (Sydney.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(NoRenderModule.class).blindness.getValue()) {
            info.setReturnValue(null);
        }
    }
}

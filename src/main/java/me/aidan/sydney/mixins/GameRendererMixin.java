package me.aidan.sydney.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import me.aidan.sydney.Sydney;
import me.aidan.sydney.events.impl.RenderWorldEvent;
import me.aidan.sydney.modules.impl.miscellaneous.FOVModifierModule;
import me.aidan.sydney.modules.impl.player.NoEntityTraceModule;
import me.aidan.sydney.modules.impl.visuals.AspectRatioModule;
import me.aidan.sydney.modules.impl.visuals.FreecamModule;
import me.aidan.sydney.modules.impl.visuals.NoRenderModule;
import me.aidan.sydney.utils.graphics.Renderer2D;
import me.aidan.sydney.utils.graphics.Renderer3D;
import me.aidan.sydney.utils.minecraft.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow private float zoom;

    @Shadow private float zoomX;

    @Shadow private float zoomY;

    @Shadow private float viewDistance;

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void renderWorld$HEAD(RenderTickCounter tickCounter, CallbackInfo info) {
        Renderer3D.prepare();
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void renderWorld$swap(RenderTickCounter tickCounter, CallbackInfo info, @Local(ordinal = 2) Matrix4f matrix4f3, @Local(ordinal = 1) float tickDelta, @Local MatrixStack matrixStack) {
        RenderSystem.getModelViewStack().pushMatrix();

        RenderSystem.getModelViewStack().mul(matrix4f3);
        RenderSystem.getModelViewStack().mul(matrixStack.peek().getPositionMatrix().invert());

        Sydney.EVENT_HANDLER.post(new RenderWorldEvent(matrixStack, tickDelta));

        Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
        Renderer3D.draw(Renderer3D.SHINE_QUADS, Renderer3D.SHINE_DEBUG_LINES, true);

        Sydney.EVENT_HANDLER.post(new RenderWorldEvent.Post(matrixStack, tickDelta));
        RenderSystem.getModelViewStack().popMatrix();
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurt(CallbackInfo info) {
        if (Sydney.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(NoRenderModule.class).hurtCamera.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "updateCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"), cancellable = true)
    private void updateCrosshairTarget(float tickDelta, CallbackInfo info) {
        if (Sydney.MODULE_MANAGER.getModule(FreecamModule.class).isToggled()) {
            client.getProfiler().pop();
            client.crosshairTarget = WorldUtils.getRaytraceTarget(Sydney.MODULE_MANAGER.getModule(FreecamModule.class).getFreeYaw(), Sydney.MODULE_MANAGER.getModule(FreecamModule.class).getFreePitch(), Sydney.MODULE_MANAGER.getModule(FreecamModule.class).getFreeX(), Sydney.MODULE_MANAGER.getModule(FreecamModule.class).getFreeY(), Sydney.MODULE_MANAGER.getModule(FreecamModule.class).getFreeZ());
            info.cancel();
        }
    }

    @ModifyExpressionValue(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
    private @Nullable EntityHitResult findCrosshairTarget(@Nullable EntityHitResult original) {
        if (Sydney.MODULE_MANAGER.getModule(NoEntityTraceModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(NoEntityTraceModule.class).shouldIgnore()) {
            return null;
        }

        return original;
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void showFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (Sydney.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(NoRenderModule.class).totemAnimation.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void renderWorld(RenderTickCounter tickCounter, CallbackInfo info, @Local(ordinal = 0) Matrix4f matrix4f) {
        MatrixStack matrix = new MatrixStack();
        matrix.peek().getPositionMatrix().mul(matrix4f);

        Renderer2D.LAST_PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        Renderer2D.LAST_MODEL_MATRIX.set(RenderSystem.getModelViewMatrix());
        Renderer2D.LAST_WORLD_MATRIX.set(matrix.peek().getPositionMatrix());
    }

    @Inject(method = "getFov", at = @At("TAIL"), cancellable = true)
    private void getFOV(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> info) {
        FOVModifierModule module = Sydney.MODULE_MANAGER.getModule(FOVModifierModule.class);
        if(module.isToggled()) {
            if(info.getReturnValue() == 70 && !module.items.getValue()) return;
            info.setReturnValue(module.fov.getValue().floatValue());
        }
    }

    @Inject(method = "getBasicProjectionMatrix",at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrix(float fovDegrees, CallbackInfoReturnable<Matrix4f> info) {
        if (Sydney.MODULE_MANAGER.getModule(AspectRatioModule.class).isToggled()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }

            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float)(fovDegrees * 0.01745329238474369), Sydney.MODULE_MANAGER.getModule(AspectRatioModule.class).ratio.getValue().floatValue(), 0.05f, viewDistance * 4.0f));
            info.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }
}

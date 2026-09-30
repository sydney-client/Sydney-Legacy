package me.aidan.sydney.mixins;

import me.aidan.sydney.Sydney;
import me.aidan.sydney.modules.impl.core.FontModule;
import me.aidan.sydney.utils.IMinecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public class TextRendererMixin {
    @Inject(method = "drawLayer(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)F", at = @At("HEAD"), cancellable = true)
    private void drawLayer$String(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int backgroundColor, int light, CallbackInfoReturnable<Float> info) {
        if (Sydney.MODULE_MANAGER.getModule(FontModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(FontModule.class).customFont.getValue() && Sydney.MODULE_MANAGER.getModule(FontModule.class).global.getValue()) {
            MatrixStack matrices = new MatrixStack();

            matrices.push();
            matrices.multiplyPositionMatrix(matrix);

            if (shadow) Sydney.FONT_MANAGER.getFontRenderer().drawString(matrices, text, x + Sydney.FONT_MANAGER.getShadowOffset(), y + Sydney.FONT_MANAGER.getShadowOffset(), color, true);
            Sydney.FONT_MANAGER.getFontRenderer().drawString(matrices, text, x, y, color, false);

            matrices.pop();

            info.setReturnValue(x + Sydney.FONT_MANAGER.getFontRenderer().getTextWidth(text));
        }
    }

    @Inject(method = "drawLayer(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)F", at = @At("HEAD"), cancellable = true)
    private void drawLayer$OrderedText(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int underlineColor, int light, CallbackInfoReturnable<Float> info) {
        if (Sydney.MODULE_MANAGER.getModule(FontModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(FontModule.class).customFont.getValue() && Sydney.MODULE_MANAGER.getModule(FontModule.class).global.getValue()) {
            MatrixStack matrices = new MatrixStack();

            matrices.push();
            matrices.multiplyPositionMatrix(matrix);

            if (shadow) Sydney.FONT_MANAGER.getFontRenderer().drawText(matrices, text, x + Sydney.FONT_MANAGER.getShadowOffset(), y + Sydney.FONT_MANAGER.getShadowOffset(), color, true);
            Sydney.FONT_MANAGER.getFontRenderer().drawText(matrices, text, x, y, color, false);

            matrices.pop();

            info.setReturnValue(x + Sydney.FONT_MANAGER.getFontRenderer().getTextWidth(text));
        }
    }

    @Inject(method = "getWidth(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private void getWidth(String text, CallbackInfoReturnable<Integer> info) {
        if (Sydney.MODULE_MANAGER.getModule(FontModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(FontModule.class).customFont.getValue() && Sydney.MODULE_MANAGER.getModule(FontModule.class).global.getValue()) {
            info.setReturnValue(Sydney.FONT_MANAGER.getWidth(text));
        }
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/StringVisitable;)I", at = @At("HEAD"), cancellable = true)
    private void getWidth(StringVisitable text, CallbackInfoReturnable<Integer> info) {
        if (Sydney.MODULE_MANAGER.getModule(FontModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(FontModule.class).customFont.getValue() && Sydney.MODULE_MANAGER.getModule(FontModule.class).global.getValue()) {
            info.setReturnValue(Sydney.FONT_MANAGER.getWidth(text.getString()));
        }
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/OrderedText;)I", at = @At("HEAD"), cancellable = true)
    private void getWidth(OrderedText text, CallbackInfoReturnable<Integer> info) {
        if (Sydney.MODULE_MANAGER.getModule(FontModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(FontModule.class).customFont.getValue() && Sydney.MODULE_MANAGER.getModule(FontModule.class).global.getValue()) {
            info.setReturnValue((int) Sydney.FONT_MANAGER.getFontRenderer().getTextWidth(text));
        }
    }
}

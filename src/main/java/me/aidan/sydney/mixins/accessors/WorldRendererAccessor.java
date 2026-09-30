package me.aidan.sydney.mixins.accessors;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("frustum")
    Frustum getFrustum();

    @Accessor("entityOutlinesFramebuffer")
    Framebuffer getEntityOutlineFramebuffer();

    @Accessor("entityOutlinesFramebuffer")
    void setEntityOutlineFramebuffer(Framebuffer framebuffer);
}

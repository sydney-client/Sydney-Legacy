package me.aidan.sydney.mixins.accessors;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExplosionS2CPacket.class)
public interface ExplosionS2CPacketAccessor {
    @Accessor("playerVelocityX") @Mutable
    void setPlayerVelocityX(float velocity);

    @Accessor("playerVelocityY") @Mutable
    void setPlayerVelocityY(float velocity);

    @Accessor("playerVelocityZ") @Mutable
    void setPlayerVelocityZ(float velocity);
}
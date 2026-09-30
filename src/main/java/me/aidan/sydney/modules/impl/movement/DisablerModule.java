package me.aidan.sydney.modules.impl.movement;

import me.aidan.sydney.events.SubscribeEvent;
import me.aidan.sydney.events.impl.PacketSendEvent;
import me.aidan.sydney.mixins.accessors.PlayerMoveC2SPacketAccessor;
import me.aidan.sydney.modules.Module;
import me.aidan.sydney.modules.RegisterModule;
import me.aidan.sydney.utils.minecraft.NetworkUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@RegisterModule(name = "Disabler", description = "Disables Grim.", category = Module.Category.MOVEMENT)
public class DisablerModule extends Module {
    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (packet.changesLook()) {
                if (packet.changesPosition()) NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.Full(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getZ()), packet.getYaw(mc.player.getYaw()) + 11813401, packet.getPitch(mc.player.getPitch()), packet.isOnGround()));
                else NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.LookAndOnGround(packet.getYaw(mc.player.getYaw()) + 11813401, packet.getPitch(mc.player.getPitch()), packet.isOnGround()));

                ((PlayerMoveC2SPacketAccessor) packet).setYaw(packet.getYaw(mc.player.getYaw()) + 11813400);
            } else {
                if (packet.changesPosition()) {
                    NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.Full(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getZ()), packet.getYaw(mc.player.getYaw()) + 11813401, packet.getPitch(mc.player.getPitch()), packet.isOnGround()));
                    NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.Full(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getZ()), packet.getYaw(mc.player.getYaw()) + 11813400, packet.getPitch(mc.player.getPitch()), packet.isOnGround()));
                    event.setCancelled(true);
                } else {
                    NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.LookAndOnGround(packet.getYaw(mc.player.getYaw()) + 11813401, packet.getPitch(mc.player.getPitch()), packet.isOnGround()));
                    NetworkUtils.sendIgnoredPacket(new PlayerMoveC2SPacket.LookAndOnGround(packet.getYaw(mc.player.getYaw()) + 11813400, packet.getPitch(mc.player.getPitch()), packet.isOnGround()));
                    event.setCancelled(true);
                }
            }
        }
    }
}

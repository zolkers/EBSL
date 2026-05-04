package fr.riege.ebsl.mixin;

import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.event.events.network.NetworkPacketEvent;
import fr.riege.ebsl.packet.PacketDirection;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public final class ConnectionPacketCaptureMixin {
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD"))
    private void ebsl$onPacketInbound(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        EbslMod.postClientEvent(new NetworkPacketEvent(PacketDirection.INBOUND, packet));
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
        at = @At("HEAD"))
    private void ebsl$onPacketOutbound(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        EbslMod.postClientEvent(new NetworkPacketEvent(PacketDirection.OUTBOUND, packet));
    }
}

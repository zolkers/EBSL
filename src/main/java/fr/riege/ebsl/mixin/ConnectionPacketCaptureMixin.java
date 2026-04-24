package fr.riege.ebsl.mixin;

import fr.riege.ebsl.packet.PacketCaptureLog;
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
    private void ebsl$captureInboundPacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        PacketCaptureLog.record(PacketDirection.INBOUND, packet);
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
        at = @At("HEAD"))
    private void ebsl$captureOutboundPacket(Packet<?> packet, ChannelFutureListener listener,
                                            boolean flush, CallbackInfo ci) {
        PacketCaptureLog.record(PacketDirection.OUTBOUND, packet);
    }
}

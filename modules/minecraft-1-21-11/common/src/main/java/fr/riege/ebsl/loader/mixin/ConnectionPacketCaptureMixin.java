/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.loader.mixin;

import fr.riege.ebsl.common.domain.packet.PacketCaptureLog;
import fr.riege.ebsl.common.domain.packet.PacketDirection;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public final class ConnectionPacketCaptureMixin {
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD"))
    private void ebslOnInbound(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if (packet == null) return;
        PacketType<?> type = packet.type();
        PacketCaptureLog.recordTrace(PacketCaptureLog.buildEvent(
            PacketDirection.INBOUND,
            type.id().toString(),
            packet.getClass().getSimpleName(),
            packet.isTerminal(),
            packet.isSkippable()));
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
        at = @At("HEAD"))
    private void ebslOnOutbound(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        if (packet == null) return;
        PacketType<?> type = packet.type();
        PacketCaptureLog.recordTrace(PacketCaptureLog.buildEvent(
            PacketDirection.OUTBOUND,
            type.id().toString(),
            packet.getClass().getSimpleName(),
            packet.isTerminal(),
            packet.isSkippable()));
    }
}

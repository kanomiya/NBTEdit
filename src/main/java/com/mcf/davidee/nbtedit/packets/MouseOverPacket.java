package com.mcf.davidee.nbtedit.packets;

import static com.mcf.davidee.nbtedit.NBTEdit.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.RayTraceResult;

import com.mcf.davidee.nbtedit.NBTEdit;

public class MouseOverPacket extends AbstractPacket {

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {

	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {

	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		RayTraceResult pos = Minecraft.getMinecraft().objectMouseOver;
		AbstractPacket packet = null;
		if (pos != null)
			if (pos.entityHit != null)
				packet = new EntityRequestPacket(pos.entityHit.getEntityId());
			else if (pos.typeOfHit == RayTraceResult.Type.BLOCK)
				packet = new TileRequestPacket(pos.getBlockPos());
		if (packet == null)
			sendMessageToPlayer(player, SECTION_SIGN + "cError - No tile or entity selected");
		else
			NBTEdit.DISPATCHER.sendToServer(packet);
	}

	@Override
	public void handleServerSide(EntityPlayerMP player) {

	}

}

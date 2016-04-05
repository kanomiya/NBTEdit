package com.mcf.davidee.nbtedit.packets;

import static com.mcf.davidee.nbtedit.NBTEdit.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import com.mcf.davidee.nbtedit.NBTEdit;

public class TileRequestPacket extends AbstractPacket {

	private BlockPos pos;

	public TileRequestPacket() {

	}

	public TileRequestPacket(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(pos.getX());
		buffer.writeInt(pos.getY());
		buffer.writeInt(pos.getZ());
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
	}

	@Override
	public void handleClientSide(EntityPlayer player) {

	}

	@Override
	public void handleServerSide(EntityPlayerMP player) {
		TileEntity te = player.worldObj.getTileEntity(pos);
		if (te != null) {
			NBTTagCompound tag = new NBTTagCompound();
			te.writeToNBT(tag);
			NBTEdit.DISPATCHER.sendTo(new TileNBTPacket(pos, tag), player);
		}
		else
			sendMessageToPlayer(player, SECTION_SIGN + "cError - There is no TileEntity at "+pos.getX()+","+pos.getY()+","+pos.getZ());
	}

}

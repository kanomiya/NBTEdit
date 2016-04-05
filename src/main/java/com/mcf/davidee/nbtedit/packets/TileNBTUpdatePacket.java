package com.mcf.davidee.nbtedit.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import com.mcf.davidee.nbtedit.NBTEdit;

public class TileNBTUpdatePacket extends TileNBTPacket {

	public TileNBTUpdatePacket() {
		super();
	}

	public TileNBTUpdatePacket(BlockPos pos, NBTTagCompound tag) {
		super(pos, tag);
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		//TODO Work on this
		TileEntity te = player.worldObj.getTileEntity(pos);
		if (te != null) {
			NBTTagCompound backup = new NBTTagCompound();
			te.writeToNBT(backup);

			try {
				te.readFromNBT(tag);
			}
			catch(Throwable t) {
				te.readFromNBT(backup);
				NBTEdit.throwing(te.toString(), "readFromNBT", t);
			}
		}
	}

	@Override
	public void handleServerSide(EntityPlayerMP player) {

	}
}

package com.mcf.davidee.nbtedit;

import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import com.mcf.davidee.nbtedit.packets.EntityRequestPacket;
import com.mcf.davidee.nbtedit.packets.MouseOverPacket;
import com.mcf.davidee.nbtedit.packets.TileRequestPacket;


public class CommandNBTEdit extends CommandBase{

	@Override
	public String getCommandName() {
		return "nbtedit";
	}
	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender)
	{
		return "/nbtedit OR /nbtedit <EntityId> OR /nbtedit <TileX> <TileY> <TileZ>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] var2) throws NumberInvalidException, WrongUsageException {
		if (var2.length == 0) {
			// sender.addChatMessage(new TextComponentString("" + sender.getEntityWorld().isRemote)); ??
		}
		if (sender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)sender;

			if (var2.length == 3) {
				int x = parseInt(var2[0]);
				int y = parseInt(var2[1]);
				int z = parseInt(var2[2]);
				NBTEdit.log(Level.FINE, sender.getName() + " issued command \"/nbtedit " + x + " " + y + " " + z + "\"");
				new TileRequestPacket(new BlockPos(x,y,z)).handleServerSide(player);
			}
			else if (var2.length == 1) {
				int entityID = (var2[0].equalsIgnoreCase("me")) ? player.getEntityId() : parseInt(var2[0], 0);
				NBTEdit.log(Level.FINE, sender.getName() + " issued command \"/nbtedit " + entityID +  "\"");
				new EntityRequestPacket(entityID).handleServerSide(player);
			}
			else if (var2.length == 0) {
				NBTEdit.log(Level.FINE, sender.getName() + " issued command \"/nbtedit\"");
				NBTEdit.DISPATCHER.sendTo(new MouseOverPacket(), player);
			}
			else  {
				String s = "";
				for (int i =0; i < var2.length; ++i) {
					s += var2[i];
					if (i != var2.length - 1)
						s += " ";
				}
				NBTEdit.log(Level.FINE, sender.getName() + " issued invalid command \"/nbtedit " + s + "\"");
				throw new WrongUsageException("Pass 0, 1, or 3 integers -- ex. /nbtedit");
			}
		}
	}

	@Override
    public boolean checkPermission(MinecraftServer server, ICommandSender s)
	{
		return s instanceof EntityPlayer && (super.checkPermission(server, s) || !NBTEdit.opOnly && ((EntityPlayer)s).capabilities.isCreativeMode);
	}

}

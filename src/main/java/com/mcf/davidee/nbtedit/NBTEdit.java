package com.mcf.davidee.nbtedit;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import com.mcf.davidee.nbtedit.forge.CommonProxy;
import com.mcf.davidee.nbtedit.nbt.NBTNodeSorter;
import com.mcf.davidee.nbtedit.nbt.NBTTree;
import com.mcf.davidee.nbtedit.nbt.NamedNBT;
import com.mcf.davidee.nbtedit.nbt.SaveStates;
import com.mcf.davidee.nbtedit.packets.PacketPipeline;

@Mod(modid = NBTEdit.MODID, name = NBTEdit.NAME,  version = NBTEdit.VERSION, acceptableRemoteVersions="*")
public class NBTEdit {

	public static final String MODID = "NBTEdit";
	public static final String NAME = "In-game NBTEdit";
	public static final String VERSION = "1.9-1.0";

	private static final String SEP = System.getProperty("line.separator");
	public static final NBTNodeSorter SORTER = new NBTNodeSorter();
	public static final PacketPipeline DISPATCHER = new PacketPipeline();
	public static final char SECTION_SIGN = '\u00A7';

	private static FileHandler logHandler = null;
	private static Logger logger = Logger.getLogger("NBTEdit");

	public static NamedNBT clipboard = null;
	public static boolean opOnly = true;

	@Instance(MODID)
	private static NBTEdit instance;

	@SidedProxy(clientSide = "com.mcf.davidee.nbtedit.forge.ClientProxy", serverSide = "com.mcf.davidee.nbtedit.forge.CommonProxy")
	public static CommonProxy proxy;

	public static void log(Level l, String s){
		logger.log(l, s);
	}

	public static void throwing(String cls, String mthd, Throwable thr){
		logger.throwing(cls, mthd, thr);
	}

	public static void logTag(NBTTagCompound tag){
		NBTTree tree = new NBTTree(tag);
		String sb = "";
		for (String s : tree.toStrings()){
			sb += SEP + "\t\t\t"+ s;
		}
		NBTEdit.log(Level.FINE, sb);
	}

	public static SaveStates getSaveStates(){
		return instance.saves;
	}

	private SaveStates saves;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		AddMeta(event);

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		opOnly = config.get("General", "opOnly", true, "true if only Ops can NBTEdit; false allows users in creative mode to NBTEdit").getBoolean(true);

		if(config.hasChanged())
			config.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event){
		logger.setLevel(Level.ALL);

		try {
			File logfile = new File(proxy.getMinecraftDirectory(),"NBTEdit.log");
			if ((logfile.exists() || logfile.createNewFile()) && logfile.canWrite() && logHandler == null)
			{
				logHandler = new FileHandler(logfile.getPath());
				logHandler.setFormatter(new LogFormatter());
				logger.addHandler(logHandler);
			}
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}


		logger.fine("NBTEdit Initalized");
		saves = new SaveStates(new File(new File(proxy.getMinecraftDirectory(),"saves"),"NBTEdit.dat"));
		DISPATCHER.initialize();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		proxy.registerInformation();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		MinecraftServer server= event.getServer();
		ServerCommandManager serverCommandManager = (ServerCommandManager) server.getCommandManager();
		serverCommandManager.registerCommand(new CommandNBTEdit());
		logger.fine("Server Starting -- Added \"/nbtedit\" command");
	}

	private void AddMeta(FMLPreInitializationEvent event)
	{
		ModMetadata m = event.getModMetadata();
		m.autogenerated = false;
		m.modId = MODID;
		m.version = VERSION;
		m.name = NAME;
		m.authorList.add("Davidee");

		m.credits = "Thanks to Mojang, Forge, and all your support.";
		m.description = "Allows you to edit NBT Tags in-game.\nPlease visit the URL above for help.";
		m.url = "http://www.minecraftforum.net/topic/1558668-151/";
	}
}

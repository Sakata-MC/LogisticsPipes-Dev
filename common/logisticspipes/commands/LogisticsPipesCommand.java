package logisticspipes.commands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.oldpackets.PacketLogisticsPipes;
import logisticspipes.network.packets.debuggui.DebugAskForTarget;
import logisticspipes.proxy.MainProxy;
import logisticspipes.ticks.DebugGuiTickHandler;
import logisticspipes.ticks.RoutingTableUpdateThread;
import logisticspipes.ticks.VersionChecker;
import logisticspipes.ticks.Watchdog;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.network.Player;

public class LogisticsPipesCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "logisticspipes";
	}

	@Override
	public String getCommandUsage(ICommandSender var1) {
		return "/" + getCommandName() + " help";
	}

	@Override
	public List<String> getCommandAliases() {
		return Arrays.asList(new String[]{"lp", "logipipes"});
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) {
		if (arguments.length <= 0) {
			throw new WrongUsageException("Type '" + this.getCommandUsage(sender) + "' for help.");
		}
		if(arguments[0].equalsIgnoreCase("version")) {
        	commandVersion(sender, arguments);
        	return;
        } else if(arguments[0].equalsIgnoreCase("changelog")) {
        	commandChangelog(sender, arguments);
        	return;
        } else if(arguments[0].equalsIgnoreCase("NBTDEBUG")) {
        	if(!(sender instanceof Player)) {
        		throw new WrongUsageException("You can't use this command.");
        	}
        	sender.sendChatToPlayer("Trying to Enable NBTDebug");
        	MainProxy.sendPacketToPlayer(new PacketLogisticsPipes() {
				@Override public void writeData(DataOutputStream data) throws IOException {}
				@Override public void readData(DataInputStream data) throws IOException {}
				@Override public int getID() {return NetworkConstants.ACTIVATNBTDEBUG;}
			}.getPacket(), (Player)sender);
        	return;
        } else if(arguments[0].equalsIgnoreCase("routingthread") || arguments[0].equalsIgnoreCase("rt")) {
        	sender.sendChatToPlayer("RoutingTableUpdateThread: TODO: " + RoutingTableUpdateThread.size());
        	return;
        } else if(arguments[0].equalsIgnoreCase("transfernames") || arguments[0].equalsIgnoreCase("tn")) {
        	if(!(sender instanceof Player)) {
        		throw new WrongUsageException("You can't use this command.");
        	}
        	sender.sendChatToPlayer("Requesting Transfer");
        	MainProxy.sendPacketToPlayer(new PacketLogisticsPipes() {
				@Override public void writeData(DataOutputStream data) throws IOException {}
				@Override public void readData(DataInputStream data) throws IOException {}
				@Override public int getID() {return NetworkConstants.REQUEST_UPDATE_NAMES;}
			}.getPacket(), (Player)sender);
        	MainProxy.proxy.sendNameUpdateRequest((Player)sender);
        	return;
        } else if(arguments[0].equalsIgnoreCase("name") && arguments.length == 3) {
        	String idString = arguments[1];
        	String metaString = arguments[2];
        	int id = Integer.valueOf(idString);
        	int meta = Integer.valueOf(metaString);
        	ItemIdentifier item = ItemIdentifier.get(id, meta, null);
        	sender.sendChatToPlayer("Name: " + item.getFriendlyNameCC());
        	return;
        } else if(arguments[0].equalsIgnoreCase("help")) {
        	sender.sendChatToPlayer("Format: '"+ this.getCommandName() +" <command> <arguments>'");
        	sender.sendChatToPlayer("Available commands:");
        	sender.sendChatToPlayer("- version : Version information.");
        	if(VersionChecker.hasNewVersion) sender.sendChatToPlayer("- changelog : Shows the changelog for the new version.");
        	sender.sendChatToPlayer("- routingthread : Display Routing thread status information.");
        	sender.sendChatToPlayer("- transfernames : Sends all item names form the client to the server to update the Language Database.");//TODO
        	return;
        } else if(arguments[0].equalsIgnoreCase("dump")) {
			Watchdog.dump(false, false, true);
			sender.sendChatToPlayer("Dump Created");
        	return;
        }
		if(LogisticsPipes.DEBUG) {
			if(arguments[0].equalsIgnoreCase("watch")) {
	        	new Watchdog(MainProxy.proxy.getSide().equals("Client"));
				LogisticsPipes.WATCHDOG = true;
	        	sender.sendChatToPlayer("Starting Watchdog");
	        	return;
	        }
		}
		if(LogisticsPipes.DEBUG || sender.getCommandSenderName().equals("davboecki") || sender.getCommandSenderName().equals("theZorro266")) {
			if(arguments[0].equalsIgnoreCase("debug")) {
	        	try {
	        		if(arguments[1].equalsIgnoreCase("me")) {
	        			DebugGuiTickHandler.instance().startWatchingOf(sender, (Player)sender);
	        			sender.sendChatToPlayer("Starting SelfDebuging");
	        			return;
	    	       	} else if(arguments[1].equalsIgnoreCase("look") || arguments[1].equalsIgnoreCase("watch") || arguments[1].equalsIgnoreCase("target")) {
	        			if(sender instanceof EntityPlayer) {
	        				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DebugAskForTarget.class).getPacket(), (Player) sender);
	        				sender.sendChatToPlayer("Asking for Target.");
	        			} else {
	        				sender.sendChatToPlayer("No player asking!");
	        			}
	        			return;
	        		}
	        	} catch(Exception e) {
	        		e.printStackTrace();
	        	}
	        	return;
	        }
        }
    	throw new WrongUsageException(this.getCommandUsage(sender));
	}

	private void commandVersion(ICommandSender sender, String[] arguments) {
    	sender.sendChatToPlayer(String.format("LogisticsPipes %s for Minecraft %s.", LogisticsPipes.class.getAnnotation(Mod.class).version(), LogisticsPipes.MCVersion));
    	if(VersionChecker.hasNewVersion) {
    		sender.sendChatToPlayer("Your LogisticsPipes version is outdated. The newest version is #" + VersionChecker.newVersion + ".");
    		sender.sendChatToPlayer("Use \"/logisticspipes changelog\" to see a changelog.");
        }
	}

	private void commandChangelog(ICommandSender sender, String[] arguments) {
    	if(VersionChecker.hasNewVersion) {
    		sender.sendChatToPlayer("The newest version is #" + VersionChecker.newVersion + ".");
			sender.sendChatToPlayer("Changelog:");
    		for(String msg:VersionChecker.changeLog) {
    			sender.sendChatToPlayer(msg);
    		}
        }
	}
}

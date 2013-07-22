package org.collidingfate.cfrandomteleport;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.WorldBorder;

public class CFRandomTeleport extends JavaPlugin implements Listener {

	protected Logger log;
	protected PluginManager pm;
	
	private Random random;
	
	// Configuration
	private boolean useWorldBorder; // Hook into WorldBorder instead of our own config?
	private String teleportOnSpecificWorld;
	//private Map<String, WorldSettings> configWorlds; // Our own world configs
	
	@Override
	public void onEnable() {
		log = getLogger();
		pm = getServer().getPluginManager();
		random = new Random(System.currentTimeMillis());
		
		// Not using WorldSettings for now
		//ConfigurationSerialization.registerClass(WorldSettings.class, "CFRandomTeleport-World");
		
		FileConfiguration config = getConfig();
		config.options().copyDefaults(true).copyHeader(true);
		saveConfig();
		
		useWorldBorder = config.getBoolean("use-worldborder");
		if (useWorldBorder != true) {
			log.severe("Unable to start CFPromote; Only WorldBorder world settings are supported right now.");
			pm.disablePlugin(this);
			return;
		}
		
		teleportOnSpecificWorld = config.getString("teleport-on-specific-world");
		if (!teleportOnSpecificWorld.isEmpty()) {
			log.fine(String.format("Players will teleport around the world '%s'", teleportOnSpecificWorld));
			World foundWorld = null;
			for (World checkWorld : getServer().getWorlds()) {
				if (checkWorld.getName().equalsIgnoreCase(teleportOnSpecificWorld)) {
					foundWorld = checkWorld;
					break;
				}
			}
			if (foundWorld == null) {
				log.warning(String.format("The world '%s' set in the config option 'teleport-on-specific-world' doesn't exist on the server. Random teleportation probably won't work!", teleportOnSpecificWorld));
			}
		} else {
			log.fine("Players will teleport within their current world");
		}
		
		if (pm.getPlugin("WorldBorder") == null) {
			log.severe("Unable to start CFRandomTeleport; WorldBorder wasn't found on the server.");
			pm.disablePlugin(this);
			return;
		}
		
		log.info("Enabled!");
	}
	
	@Override
	public void onDisable() {
		//ConfigurationSerialization.unregisterClass(WorldSettings.class);
		
		// Release all our handles now. This is helpful for the garbage
		// collector if the plugin object is kept after being disabled.
		log = null;
		pm = null;
		random = null;
		
		getLogger().info("Disabled!");
	}

	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("cfrtp")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				World tpWorld;
				if (!teleportOnSpecificWorld.isEmpty()) {
					tpWorld = getServer().getWorld(teleportOnSpecificWorld);
					if (tpWorld == null) {
						log.severe(String.format("The world '%1$s' set in the config option 'teleport-on-specific-world' doesn't exist on the server. Unable to randomly teleport %2$s!", teleportOnSpecificWorld, player.getName()));
						sender.sendMessage(ChatColor.RED + "Unable to find a suitable location to teleport to!");
						return true;
					}
				} else {
					tpWorld = player.getWorld();
				}
				Location location = findSuitableLocationWB(tpWorld);
				if (location == null) {
					sender.sendMessage(ChatColor.RED + "Unable to find a suitable location to teleport to!");
					return true;
				}
				boolean success = player.teleport(location, TeleportCause.COMMAND);
				if (success) {
					player.sendMessage(new String[] {
						String.format("%1$sYou have been teleported to a random location!", ChatColor.GREEN),
						String.format("%1$sWorld: %2$s%3$s", ChatColor.AQUA, ChatColor.YELLOW, location.getWorld().getName()),
						String.format("%1$sX: %2$s%3$f%1$s, Y: %2$s%4$f%1$s, Z: %2$s%5$f%1$s", ChatColor.AQUA, ChatColor.YELLOW, location.getX(), location.getY(), location.getZ()),
						String.format("%1$sBiome: %2$s%3$s", ChatColor.AQUA, ChatColor.YELLOW, location.getBlock().getBiome())
					});
				} else {
					player.sendMessage(ChatColor.RED + "Unable to teleport you to the random location! Seek the server staff for assistance.");
					log.severe(String.format(ChatColor.RED + "Unable to randomly teleport %1$s to %2$s ! Maybe another plugin blocked the teleport?", player.toString(), location.toString()));
				}
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
				return true;
			}
		}
		
		return false;
    }
	
	/**
	 * Get a random 2D (X,Z) point within an ellipse.
	 * 
	 * Adapted from the algorithm given here: http://stackoverflow.com/a/5838055
	 * 
	 * @param random  random number generator
	 * @param radiusX  ellipse radius along X coordinate
	 * @param radiusZ  ellipse radius along Z coordinate
	 * @return  Vector with coordinates X and Z set
	 */
	private Vector getRandomPointInEllipse(Random random, double radiusX, double radiusZ) {
		double t = 2.0 * Math.PI * random.nextDouble();
		double u = random.nextDouble() + random.nextDouble();
		double r = u > 1.0 ? 2.0 - u : u;
		return new Vector(radiusX * r * Math.cos(t), 0.0, radiusZ * r * Math.sin(t));
	}

	/**
	 * Get a random 2D (X,Z) point within a rectangle.
	 * 
	 * @param random  random number generator
	 * @param radius  rectangle radius (half-length) along X coordinate
	 * @param radius  rectangle radius (half-length) along Z coordinate
	 * @return  Vector with coordinates X and Z set
	 */
	private Vector getRandomPointInRectangle(Random random, double radiusX, double radiusZ) {
		double x = (2.0 * random.nextDouble()) - 1.0; // [-1,1)
		double z = (2.0 * random.nextDouble()) - 1.0; // [-1,1)
		return new Vector(radiusX * x, 0.0, radiusZ * z);
	}
	
	/**
	 * Get a random yaw rotation (degrees) from -180 to 180.
	 * @return
	 */
	private float getRandomYaw() {
		return (360.0f * random.nextFloat()) - 180.0f; // [-180,180)
	}
	
	/**
	 * Find a suitable location for a player to teleport to in the given world.
	 * This method uses WorldBorder as the configuration source.
	 * 
	 * @param world  World to teleport to
	 * @return  Location to teleport to
	 */
	private Location findSuitableLocationWB(World world) {
		BorderData borderData = WorldBorder.plugin.GetWorldBorder(world.getName());
		
		if (borderData == null) {
			//throw new IllegalStateException(String.format("World %1$s isn't configured in WorldBorder.", world.getName()));
			return null;
		}

		for (int i = 0; i < 100; i++) {
			Vector position = new Vector(borderData.getX(), 0.0, borderData.getZ());

			// Get a uniform-area random position within the world border's geometry
			boolean isRound = (borderData.getShape() == null) ? true : borderData.getShape();
			if (isRound) {
				position.add(getRandomPointInEllipse(random, borderData.getRadiusX(), borderData.getRadiusZ()));
			} else {
				position.add(getRandomPointInRectangle(random, borderData.getRadiusX(), borderData.getRadiusZ()));
			}

			// Ensure there's a solid block to stand on
			Block highestBlock = world.getHighestBlockAt(position.getBlockX(), position.getBlockZ());
			if (highestBlock == null)
				continue;
			highestBlock = highestBlock.getRelative(0, -1, 0); // Because the javadocs are wrong.
			if (highestBlock == null)
				continue;
			if (highestBlock.getY() < 1 || highestBlock.getY() >= world.getMaxHeight() - 2)
				continue;
			if (highestBlock.isLiquid())
				continue;

			position.setX((double) position.getBlockX() + 0.5);
			position.setY(highestBlock.getY() + 2);
			position.setZ((double) position.getBlockZ() + 0.5);

			return position.toLocation(world, getRandomYaw(), 0.0f);
		}
		
		return null;
	}
	
	/* Old version */
	/*private Location findSuitableLocation(World world) {
		WorldSettings settings = configWorlds.get(world.getName());
		if (settings == null) {
			throw new IllegalStateException(String.format("World %1$s isn't configured for random teleportation.", world.getName()));
		}

		Vector position = new Vector(settings.getCenterX(), 0.0, settings.getCenterZ());
		
		if (settings.getShape() == WorldSettings.Shape.CIRCLE) {
			position.add(getRandomPointInCircle(random, settings.getRadius()));
		} else if (settings.getShape() == WorldSettings.Shape.SQUARE) {
			position.add(getRandomPointInSquare(random, settings.getRadius()));
		} else {
			throw new IllegalStateException(String.format("World %1$s doesn't have a valid shape configured.", world.getName()));
		}
		
		return position.toLocation(world, getRandomYaw(), 0.0f);
	}*/
	
}

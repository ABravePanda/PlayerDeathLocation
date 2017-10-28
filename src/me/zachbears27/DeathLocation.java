package me.zachbears27;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;

import me.zachbears27.MySQL;
import me.zachbears27.InventoryStringDeSerializer;
import net.md_5.bungee.api.ChatColor;

// This Plugin Is Made For MumboCraft & It's Players & Staff 
// Any misuse or re-distribution of this plugin will call for said plugin to be removed
// Written by A_Brave_Panda & Jamdoggy

public class DeathLocation extends JavaPlugin implements Listener {
	
	private MySQL MySQLC = null;
	private Connection c = null;
	
	public ItemStack[] contents;

	// Retrieve MySQL database connection details from the config.yml file
	String host     = getConfig().getString("Hostname");
	String port     = getConfig().getString("Port");
	String db       = getConfig().getString("Database");
	String username = getConfig().getString("Username");
	String password = getConfig().getString("Password");
	String table    = getConfig().getString("Table");
			
 
	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		registerConfig();
		
		this.MySQLC = new MySQL(host, port, db, username, password);
		try {
			this.c = this.MySQLC.openConnection();
		} catch (ClassNotFoundException er) {
			er.printStackTrace();
		} catch (SQLException er) {
			er.printStackTrace();
		}
	}
 
	public void registerConfig() {
		saveDefaultConfig();
	}

 	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {

		if (e.getEntity() instanceof Player) {
			Player p = e.getEntity();
			Location loc = e.getEntity().getLocation();
			double x = loc.getBlockX() + 0.5;
			double y = loc.getBlockY();
			double z = loc.getBlockZ() + 0.5;
			String dr = e.getDeathMessage();
			String world = loc.getWorld().getName();
			java.util.Date dt = new java.util.Date();

			java.text.SimpleDateFormat sdf = 
					new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			String time = sdf.format(dt);
			///inventoryContents.put(p.getName(), p.getInventory().getContents());
			//inventoryArmorContents.put(p.getName(), p.getInventory().getArmorContents());
      
			String statementstring = "SELECT * FROM " + table + " WHERE PlayerName = '" + p.getName() +"';";
			String statementstring2 = "INSERT INTO "+ table + " (id, PlayerName, X, Y, Z, DeathReason, world,Time) VALUES (NULL, '" + p.getName() + "', '" + x + "', '" + y + "', '" + z + "', '" + dr + "', '" + world + "', '" + time + "');";
			
			this.MySQLC = new MySQL(host, port, db, username, password);
			try {
				this.c = this.MySQLC.openConnection();
			} catch (ClassNotFoundException er) {
				er.printStackTrace();
			} catch (SQLException er) {
				er.printStackTrace();
			}
		
			try {

				Statement statement = c.createStatement();
				ResultSet res = statement.executeQuery(statementstring);
				if(res.next()) {  // Player already has a death in the database, update it
					String statementstring3 = "UPDATE " + table + " SET X='" + x + "', Y='" + y + "', Z='" + z + "', DeathReason='" + dr + "', world='" + world + "',Time='" + time + "' WHERE id='" + res.getInt("id") + "';";
					statement.executeUpdate(statementstring3);
				} else {         // First time player has died, create a new death entry
					statement.executeUpdate(statementstring2);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		
		
		//When the player dies, save the inventory
			try {
				Statement statement = c.createStatement();
				String statementstring333 = "SELECT * FROM invs WHERE name = '" + p.getName() +"';";
				ResultSet res = statement.executeQuery(statementstring333);
				ItemStack[] items = e.getEntity().getInventory().getContents();
				String n = InventoryStringDeSerializer.itemStackArrayToBase64(items);
				if(res.next()) {  // Player already has an inv
					String statementstring3 = "UPDATE invs SET name='" + p.getName() + "', itemstack = '" + n + "' WHERE id='" + res.getInt("id") + "';";
					statement.executeUpdate(statementstring3);
				} else {         // First time player has died, create a new death entry
					String statementstring22 = "INSERT INTO `invs`(`id`, `name`, `itemstack`) VALUES (NULL, '" + e.getEntity().getName() + "','" + n + "')";
					statement.executeUpdate(statementstring22);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}	
		}
 	}
	

  
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player)sender;
		boolean tp = false;
		boolean load = false;
		String statementstring4 = null;
		if(cmd.getLabel().equalsIgnoreCase("death")) {
			

			this.MySQLC = new MySQL(host, port, db, username, password);
			try {
				this.c = this.MySQLC.openConnection();
			} catch (ClassNotFoundException er) {
				er.printStackTrace();
			} catch (SQLException er) {
				er.printStackTrace();
			}

			if(args.length != 0) {
				if(args[0].equals("help")) {
					p.sendMessage(ChatColor.YELLOW + "/death" + ChatColor.GOLD + "      : Displays the co-ordinates of your last death");
					p.sendMessage(ChatColor.YELLOW + "/death help" + ChatColor.GOLD + " : Displays this information page");
					if(p.hasPermission("death.others")) {	
						p.sendMessage(ChatColor.YELLOW + "/death <player>" + ChatColor.GOLD + "     : Displays the co-ordinates of players last death");
					}
					if(p.hasPermission("death.teleport")) {	
						p.sendMessage(ChatColor.YELLOW + "/death tp <player>" + ChatColor.GOLD + "  : Teleport to players last death location");
					}
					if(p.hasPermission("death.restore")) {	
						p.sendMessage(ChatColor.YELLOW + "/death restore <player>" + ChatColor.GOLD + "  : Restores players last inventory");
					}
				}
				else if(args[0].equals("restore")) {
					if(p.hasPermission("death.restore")) {
						try {
							Statement statement = c.createStatement(); // Player already has a death in the database, update it
								String statementstring3 = "SELECT * FROM invs WHERE name = '" + args[1] + "';";
								ResultSet res = statement.executeQuery(statementstring3);
								if(res.next()) {
								String itemstack = res.getString("itemstack");
								p.sendMessage(ChatColor.GREEN + "Inventory of " + ChatColor.GOLD + args[1] + ChatColor.GREEN + " has been restored.");
								
								ItemStack[] r = InventoryStringDeSerializer.itemStackArrayFromBase64(itemstack);
								
								Player target = Bukkit.getPlayer(args[1]);
								target.getInventory().setContents(r);
								target.sendMessage(ChatColor.GREEN + "Inventory restored by: " + ChatColor.GOLD + p.getDisplayName());
							
								}
						} catch (SQLException e2) {
							e2.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else if(args[0].equals("inventory")) {
					if(p.hasPermission("death.inventory")) {
						try {
							Statement statement = c.createStatement(); // Player already has a death in the database, update it
								String statementstring3 = "SELECT * FROM invs WHERE name = '" + args[1] + "';";
								ResultSet res = statement.executeQuery(statementstring3);
								if(res.next()) {
								String itemstack = res.getString("itemstack");
								
								
								ItemStack[] r = InventoryStringDeSerializer.itemStackArrayFromBase64(itemstack);
								
								Player target = Bukkit.getPlayer(args[1]);
								Inventory myInventory = Bukkit.createInventory(null, 54, target.getName() + "'s Inventory");
								myInventory.setContents(r);
								p.openInventory(myInventory);
							
								}
						} catch (SQLException e2) {
							e2.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else if(args[0].equals("tp")) {
					if(p.hasPermission("death.teleport")) {	
						String sanitised_arg = args[1].replace("\\","");
						sanitised_arg = sanitised_arg.replace("'","");
						statementstring4 = "SELECT * FROM " + table + " WHERE PlayerName = '" + sanitised_arg.toString() +"';";
						try {
							Statement statement = c.createStatement();
							ResultSet res = statement.executeQuery(statementstring4);
							if(res.next()) {
								Location l = new Location(Bukkit.getWorld(res.getString("world")), res.getDouble("X"), res.getInt("Y"), res.getDouble("Z"));
								p.teleport(l);
								p.sendMessage(ChatColor.GRAY +  "You have been teleported to " + ChatColor.GOLD + "" + ChatColor.BOLD + res.getString("PlayerName") + ChatColor.GRAY + "'s death location at [" +  ChatColor.GOLD + "" +
										res.getString("world") + ": " + res.getString("X") + ", " + res.getString("Y") + ", " + res.getString("Z") + ChatColor.GRAY + "] at: " + ChatColor.GOLD + "" + 
										res.getString("time") + ChatColor.GRAY + ".");
								p.sendMessage(ChatColor.GRAY + "Cause of Death: " + ChatColor.GOLD + res.getString("DeathReason") + ChatColor.GRAY + "." );
							} else {
								p.sendMessage(ChatColor.RED + args[1] + " hasn't died! (yet :D)");
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {
						p.sendMessage(ChatColor.RED + "You do not have permission to teleport to your death location.");
					}
					tp = true;
				} else if (p.hasPermission("death.others")) {
					// Player used /death <name> and has permission to do so
					String sanitised_arg = args[0].replace("\\","");
					sanitised_arg = sanitised_arg.replace("'","");
					statementstring4 = "SELECT * FROM " + table + " WHERE PlayerName = '" + sanitised_arg +"';";
				} else {
					// Player used /death <name> but doesn't have permission to do so
					statementstring4 = "SELECT * FROM " + table + " WHERE PlayerName = '" + p.getName() +"';";
				}
			} else {
				// Player just used /death with no args - use their own name
				statementstring4 = "SELECT * FROM " + table + " WHERE PlayerName = '" + p.getName() +"';";
			}
	  
			// If 'tp' wasn't used...
			if (tp == false || load == false) {
				try {
					Statement statement = c.createStatement();
					ResultSet res = statement.executeQuery(statementstring4);
					if(res.next()) {
						p.sendMessage(ChatColor.GRAY +  "Player " + ChatColor.GOLD + "" + ChatColor.BOLD + res.getString("PlayerName") + ChatColor.GRAY + " died at [" +  ChatColor.GOLD + "" +
								res.getString("world") + ": " + res.getString("X") + ", " + res.getString("Y") + ", " + res.getString("Z") + ChatColor.GRAY + "] at: " + ChatColor.GOLD + "" + 
								res.getString("time") + ChatColor.GRAY + ".");
						p.sendMessage(ChatColor.GRAY + "Cause of Death: " + ChatColor.GOLD + res.getString("DeathReason") + ChatColor.GRAY + "." );
					} else if(args.length != 0) {
						p.sendMessage(ChatColor.RED + args[0] + " hasn't died! (yet :D)");
					} else {
						p.sendMessage(ChatColor.RED + "You haven't died! (yet :D)");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}	
		}  // End command: death
		return true;
	}
}
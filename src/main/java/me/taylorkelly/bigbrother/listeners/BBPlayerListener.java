package me.taylorkelly.bigbrother.listeners;

import java.util.ArrayList;

import me.taylorkelly.bigbrother.ActionProvider;
import me.taylorkelly.bigbrother.BBCommand;
import me.taylorkelly.bigbrother.BBLogging;
import me.taylorkelly.bigbrother.BBPermissions;
import me.taylorkelly.bigbrother.BBPlayerInfo;
import me.taylorkelly.bigbrother.BBSettings;
import me.taylorkelly.bigbrother.BigBrother;
import me.taylorkelly.bigbrother.datablock.*;
import me.taylorkelly.bigbrother.tablemgrs.BBUsersTable;
import me.taylorkelly.util.ChestTools;
import net.nexisonline.bigbrother.ownership.OwnershipManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BBPlayerListener extends PlayerListener {
    
    private BigBrother plugin;
    
    public BBPlayerListener(BigBrother plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        try {
            //plugin.processPsuedotick();
            if (event == null || event.getPlayer() == null)
                return;
            String[] parts = BBCommand.groupArgs(event.getMessage().split(" "));
            String cmd = parts[0].toLowerCase();
            String msg = event.getMessage();
            
            // Perform censoring
            if (BBSettings.censoredCommands.contains(cmd)) {
                msg = cmd;
                if (parts.length > 1) {
                    for (int i = 1; i < parts.length; i++) {
                        msg += " ";
                        for (int j = 0; j < parts[i].length(); j++)
                            msg += "*";
                    }
                }
            }
            Player player = event.getPlayer();
            BBPlayerInfo pi = BBUsersTable.getInstance().getUserByName(player.getName());
            plugin.closeChestIfOpen(pi);
            if (!ActionProvider.isDisabled(Command.class) && pi.getWatched()) {
                Command dataBlock = new Command(player, msg, player.getWorld().getName());
                dataBlock.send();
            }
        } catch (Throwable e) {
            BBLogging.severe("onPlayerCommandPreprocess(" + event.toString() + ")", e);
        }
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            //plugin.processPsuedotick();
            if (event == null || event.getPlayer() == null)
                return;
            Player player = event.getPlayer();
            
            BBUsersTable.getInstance().addOrUpdateUser(player);
            BBPlayerInfo pi = BBUsersTable.getInstance().getUserByName(player.getName());
            
            if (!ActionProvider.isDisabled(Login.class) && pi.getWatched()) {
                Login dataBlock = new Login(player, player.getWorld().getName());
                dataBlock.send();
            }
            
            if (BBPermissions.info(player)) {
                plugin.sticker.onPlayerJoin(player, pi);
            }
            
            BBLogging.debug(player.getName() + " has Permissions: ");
            BBLogging.debug("- Watching privileges: " + BBPermissions.watch(player));
            BBLogging.debug("- Info privileges: " + BBPermissions.info(player));
            BBLogging.debug("- Rollback privileges: " + BBPermissions.rollback(player));
            BBLogging.debug("- Cleansing privileges: " + BBPermissions.cleanse(player));
        } catch (Throwable e) {
            BBLogging.severe("onPlayerJoin(" + event.toString() + ")", e);
        }
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            //plugin.processPsuedotick();
            if (event == null || event.getPlayer() == null)
                return;
            final Player player = event.getPlayer();
            BBPlayerInfo pi = BBUsersTable.getInstance().getUserByName(player.getName());
            plugin.closeChestIfOpen(pi);
            if (!ActionProvider.isDisabled(Disconnect.class) && pi.getWatched()) {
                Disconnect dataBlock = new Disconnect(player.getName(), player.getLocation(), player.getWorld().getName());
                dataBlock.send();
            }
        } catch (Throwable e) {
            BBLogging.severe("onPlayerQuit(" + event.toString() + ")", e);
        }
    }
    
    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        try {
            //plugin.processPsuedotick();
            if (event == null || event.getPlayer() == null)
                return;
            Location from = event.getFrom();
            Location to = event.getTo();
            
            final Player player = event.getPlayer();
            BBPlayerInfo pi = BBUsersTable.getInstance().getUserByName(player.getName());
            plugin.closeChestIfOpen(pi);
            if (!ActionProvider.isDisabled(Teleport.class) && pi.getWatched() && distance(from, to) > 5 && !event.isCancelled()) {
                Teleport dataBlock = new Teleport(player.getName(), event.getTo());
                dataBlock.send();
            }
        } catch (Throwable e) {
            BBLogging.severe("onPlayerTeleport(" + event.toString() + ")", e);
        }
    }
    
    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        try {
            //plugin.processPsuedotick();
            if (event == null || event.getPlayer() == null)
                return;
            final Player player = event.getPlayer();
            BBPlayerInfo pi = BBUsersTable.getInstance().getUserByName(player.getName());
            plugin.closeChestIfOpen(pi);
            if (!ActionProvider.isDisabled(Chat.class) && pi.getWatched()) {
                Chat dataBlock = new Chat(player, event.getMessage(), player.getWorld().getName());
                dataBlock.send();
            }
        } catch (Throwable e) {
            BBLogging.severe("onPlayerChat(" + event.toString() + ")", e);
        }
    }
    
    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        try {
            if (event == null || event.getPlayer() == null || event.getItem() == null)
                return;
            final Player player = event.getPlayer();
            BBPlayerInfo pi = BBUsersTable.getInstance().getUserByName(player.getName());
            if (!ActionProvider.isDisabled(PickupItem.class) && pi.getWatched()) {
                // It should not be null, but I have no other way to explain the NPEs.  Bukkit Bug?
                if (event.getItem() != null && event.getItem().getItemStack() != null) {
                    PickupItem dataBlock = new PickupItem(player.getName(), event.getItem(), event.getItem().getWorld().getName());
                    dataBlock.send();
                }
            }
        } catch (Throwable e) {
            BBLogging.severe("onPlayerPickupItem(" + event.toString() + ")", e);
        }
    }
    
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        try {
            if (event == null || event.getPlayer() == null || event.getItemDrop() == null || event.getItemDrop().getItemStack() == null)
                return;
            final Player player = event.getPlayer();
            BBPlayerInfo pi = BBUsersTable.getInstance().getUserByName(player.getName());
            if (!ActionProvider.isDisabled(DropItem.class) && pi.getWatched()) {
                DropItem dataBlock = new DropItem(player.getName(), event.getItemDrop(), event.getItemDrop().getWorld().getName());
                dataBlock.send();
            }
        } catch (Throwable e) {
            BBLogging.severe("onPlayerDropItem(" + event.toString() + ")", e);
        }
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            if (event == null || event.getPlayer() == null)
                return;
            //plugin.processPsuedotick();
            if (event.isCancelled())
                return;
            
            Player player = event.getPlayer();
            BBPlayerInfo pi = BBUsersTable.getInstance().getUserByName(player.getName());
            
            if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                if (BBPermissions.info(player) && plugin.hasStick(player, player.getItemInHand()) && plugin.leftClickStick(player)) {
                    // Process left-clicks (punch action on log, etc)
                    plugin.stick(player, event.getClickedBlock(), true);
                    
                    event.setCancelled(true); // Cancel in case of 1-hit breakable stuff like flowers.
                }
            }
            
            // Process right-clicking stuff.
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                // Process stick/log events first.
                if (BBPermissions.info(player) && plugin.hasStick(player, player.getItemInHand()) && plugin.rightClickStick(player)) {
                    // Get info
                    plugin.stick(player, event.getClickedBlock(), false);
                    
                    // Cancel any interactions.
                    ArrayList<Material> nonInteracts = new ArrayList<Material>();
                    nonInteracts.add(Material.WOOD_PLATE);
                    nonInteracts.add(Material.STONE_PLATE);
                    if (!nonInteracts.contains(event.getClickedBlock().getType())) {
                        event.setCancelled(true);
                    }
                    // Otherwise...
                } else if (!ActionProvider.isDisabled(PlacedBlock.class) && pi.getWatched()) {
                    int x;
                    int y;
                    int z;
                    int type;
                    PlacedBlock dataBlock;
                    World world;
                    Block block = event.getClickedBlock();
                    
                    plugin.closeChestIfOpen(pi);
                    if (block.getState() instanceof Chest) {
                        Chest chest = ((Chest) block.getState());
                        // OH SHI-
                        BBUsersTable.getInstance().userOpenedChest(player.getName(), chest, ChestTools.getChestContents(chest));
                        return;
                    }
                    switch (event.getMaterial()) {
                        //TODO Door logging
                        case LAVA_BUCKET:
                            x = event.getClickedBlock().getX() + event.getBlockFace().getModX();
                            y = event.getClickedBlock().getY() + event.getBlockFace().getModY();
                            z = event.getClickedBlock().getZ() + event.getBlockFace().getModZ();
                            type = Material.LAVA.getId();
                            world = event.getClickedBlock().getWorld();
                            dataBlock = new PlacedBlock(event.getPlayer().getName(), world.getName(), x, y, z, type, (byte) 0);
                            OwnershipManager.setOwnerLocation(new Location(world, x, y, z), pi);
                            dataBlock.send();
                            break;
                        case WATER_BUCKET:
                            x = event.getClickedBlock().getX() + event.getBlockFace().getModX();
                            y = event.getClickedBlock().getY() + event.getBlockFace().getModY();
                            z = event.getClickedBlock().getZ() + event.getBlockFace().getModZ();
                            type = Material.WATER.getId();
                            world = event.getClickedBlock().getWorld();
                            dataBlock = new PlacedBlock(event.getPlayer().getName(), world.getName(), x, y, z, type, (byte) 0);
                            OwnershipManager.setOwnerLocation(new Location(world, x, y, z), pi);
                            dataBlock.send();
                            break;
                        case SIGN:
                            x = event.getClickedBlock().getX() + event.getBlockFace().getModX();
                            y = event.getClickedBlock().getY() + event.getBlockFace().getModY();
                            z = event.getClickedBlock().getZ() + event.getBlockFace().getModZ();
                            world = event.getClickedBlock().getWorld();
                            
                            int data = 0;
                            switch (event.getBlockFace()) {
                                case UP:
                                    type = Material.SIGN_POST.getId();
                                    break;
                                case NORTH:
                                    data = 4;
                                    type = Material.WALL_SIGN.getId();
                                    break;
                                case SOUTH:
                                    data = 5;
                                    type = Material.WALL_SIGN.getId();
                                    break;
                                case EAST:
                                    data = 2;
                                    type = Material.WALL_SIGN.getId();
                                    break;
                                case WEST:
                                    data = 3;
                                    type = Material.WALL_SIGN.getId();
                                    break;
                                default:
                                    type = Material.SIGN.getId();
                            }
                            dataBlock = new PlacedBlock(event.getPlayer().getName(), world.getName(), x, y, z, type, (byte) data);
                            dataBlock.send();
                            break;
                        case BUCKET:
                            BrokenBlock dataBlock2;
                            world = event.getClickedBlock().getWorld();
                            switch (event.getClickedBlock().getType()) {
                                case STATIONARY_LAVA:
                                case LAVA:
                                    x = event.getClickedBlock().getX();
                                    y = event.getClickedBlock().getY();
                                    z = event.getClickedBlock().getZ();
                                    type = Material.LAVA.getId();
                                    dataBlock2 = new BrokenBlock(BBUsersTable.getInstance().getUserByName(event.getPlayer().getName()), world.getName(), x, y, z, type, (byte) 0);
                                    OwnershipManager.removeOwnerByLocation(event.getClickedBlock().getLocation());
                                    dataBlock2.send();
                                    break;
                                case STATIONARY_WATER:
                                case WATER:
                                    x = event.getClickedBlock().getX();
                                    y = event.getClickedBlock().getY();
                                    z = event.getClickedBlock().getZ();
                                    type = Material.WATER.getId();
                                    dataBlock2 = new BrokenBlock(BBUsersTable.getInstance().getUserByName(event.getPlayer().getName()), world.getName(), x, y, z, type, (byte) 0);
                                    OwnershipManager.removeOwnerByLocation(event.getClickedBlock().getLocation());
                                    dataBlock2.send();
                            }
                            break;
                        default:

                            switch (event.getClickedBlock().getType()) {
                                case WOODEN_DOOR:
                                    //case IRON_DOOR:
                                    if (!ActionProvider.isDisabled(DoorOpen.class)) {
                                        DoorOpen doorDataBlock = new DoorOpen(event.getPlayer().getName(), block, block.getWorld().getName());
                                        doorDataBlock.send();
                                    }
                                    break;
                                case LEVER:
                                    if (!ActionProvider.isDisabled(LeverSwitch.class)) {
                                        LeverSwitch leverDataBlock = new LeverSwitch(event.getPlayer().getName(), block, block.getWorld().getName());
                                        leverDataBlock.send();
                                    }
                                    break;
                                case STONE_BUTTON:
                                    if (!ActionProvider.isDisabled(ButtonPress.class)) {
                                        ButtonPress buttonDataBlock = new ButtonPress(event.getPlayer().getName(), block, block.getWorld().getName());
                                        buttonDataBlock.send();
                                    }
                                    break;
                                case CHEST:
                                    if (!ActionProvider.isDisabled(ChestOpen.class)) {
                                        BBAction chestDataBlock = new ChestOpen(event.getPlayer().getName(), block, block.getWorld().getName());
                                        chestDataBlock.send();
                                    }
                                    break;
                            }
                            break;
                    }
                }
            }
        } catch (Throwable e) {
            BBLogging.severe("onPlayerInteract(" + event.toString() + ")", e);
        }
    }
    
    private double distance(Location from, Location to) {
        return Math.sqrt(Math.pow(from.getX() - to.getX(), 2) + Math.pow(from.getY() - to.getY(), 2) + Math.pow(from.getZ() - to.getZ(), 2));
    }
}

/**
 * BigBrother's settings handler
 * Copyright (C) 2011 BigBrother Contributors
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.taylorkelly.bigbrother;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import me.taylorkelly.bigbrother.datablock.explosions.TNTLogger;
import me.taylorkelly.bigbrother.datasource.BBDB;
import me.taylorkelly.bigbrother.tablemgrs.ActionTable;

import org.bukkit.Material;
import org.bukkit.Server;

import com.sk89q.worldedit.blocks.ItemType;

public class BBSettings {
    
    public static List<String> watchedActions = new ArrayList<String>();
    
    public static boolean logPlayerIPs = true;
    public static boolean libraryAutoDownload;
    public static boolean debugMode;
    public static boolean restoreFire = false;
    public static boolean autoWatch = true;
    public static boolean flatLog = false;
    public static int defaultSearchRadius = 2; // 2 blocks
    public static int sendDelay = 4; // 4s
    public static int stickItem = 280; // A stick
    public static int logItem = Material.LOG.getId(); // A stick
    public static long cleanseAge = 604800; // 7d
    public static long deletesPerCleansing = 20000L;
    private static ArrayList<String> watchList;
    private static ArrayList<String> seenList;
    private static ArrayList<Integer> blockExclusionList;
    private static ArrayList<Integer> gnomes;
    public static List<String> worldExclusionList = new ArrayList<String>();
    public static int rollbacksPerTick;
    //private static BigBrother plugin;
    public static File dataFolder;
    public static boolean storeOwners = true;
    
    public static List<String> censoredCommands;
    
    public static void initialize(BigBrother plg, File dataFolder) {
        BBSettings.dataFolder = dataFolder;
        //BBSettings.plugin=plg;
        watchList = new ArrayList<String>();
        seenList = new ArrayList<String>();
        blockExclusionList = new ArrayList<Integer>();
        
        gnomes = new ArrayList<Integer>();
        gnomes.add(6); // Sapling
        gnomes.add(37); // Yellow Flower
        gnomes.add(38); // Red Flower
        gnomes.add(39); // Brown Mushroom
        gnomes.add(40); // Red Mushroom
        gnomes.add(55); // Redstone
        gnomes.add(59); // Crops
        gnomes.add(64); // Wood Door
        gnomes.add(66); // Tracks
        gnomes.add(69); // Lever
        gnomes.add(70); // Stone pressure plate
        gnomes.add(71); // Iron Door
        gnomes.add(72); // Wood pressure ePlate
        gnomes.add(78); // Snow
        gnomes.add(81); // Cactus
        gnomes.add(83); // Reeds
        gnomes.add(Material.LONG_GRASS.getId());
        gnomes.add(Material.DIODE_BLOCK_ON.getId());
        gnomes.add(Material.DIODE_BLOCK_OFF.getId());
        //gnomes.add(Material.FENCE.getId());
        gnomes.add(Material.DEAD_BUSH.getId());
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        final File exampleYml = new File(dataFolder, "BigBrother.example.yml");
        if (!exampleYml.exists()) {
            saveDefaultConfig(exampleYml);
        }
        final File yml = new File(dataFolder, "BigBrother.yml");
        BBLogging.debug("Path to BigBrother.yml: " + yml.getPath());
        loadLists(dataFolder);
        loadYaml(yml);
        BBLogging.debug("Loaded Settings");
        
    }
    
    public static void loadPostponed() {
        final File yamlfile = new File(dataFolder, "BigBrother.yml");
        final BetterConfig yml = new BetterConfig(yamlfile);
        
        // If the file's not there, don't load it
        if (yamlfile.exists())
            yml.load();
        
        ActionProvider.loadDisabled(yml);
        
        ActionTable.performPostponedUpdates();
        
        yml.save();
    }
    
    private static void loadYaml(File yamlfile) {
        final BetterConfig yml = new BetterConfig(yamlfile);
        
        // If the file's not there, don't load it
        if (yamlfile.exists())
            yml.load();
        
        logPlayerIPs = yml.getBoolean("general.log-ips", true);
        
        // Import old settings into new config defaults and remove the old versions.
        if (yml.getProperty("database.mysql.username") != null) {
            BBDB.username = yml.getString("database.mysql.username", BBDB.username);
            yml.removeProperty("database.mysql.username");
            BBDB.password = yml.getString("database.mysql.password", BBDB.password);
            yml.removeProperty("database.mysql.password");
            BBDB.hostname = yml.getString("database.mysql.hostname", BBDB.hostname);
            yml.removeProperty("database.mysql.hostname");
            BBDB.schema = yml.getString("database.mysql.database", BBDB.schema);
            yml.removeProperty("database.mysql.database");
            BBDB.port = yml.getInt("database.mysql.port", BBDB.port);
            yml.removeProperty("database.mysql.port");
            BBDB.prefix = yml.getString("database.mysql.prefix", BBDB.prefix);
            yml.removeProperty("database.mysql.prefix");
        }
        BBDB.initSettings(yml);
        
        List<Object> excluded = yml.getList("general.excluded-blocks");
        // Dodge NPE reported by Mineral (and set a default)
        if (excluded == null) {
            yml.setProperty("general.excluded-blocks", blockExclusionList);
        } else {
            for (Object o : excluded) {
                int id = 0;
                if (o instanceof Integer)
                    id = (int) (Integer) o;
                else if (o instanceof String) {
                    id = ItemType.lookup((String) o).getID();
                }
                blockExclusionList.add(id);
            }
        }
        
        censoredCommands = new ArrayList<String>();
        censoredCommands.add("/login"); // xAuth
        censoredCommands.add("/l"); // xAuth
        censoredCommands.add("/register"); // xAuth
        censoredCommands.add("/changepw"); // xAuth
        censoredCommands.add("/changepass"); // xAuth
        censoredCommands.add("/cpw"); // xAuth
        censoredCommands.add("/changepassword"); // xAuth
        censoredCommands.add("/xauth"); // xAuth
        censoredCommands.add("/login"); // ?
        censoredCommands = yml.getStringList("general.censored-commands", censoredCommands);
        
        List<String> excludedWorlds = yml.getStringList("general.excluded-worlds", new ArrayList<String>());
        if (excludedWorlds == null) {
            yml.setProperty("general.excluded-worlds", new ArrayList<String>());
        } else {
            worldExclusionList.addAll(excludedWorlds);
        }
        
        storeOwners = yml.getBoolean("general.store-owners", storeOwners);
        stickItem = yml.getInt("general.stick-item", 280);// "The item used for /bb stick");
        restoreFire = yml.getBoolean("general.restore-fire", false);// "Restore fire when rolling back");
        autoWatch = yml.getBoolean("general.auto-watch", true);// "Automatically start watching players");
        defaultSearchRadius = yml.getInt("general.default-search-radius", 5);// "Default search radius for bbhere and bbfind");
        flatLog = yml.getBoolean("general.personal-log-files", false);// "If true, will also log actions to .logs (one for each player)");
        rollbacksPerTick = yml.getInt("general.rollbacks-per-tick", 2000);// "If true, will also log actions to .logs (one for each player)");
        debugMode = yml.getBoolean("general.debug-mode", false);// "If true, will also log actions to .logs (one for each player)");
        libraryAutoDownload = yml.getBoolean("general.library-autodownload", true);// "If true, will also log actions to .logs (one for each player)");
        TNTLogger.THRESHOLD = 10.0;//yml.getDouble("general.tnt-threshold", 10.0);// "If true, will also log actions to .logs (one for each player)");
        yml.save();
    }
    
    /**
     * @todo Move to SQL tables.
     * @param dataFolder
     */
    private static void loadLists(File dataFolder) {
        File file = new File(dataFolder, "WatchedPlayers.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            final Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                final String player = sc.nextLine();
                if (player.equals("")) {
                    continue;
                }
                if (player.contains(" ")) {
                    continue;
                }
                watchList.add(player);
            }
        } catch (final FileNotFoundException e) {
            BBLogging.severe("Cannot read file " + file.getName());
        } catch (final IOException e) {
            BBLogging.severe("IO Exception with file " + file.getName());
        }
        
        file = new File(dataFolder, "SeenPlayers.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            final Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                final String player = sc.nextLine();
                if (player.equals("")) {
                    continue;
                }
                if (player.contains(" ")) {
                    continue;
                }
                seenList.add(player);
            }
        } catch (final FileNotFoundException e) {
            BBLogging.severe("Cannot read file " + file.getName());
        } catch (final IOException e) {
            BBLogging.severe("IO Exception with file " + file.getName());
        }
        
    }
    
    public static Watcher getWatcher(Server server, File dataFolder) {
        return new Watcher(server);
    }
    
    public enum DBMS {
        NULL, // Not set up
        MYSQL,
        POSTGRES,
    }
    
    /**
     * Replace placeholder with the table prefix.
     * 
     * @param sql
     * @param placeholder
     * @return
     */
    public static String replaceWithPrefix(String sql, String placeholder) {
        return sql.replace(placeholder, BBDB.prefix);
    }
    
    /**
     * Check if a blocktype is being ignored.
     * 
     * @param type
     * @return
     */
    public static boolean isBlockIgnored(int type) {
        return blockExclusionList.contains(type);
    }
    
    /**
     * Save example to file.
     * 
     * @param f
     *            dataFolder.
     */
    private static void saveDefaultConfig(File f) {
        try {
            InputStream is = BigBrother.class.getResourceAsStream("/BigBrother.example.yml");
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            FileWriter fstream = new FileWriter(f);
            BufferedWriter out = new BufferedWriter(fstream);
            String line;
            while ((line = in.readLine()) != null) {
                out.write(line + "\n");
            }
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            BBLogging.severe("Error while saving default config: ", e);
        }
    }
}

package me.taylorkelly.bigbrother;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.Bukkit;
// import org.anjocaido.groupmanager.GroupManager; // Inactive and Essentials screwed up their maven repo

public class BBPermissions {

    private enum PermissionHandler 
    {
        PERMISSIONS,
        GROUP_MANAGER, 
        NONE
    }
    private static PermissionHandler handler;
    private static Plugin permissionPlugin;

    public static void initialize(Server server) {
        //Plugin groupManager = server.getPluginManager().getPlugin("GroupManager");
        Plugin permissions = server.getPluginManager().getPlugin("Permissions");
        
        /* Use FakePermissions 
         if (groupManager != null) {
            permissionPlugin = groupManager;
            handler = PermissionHandler.GROUP_MANAGER;
            String version = groupManager.getDescription().getVersion();
            BBLogging.info("Permissions enabled using: GroupManager v" + version);
        } else*/ 
        if (permissions != null) {
            permissionPlugin = permissions;
            handler = PermissionHandler.PERMISSIONS;
            String version = permissions.getDescription().getVersion();
            
            if (!Bukkit.getServer().getPluginManager().isPluginEnabled(permissionPlugin)) {
                BBLogging.info("Permissions plugin was found but disabled. Enabling 'Permissions' (v"+version+") now.");
                Bukkit.getServer().getPluginManager().enablePlugin(permissionPlugin);
            }
            
            BBLogging.info("Permissions enabled using: Permissions v" + version);
        } else {
            handler = PermissionHandler.NONE;
            BBLogging.severe("A permission plugin isn't loaded, only OPs can use commands");
        }
    }

    private static boolean permission(Player player, String string) {
        switch (handler) {
            case PERMISSIONS:
                return ((Permissions)permissionPlugin).getHandler().permission(player, string);
            //case GROUP_MANAGER:
            //    return ((GroupManager)permissionPlugin).getWorldsHolder().getWorldPermissions(player).has(player, string);
            case NONE:
                return player.isOp();
            default:
                return player.isOp();
        }
    }

    public static boolean info(Player player) {
        return permission(player, "bb.admin.info");
    }

    public static boolean rollback(Player player) {
        return permission(player, "bb.admin.rollback");
    }

    public static boolean watch(Player player) {
        return permission(player, "bb.admin.watch");
    }

    public static boolean cleanse(Player player) {
        return permission(player, "bb.admin.cleanse");
    }
}

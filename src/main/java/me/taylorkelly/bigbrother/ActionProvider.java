/**
 * A listener-like Action factory
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.taylorkelly.bigbrother.datablock.Action;
import me.taylorkelly.bigbrother.datablock.ActionCategory;
import me.taylorkelly.bigbrother.tablemgrs.ActionTable;

import org.bukkit.plugin.Plugin;

/**
 * @author Rob
 * 
 */
public abstract class ActionProvider {
    public static class ActionData {
        public Plugin          plugin = null;
        public String          pluginName;
        public Action          action = null;
        public String          actionName;
        public ActionCategory  category;
        private ActionProvider provider;
        
        public ActionData(Plugin plugin, ActionProvider ap, Action action) {
            this(plugin.getDescription().getName(), action.getCategory(), action.getName());
            this.plugin = plugin;
            this.provider = ap;
            this.action = action;
        }
        
        public ActionData(String plugin, ActionCategory category, String action) {
            this.pluginName = plugin;
            this.category = category;
            this.actionName = action;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof ActionData) {
                return ((ActionData) o).pluginName.equalsIgnoreCase(pluginName)
                        && ((ActionData) o).actionName.equalsIgnoreCase(actionName)
                        && ((ActionData) o).category == category;
            }
            return false;
        }
    }
    
    public static Map<Integer, ActionData> Actions = new HashMap<Integer, ActionData>();
    private Plugin                         plugin;
    
    /**
     * Override this to provide custom Actions.
     * @param actionName
     * @param player
     * @param world
     * @param x
     * @param y
     * @param z
     * @param type
     * @param data
     * @return
     */
    public abstract Action getAction(String actionName, BBPlayerInfo player, String world, int x, int y, int z, int type, String data);
    
    public ActionProvider(Plugin p) {
        this.plugin = p;
    }
    
    public final void disable() {
        Map<Integer, ActionData> copy = new HashMap<Integer, ActionData>();
        copy.putAll(Actions);
        for (Entry<Integer, ActionData> e : copy.entrySet()) {
            if (e.getValue().plugin == plugin) {
                Actions.remove(e.getKey());
            }
        }
    }
    
    /**
     * Called to register custom Actions with BigBrother.
     * @param plugin
     * @param provider
     * @param action
     */
    protected final void registerAction(Plugin plugin, ActionProvider provider, Action action) {
        ActionData dat = new ActionData(plugin, provider, action);
        if (!Actions.containsValue(dat)) {
            int id = ActionTable.add(plugin.getDescription().getName(), action.getName(), action.getCategory().ordinal(),action.getDescription());
            BBLogging.info("Action #"+id+" - "+action.getName());
            Actions.put(id, dat);
        }
    }
    
    /**
     * Get the ID # of an action.
     * @param action
     * @return
     */
    public static int getActionID(Action action) {
        for (Entry<Integer, ActionData> e : Actions.entrySet()) {
            if (e.getValue().action.getName().equalsIgnoreCase(action.getName())) {
                return e.getKey();
            }
        }
        BBLogging.severe("Unknown Action: " + action.getName());
        return -1;
    }
    
    /**
     * Find the LoggingProvider for the ActionID provided, and get the Action.
     * 
     * @param actionID
     * @param player
     * @param world
     * @param x
     * @param y
     * @param z
     * @param type
     * @param data
     * @return
     */
    public static Action findAndProvide(int actionID, BBPlayerInfo player, String world, int x, int y, int z, int type, String data) {
        if (!Actions.containsKey(actionID))
            return null;
        ActionProvider provider = Actions.get(actionID).provider;
        if (provider == null)
            return null;
        return provider.getAction(Actions.get(actionID).actionName, player, world, x, y, z, type, data);
    }
    
    /**
     * Find the ID of the provided action name
     * @param actionName
     * @return
     */
    public static int findActionID(String actionName) {
        for (Entry<Integer, ActionData> e : Actions.entrySet()) {
            if (e.getValue().action.getName().equalsIgnoreCase(actionName) || e.getValue().action.getName().contains(actionName)) {
                return e.getKey();
            }
        }
        return -1;
    }
    
    /**
     * Only for use by BigBrother!  Use registerAction instead!
     * @param plugin
     * @param bbActionProvider
     * @param deltaChest
     * @param i
     */
    protected void registerActionForceID(BigBrother plugin, BBActionProvider provider, Action action, int id) {
        ActionData dat = new ActionData(plugin, provider, action);
        if (!Actions.containsValue(dat) && !Actions.containsKey(id)) {
            ActionTable.addForcedID(plugin.getDescription().getName(), action.getName(), action.getCategory().ordinal(), id,action.getDescription());
            Actions.put(id, dat);
        }
    }
}

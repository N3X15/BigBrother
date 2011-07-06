/**
 * BigBrother's own ActionProvider
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

import me.taylorkelly.bigbrother.datablock.*;

/**
 * This should only be initialized after BigBrother is loaded (e.g. from a PluginListener)
 * @author Rob
 *
 */
public class BBActionProvider extends ActionProvider {
    public BBActionProvider(BigBrother plugin) {
        super(plugin);
        registerAction(plugin, this, new BlockBurn());
        registerAction(plugin, this, new BrokenBlock());
        registerAction(plugin, this, new ButtonPress());
        registerAction(plugin, this, new Chat());
        registerAction(plugin, this, new ChestOpen());
        registerAction(plugin, this, new Command());
        registerAction(plugin, this, new CreateSignText());
        registerAction(plugin, this, new DeltaChest());
        registerAction(plugin, this, new DestroySignText());
        registerAction(plugin, this, new Disconnect());
        registerAction(plugin, this, new DoorOpen());
        registerAction(plugin, this, new DropItem());
        registerAction(plugin, this, new FlintAndSteel());
        registerAction(plugin, this, new Flow());
        registerAction(plugin, this, new LeafDecay());
        registerAction(plugin, this, new LeverSwitch());
        registerAction(plugin, this, new Login());
        registerAction(plugin, this, new PickupItem());
        registerAction(plugin, this, new PlacedBlock());
        registerAction(plugin, this, new SignDestroyed());
        registerAction(plugin, this, new Teleport());
    }
    
    /* (non-Javadoc)
     * @see me.taylorkelly.bigbrother.ActionProvider#getAction(java.lang.String, me.taylorkelly.bigbrother.BBPlayerInfo, java.lang.String, int, int, int, int, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Action getAction(String actionName, BBPlayerInfo player, String world, int x, int y, int z, int type, String data) {
        //And here, we use the magic of reflection.
        Class<? extends Action> c=null;
        try {
            c=(Class<? extends Action>) Class.forName("me.taylorkelly.bigbrother.datablock."+actionName);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            return c.getConstructor(BBPlayerInfo.class,String.class,int.class,int.class,int.class,int.class,String.class).newInstance(player,world,  x,  y,  z,  type,  data);
        } catch (Exception e) {
            BBLogging.severe("Error loading me.taylorkelly.bigbrother.datablock."+actionName+": ",e);
        }
        return null;
    }
    
}
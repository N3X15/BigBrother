package me.taylorkelly.bigbrother;

import java.sql.Statement;

import me.taylorkelly.bigbrother.datasource.BBDB;
import me.taylorkelly.bigbrother.tablemgrs.BBDataTable;
import me.taylorkelly.util.Time;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

// Rule 1 - If you end up using ResultSet, you're doing it wrong.
public class Cleanser {
    
    private static CleanupThread cleanupThread = null;
    
    public static boolean needsCleaning() {
        return BBSettings.cleanseAge != -1;
    }
    
    /**
     * Start the cleaner thread and pray to God it finishes.
     * @param player Person to blame for the lag. Or null.
     */
    public static void clean(Player player) {
        if (cleanupThread != null && cleanupThread.done) {
            cleanupThread = null;
        }
        if (cleanupThread == null || !cleanupThread.isAlive()) {
            cleanupThread = new CleanupThread(player);
            cleanupThread.start();
        } else {
            if (player != null) {
                player.chat(BigBrother.premessage + " Cleaner already busy.  Try again later.");
            }
        }
    }
    
    public static void initialize(BigBrother bigbrother) {
        bigbrother.getServer().getScheduler().scheduleAsyncRepeatingTask(bigbrother, new CleanupTask(), 50, 26000);
    }
    
    private static class CleanupTask implements Runnable {
        @Override
        public void run() {
            clean(null);
        }
    }
    
    /**
     * Cleanser thread, to avoid blocking the main app when cleaning crap.
     * @author N3X15 <nexis@7chan.org>
     *
     */
    private static class CleanupThread extends Thread {
        
        private long cleanedSoFarAge = 0;
        private boolean done = false;
        private final Player player;
        
        /**
         * @param p The player. Can be null.
         */
        public CleanupThread(Player p) {
            // Constructor.
            player = p;
            this.setName("Cleanser");
        }
        
        @Override
        public void run() {
            //BBLogging.info("Starting Cleanser thread...");
            if (BBSettings.cleanseAge != -1) {
                cleanByAge();
            }
            //BBLogging.info("Ending Cleanser thread...");
            done = true; // Wait for cleanup
        }
        
        private void cleanByAge() {
            long start = System.currentTimeMillis() / 1000;
            
            cleanedSoFarAge = BBDB.executeUpdate(BBDataTable.getInstance().getCleanseAged(Long.valueOf(Time.ago(BBSettings.cleanseAge)),
                    BBSettings.deletesPerCleansing));
            if(cleanedSoFarAge==Statement.EXECUTE_FAILED)
                return;
            String timespent = Time.formatDuration(System.currentTimeMillis() / 1000 - start);
            
            String words = String.format("Cleaned out %d expired records in %s.", cleanedSoFarAge, timespent);
            if (player == null) {
                BBLogging.info(words);
            } else {
                synchronized (player) {
                    player.sendMessage(ChatColor.BLUE + words);
                }
            }
        }
    }
}

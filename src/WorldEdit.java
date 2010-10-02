// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.io.*;
import org.mozilla.javascript.*;
import com.sk89q.worldedit.*;

/**
 *
 * @author sk89q
 */
public class WorldEdit extends Plugin {
    private static Logger logger = Logger.getLogger("Minecraft");
    private HashMap<String,WorldEditSession> sessions = new HashMap<String,WorldEditSession>();
    private HashMap<String,String> commands = new HashMap<String,String>();

    public WorldEdit() {
        super();

        commands.put("/editpos1", "Set editing position #1");
        commands.put("/editpos2", "Set editing position #2");
        commands.put("/editsize", "Get size of selected region");
        commands.put("/editset", "<Type> - Set all  blocks inside region");
        commands.put("/editreplace", "<ID> - Replace all existing blocks inside region");
        commands.put("/editoverlay", "<ID> - Overlay the area one layer");
        commands.put("/removeabove", "<Size> - Remove blocks above head");
        commands.put("/editfill", "<ID> <Radius> <Depth> - Fill a hole");
        commands.put("/editscript", "<Filename> [Args...] - Run an editscript");
    }

    /**
     * Enables the plugin.
     */
    public void enable() {
        etc controller = etc.getInstance();

        for (Map.Entry<String,String> entry : commands.entrySet()) {
            controller.addCommand(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Disables the plugin.
     */
    public void disable() {
        etc controller = etc.getInstance();
        
        for (String key : commands.keySet()) {
            controller.removeCommand(key);
        }

        sessions.clear();
    }

    /**
     * Gets the WorldEdit session for a player.
     *
     * @param player
     * @return
     */
    private WorldEditSession getSession(Player player) {
        if (sessions.containsKey(player.getName())) {
            return sessions.get(player.getName());
        } else {
            WorldEditSession session = new WorldEditSession();
            sessions.put(player.getName(), session);
            return session;
        }
    }

    /**
     * Get an item ID from an item name or an item ID number.
     *
     * @param id
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    private int getItem(String id) throws UnknownItemException,
                                          DisallowedItemException {
        int foundID;

        try {
            foundID = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            try {
                foundID = etc.getInstance().getDataSource().getItem(id);
            } catch (NumberFormatException e2) {
                throw new UnknownItemException();
            }
        }

        if ((foundID >= 0 && foundID <= 5) ||
            (foundID >= 7 && foundID <= 20) ||
            foundID == 35 ||
            (foundID >= 41 && foundID <= 45) ||
            (foundID >= 47 && foundID <= 49) ||
            (foundID >= 52 && foundID <= 54) ||
            (foundID >= 56 && foundID <= 58) ||
            (foundID >= 60 && foundID <= 62) ||
            foundID == 67 ||
            foundID == 73 ||
            (foundID >= 78 && foundID <= 82) ||
            foundID == 85) {
            return foundID;
        } else {
            throw new DisallowedItemException();
        }
    }

    /**
     * Sets the block at position x, y, z with a block type.
     *
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @return
     */
    private boolean setBlock(int x, int y, int z, int blockType) {
        return etc.getMCServer().e.d(x, y, z, blockType);
    }

    /**
     * Gets the block type at a position x, y, z.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    private int getBlock(int x, int y, int z) {
        return etc.getMCServer().e.a(x, y, z);
    }

    /**
     *
     * @override
     * @param player
     */
    public void onDisconnect(Player player) {
        sessions.remove(player.getName());
    }

    /**
     * 
     * @override
     * @param player
     * @param split
     * @return
     */
    public boolean onCommand(Player player, String[] split) {
        try {
            if (commands.containsKey(split[0])) {
                if (etc.getInstance().canUseCommand(player.getName(), split[0])) {
                    return handleEditCommand(player, split);
                }
            }

            return false;
        } catch (NumberFormatException e) {
            player.sendMessage(Colors.Rose + "Number expected; string given.");
            return true;
        } catch (IncompleteRegionException e2) {
            player.sendMessage(Colors.Rose + "The edit region has not been fully defined.");
            return true;
        } catch (UnknownItemException e3) {
            player.sendMessage(Colors.Rose + "Unknown item.");
            return true;
        } catch (DisallowedItemException e4) {
            player.sendMessage(Colors.Rose + "Disallowed item.");
            return true;
        } catch (InsufficientArgumentsException e5) {
            player.sendMessage(Colors.Rose + e5.getMessage());
            return true;
        }
    }

    /**
     * Checks to make sure that there are enough arguments.
     * 
     * @param args
     * @param min
     * @throws InsufficientArgumentsException
     */
    private void checkArgs(String[] args, int min) throws InsufficientArgumentsException {
        if (args.length <= min) {
            throw new InsufficientArgumentsException(String.format("Min. %d arguments required", min));
        }
    }

    private boolean handleEditCommand(Player player, String[] split)
            throws UnknownItemException, IncompleteRegionException,
                   InsufficientArgumentsException, DisallowedItemException
    {
        WorldEditSession session = getSession(player);

        // Set edit position #1
        if (split[0].equalsIgnoreCase("/editpos1")) {
            session.setPos1((int)Math.floor(player.getX()),
                            (int)Math.floor(player.getY()),
                            (int)Math.floor(player.getZ()));
            player.sendMessage(Colors.LightPurple + "First edit position set.");
            return true;

        // Set edit position #2
        } else if (split[0].equalsIgnoreCase("/editpos2")) {
            session.setPos2((int)Math.floor(player.getX()),
                            (int)Math.floor(player.getY()),
                            (int)Math.floor(player.getZ()));
            player.sendMessage(Colors.LightPurple + "Second edit position set.");
            return true;

        // Fill a hole
        } else if (split[0].equalsIgnoreCase("/editfill")) {
            checkArgs(split, 1);
            int blockType = getItem(split[1]);
            int radius = split.length > 2 ? Integer.parseInt(split[2]) : 50;
            int depth = split.length > 3 ? Integer.parseInt(split[3]) : 1;

            int cx = (int)Math.floor(player.getX());
            int cy = (int)Math.floor(player.getY());
            int cz = (int)Math.floor(player.getZ());
            int minY = Math.max(-128, cy - depth);

            int affected = fill(cx, cz, cx, cy, cz, blockType, radius, minY);

            logger.log(Level.INFO, player.getName() + " used /editfill");
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been created.");

            return true;

        // Remove blocks above current position
        } else if (split[0].equalsIgnoreCase("/removeabove")) {
            int size = split.length > 1 ? Integer.parseInt(split[1]) - 1 : 0;

            int affected = 0;
            int cx = (int)Math.floor(player.getX());
            int cy = (int)Math.floor(player.getY());
            int cz = (int)Math.floor(player.getZ());

            for (int x = cx - size; x <= cx + size; x++) {
                for (int z = cz - size; z <= cz + size; z++) {
                    for (int y = cy; y <= 127; y++) {
                        if (getBlock(x, y, z) != 0) {
                            setBlock(x, y, z, 0);
                            affected++;
                        }
                    }
                }
            }

            logger.log(Level.INFO, player.getName() + " used /removeabove");
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been removed.");

            return true;

        // Run an editscript
        } else if (split[0].equalsIgnoreCase("/editscript")) {
            checkArgs(split, 1);
            String filename = split[1].replace("\0", "") + ".js";
            File dir = new File("editscripts");
            File f = new File("editscripts", filename);

            try {
                String filePath = f.getCanonicalPath();
                String dirPath = dir.getCanonicalPath();

                if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                    player.sendMessage(Colors.Rose + "Editscript file does not exist.");
                } else {
                    // Read file
                    StringBuffer buffer = new StringBuffer();
                    FileInputStream stream = new FileInputStream(f);
                    BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                    int c;
                    while ((c = in.read()) > -1) {
                        buffer.append((char)c);
                    }
                    in.close();
                    String code = buffer.toString();

                    // Evaluate
                    Context cx = Context.enter();
                    try {
                        ScriptableObject scope = cx.initStandardObjects();

                        // Add args
                        String[] args = new String[split.length - 2];
                        System.arraycopy(split, 2, args, 0, split.length - 2);
                        ScriptableObject.putProperty(scope, "args",
                            Context.javaToJS(args, scope));

                        // Add context
                        EditScriptPlayer scriptPlayer = new EditScriptPlayer(player);
                        EditScriptContext context = new EditScriptContext(
                            scriptPlayer);
                        ScriptableObject.putProperty(scope, "context",
                            Context.javaToJS(context, scope));
                        ScriptableObject.putProperty(scope, "player",
                            Context.javaToJS(scriptPlayer, scope));

                        // Add Minecraft context
                        EditScriptMinecraftContext minecraft = new EditScriptMinecraftContext();
                        ScriptableObject.putProperty(scope, "minecraft",
                            Context.javaToJS(minecraft, scope));

                        cx.evaluateString(scope, code, filename, 1, null);
                        player.sendMessage(Colors.LightPurple + filename + " executed successfully.");
                    } catch (RhinoException re) {
                        player.sendMessage(Colors.Rose + "JS error: " + re.getMessage());
                        re.printStackTrace();
                    } finally {
                        Context.exit();
                    }

                    return true;
                }
            } catch (IOException e) {
                player.sendMessage(Colors.Rose + "Editscript could not read or it does not exist.");
            }
            return true;
        }

        int lowerX = session.getLowerX();
        int upperX = session.getUpperX();
        int lowerY = session.getLowerY();
        int upperY = session.getUpperY();
        int lowerZ = session.getLowerZ();
        int upperZ = session.getUpperZ();
        
        // Get size of area
        if (split[0].equalsIgnoreCase("/editsize")) {
            player.sendMessage(Colors.LightPurple + "# of blocks: " + getSession(player).getSize());
            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/editset")) {
            checkArgs(split, 1);
            int blockType = getItem(split[1]);
            int affected = 0;

            for (int x = lowerX; x <= upperX; x++) {
                for (int y = lowerY; y <= upperY; y++) {
                    for (int z = lowerZ; z <= upperZ; z++) {
                        setBlock(x, y, z, blockType);
                        affected++;
                    }
                }
            }

            logger.log(Level.INFO, player.getName() + " used /editset");
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been set.");

            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/editreplace")) {
            checkArgs(split, 1);
            int blockType = getItem(split[1]);

            int affected = 0;

            for (int x = lowerX; x <= upperX; x++) {
                for (int y = lowerY; y <= upperY; y++) {
                    for (int z = lowerZ; z <= upperZ; z++) {
                        if (getBlock(x, y, z) != 0) {
                            setBlock(x, y, z, blockType);
                            affected++;
                        }
                    }
                }
            }

            logger.log(Level.INFO, player.getName() + " used /editreplace");
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been replaced.");

            return true;

        // Lay blocks over an area
        } else if (split[0].equalsIgnoreCase("/editoverlay")) {
            checkArgs(split, 1);
            int blockType = getItem(split[1]);

            // We don't want to pass beyond boundaries
            upperY = Math.min(127, upperY + 1);
            lowerY = Math.max(-128, lowerY - 1);

            int affected = 0;

            for (int x = lowerX; x <= upperX; x++) {
                for (int z = lowerZ; z <= upperZ; z++) {
                    for (int y = upperY; y >= lowerY; y--) {
                        if (y + 1 <= 127 && getBlock(x, y, z) != 0 && getBlock(x, y + 1, z) == 0) {
                            setBlock(x, y + 1, z, blockType);
                            affected++;
                            break;
                        }
                    }
                }
            }

            logger.log(Level.INFO, player.getName() + " used /editoverlay");
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been overlayed.");

            return true;
        }

        return false;
    }

    private int fill(int x, int z, int cx, int cy, int cz, int blockType, int radius, int minY) {
        double dist = Math.sqrt(Math.pow(cx - x, 2) + Math.pow(cz - z, 2));
        int affected = 0;
        
        if (dist > radius) {
            return 0;
        }

        if (getBlock(x, cy, z) == 0) {
            affected = fillY(x, cy, z, blockType, minY);
        } else {
            return 0;
        }
        
        affected += fill(x + 1, z, cx, cy, cz, blockType, radius, minY);
        affected += fill(x - 1, z, cx, cy, cz, blockType, radius, minY);
        affected += fill(x, z + 1, cx, cy, cz, blockType, radius, minY);
        affected += fill(x, z - 1, cx, cy, cz, blockType, radius, minY);

        return affected;
    }

    private int fillY(int x, int cy, int z, int blockType, int minY) {
        int affected = 0;
        
        for (int y = cy; y > minY; y--) {
            if (getBlock(x, y, z) == 0) {
                setBlock(x, y, z, blockType);
                affected++;
            } else {
                break;
            }
        }

        return affected;
    }
}
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

package com.sk89q.worldedit.bukkit;

import org.bukkit.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bags.BlockBag;

public class BukkitPlayer extends WorldEditPlayer {
    private Player player;
    
    public BukkitPlayer(Player player) {
        this.player = player;
    }

    @Override
    public Vector getBlockTrace(int range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vector getSolidBlockTrace(int range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getItemInHand() {
        ItemStack itemStack = player.getSelectedItem();
        return itemStack != null ? itemStack.getTypeID() : 0;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public Vector getPosition() {
        Location loc = player.getLocation();
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public double getPitch() {
        return player.getLocation().getPitch();
    }

    @Override
    public double getYaw() {
        return player.getLocation().getYaw();
    }

    @Override
    public void giveItem(int type, int amt) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean passThroughForwardWall(int range) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void printRaw(String msg) {
        player.sendMessage(msg);
    }

    @Override
    public void print(String msg) {
        player.sendMessage(msg);
    }

    @Override
    public void printError(String msg) {
        player.sendMessage(msg);
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        // TODO Auto-generated method stub
    }

    @Override
    public String[] getGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasPermission(String perm) {
        // TODO Auto-generated method stub
        return true;
    }

}
package cc.i9mc.onebedwars.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by JinVan on 2021-01-10.
 */
public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        ((Player) commandSender).teleport(new Location(Bukkit.getWorld("world"), 0, 100, 0));

        return false;
    }
}

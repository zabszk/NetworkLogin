package net.zabszk.networklogin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Event implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!Main.getInstance().IsAuthenticated(e.getPlayer(), true)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent e) {
        if (!Main.getInstance().config.getBoolean("CheckForRegistrationOnJoin")) return;
        System.out.println("[Zabszk NetworkLogin] Preauthenticating player " + e.getPlayer().getName() + "...");

        int response = Functions.Authenticate(e.getPlayer().getName());
        System.out.println("[Zabszk NetworkLogin] Result of preauthentication of player " + e.getPlayer().getName() + " is " + response);

        if (response == 0 || response == 2) e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Functions.getMessage("AccountNotFoundMessage"));
        else if (response == 3) e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Functions.getMessage("AccountInactiveMessage"));
        else if (response == 4) e.disallow(PlayerLoginEvent.Result.KICK_BANNED, Functions.getMessage("AccountBannedMessage"));
        else if (Main.getInstance().config.getBoolean("KickWhenPreauthFailsToConnect")) e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Functions.getMessage("InternalErrorMessage"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage();

        if (command.length() > 1) {
            command = command.substring(1);
            if (command.contains(" ")) command = command.substring(0, command.indexOf(" "));
        }
        List<String> args = new ArrayList(Arrays.asList(e.getMessage().split(" ")));
        args.remove(0);

        if (command.equalsIgnoreCase("login") || command.equalsIgnoreCase("l") || command.equalsIgnoreCase("signin")) {
            e.setCancelled(true);
            if (args.size() == 2 && args.get(0).equalsIgnoreCase("-f")) {
                if (e.getPlayer().hasPermission("networklogin.forcelogin") && e.getPlayer().isOp() && Main.getInstance().IsAuthenticated(e.getPlayer(), true)) {
                    try {
                        Player tr = Bukkit.getPlayer(args.get(1));
                        if (tr == null || !tr.isOnline()) {
                            e.getPlayer().sendMessage(ChatColor.RED + "[Zabszk NetworkLogin] Player " + args.get(1) + " is offline.");
                            return;
                        }
                        System.out.println("[Zabszk NetworkLogin] Player " + tr.getName() + " has been forced authenticated by " + e.getPlayer().getName());
                        Main.getInstance().SetAuthenticated(tr, true);
                    } catch (Exception ex) {
                        e.getPlayer().sendMessage(ChatColor.RED + "[Zabszk NetworkLogin] Player " + args.get(1) + " is offline or error occurred.");
                    }
                } else
                    e.getPlayer().sendMessage(ChatColor.RED + "[Zabszk NetworkLogin] You don't have permissions to force authenticate.");
            } else if (args.size() > 0) {
                if (Main.getInstance().IsAuthenticated(e.getPlayer(), false))
                    e.getPlayer().sendMessage(Functions.getMessage("AlreadyLogged"));
                System.out.println("[Zabszk NetworkLogin] Player " + e.getPlayer().getName() + " is trying to authenticate...");
                int response = Functions.Authenticate(e.getPlayer().getName(), args.get(0));
                System.out.println("[Zabszk NetworkLogin] Authentication result of player " + e.getPlayer().getName() + " is " + response);

                if (response == 0) e.getPlayer().sendMessage(Functions.getMessage("AccountNotFoundMessage"));
                else if (response == 1) Main.getInstance().SetAuthenticated(e.getPlayer(), true);
                else if (response == 2) e.getPlayer().sendMessage(Functions.getMessage("InvalidPasswordMessage"));
                else if (response == 3) e.getPlayer().sendMessage(Functions.getMessage("AccountInactiveMessage"));
                else if (response == 4) e.getPlayer().sendMessage(Functions.getMessage("AccountBannedMessage"));
                else e.getPlayer().sendMessage(Functions.getMessage("InternalErrorMessage"));
            } else e.getPlayer().sendMessage(ChatColor.RED + "Syntax: /login password OR /login -f username");
        } else {
            if (!Main.getInstance().IsAuthenticated(e.getPlayer(), false)) {
                List<String> cmds = new ArrayList();
                cmds = Main.getInstance().config.getStringList("CommandsAllowedForNotAuthenticated");
                for (String cmd : cmds) {
                    if (cmd.equalsIgnoreCase(command)) return;
                }
                e.setCancelled(true);
                Main.getInstance().SendReminder(e.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Main.getInstance().SetAuthenticated(e.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        if (!Main.getInstance().IsAuthenticated(e.getPlayer(), true)) e.getPlayer().teleport(e.getFrom());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent e) {
        if (!Main.getInstance().IsAuthenticated(e.getPlayer(), true)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickup(PlayerPickupItemEvent e) {
        if (!Main.getInstance().IsAuthenticated(e.getPlayer(), true)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!Main.getInstance().IsAuthenticated(e.getPlayer(), true)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPleace(BlockPlaceEvent e) {
        if (!Main.getInstance().IsAuthenticated(e.getPlayer(), true)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (!Main.getInstance().IsAuthenticated(e.getPlayer(), true)) e.setCancelled(true);
    }
}

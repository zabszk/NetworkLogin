package net.zabszk.networklogin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin {
    private static Main instance;
    private List<String> Authenticated = new ArrayList();
    List<String> TimedList = new ArrayList();
    Event event;
    FileConfiguration config;

    public void onEnable()
    {
        System.out.println("[Zabszk NetworkLogin] Activating the plugin...");

        instance = this;

        config = getConfig();
        config.addDefault("AuthenticationURL", "https://someserver.example/authenticateMyBB.php");
        config.addDefault("ServerToken", "");
        config.addDefault("SignInMessage", "&4Please log in using: /login YourPassword");
        config.addDefault("AuthenticatedMessage", "&aYou are now logged in.");
        config.addDefault("LoggedOutMessage", "&eYou are now logged out.");
        config.addDefault("AlreadyLogged", "&aYou are already logged in.");
        config.addDefault("InvalidPasswordMessage", "&4Password rejected.");
        config.addDefault("AccountInactiveMessage", "&4Your account is inactive.");
        config.addDefault("AccountBannedMessage", "&4Your account has been banned.");
        config.addDefault("AccountNotFoundMessage", "&4Your account doesn't exists. Please register at htttps://someserver.example/register.php");
        config.addDefault("InternalErrorMessage", "&4Internal error occurred. Please contant administrator.");

        List<String> cmds = new ArrayList();
        config.addDefault("CommandsAllowedForNotAuthenticated", cmds);

        config.options().copyDefaults(true);
        saveConfig();

        event = new Event();
        getServer().getPluginManager().registerEvents(event, this);

        System.out.println("[Zabszk NetworkLogin] Plugin enabled.");
    }

    public void onDisable()
    {
        System.out.println("[Zabszk NetworkLogin] Deactivating the plugin...");
        Authenticated.clear();
        System.out.println("[Zabszk NetworkLogin] All players has been logged out.");
        System.out.println("[Zabszk NetworkLogin] Plugin disabled.");
    }

    public static Main getInstance() {
        return instance;
    }

    public boolean IsAuthenticated(Player target, boolean SendReminder)
    {
        if (Authenticated.contains(target.getUniqueId().toString())) return true;
        else
        {
            if (SendReminder) SendReminder(target);
            return false;
        }
    }

    public void SetAuthenticated(Player target, Boolean value) {
        if (value == IsAuthenticated(target, false)) return;
        if (value && target.isOnline()) {
            System.out.println("[Zabszk NetworkLogin] Player " + target.getName() + " has been authenticated.");
            Authenticated.add(target.getUniqueId().toString());
            target.sendMessage(Functions.getMessage("AuthenticatedMessage"));
        } else if (!value) {
            Authenticated.remove(target.getUniqueId().toString());
            if (target.isOnline()) target.sendMessage(Functions.getMessage("LoggedOutMessage"));
        }
    }

    public void SendReminder(Player target)
    {
        if (!Authenticated.contains(target.getUniqueId().toString())) target.sendMessage(Functions.getMessage("SignInMessage"));
    }
}

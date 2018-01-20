package net.zabszk.networklogin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;

public class Functions {
    public static Player[] getOnline() {
        try {
            Collection<Player> newPlayers = (Collection<Player>) Bukkit.getOnlinePlayers();

            Player[] online = new Player[newPlayers.size()];

            Object[] obj = newPlayers.toArray();

            int counter = 0;

            for (int i = 0; i < obj.length; i++) {
                if (obj[i] instanceof Player) {
                    String name = obj[i].toString().substring(obj[i].toString().indexOf("{"));
                    name = name.replace("{name=", "");
                    name = name.substring(0, name.length() - 1);

                    online[counter] = Bukkit.getPlayer(name);
                    counter = counter + 1;
                }
            }
            return online;
        } catch (Exception e) {
            System.out.println("Player online ERROR");
            System.out.println(e.toString());
            e.printStackTrace();

            return null;
        }
    }

    public static int Authenticate(String username) {
        String resp = MakeQuery(username, null);
        if (resp == null) return -1;
        if (resp.equalsIgnoreCase("Missing data") || resp.equalsIgnoreCase("Invalid token")) {
            System.out.print("[Zabszk NetworkLogin] Error received from authentication server: " + resp);
            return -1;
        }
        if (resp.equalsIgnoreCase("User not found")) return 0;
        if (resp.equalsIgnoreCase("Permitted to join")) return 1;
        if (resp.equalsIgnoreCase("Authenticated")) return 1; //Empty password or something
        if (resp.equalsIgnoreCase("Rejected")) return 2;
        if (resp.equalsIgnoreCase("Not activated")) return 3;
        if (resp.equalsIgnoreCase("Banned")) return 4;
        System.out.println("Zabszk NetworkLogin] Unknown response from authentication server: " + resp);
        return -3;
    }

    public static int Authenticate(String username, String password) {
        String resp = MakeQuery(username, password);
        if (resp == null) return -1;
        if (resp.equalsIgnoreCase("Missing data") || resp.equalsIgnoreCase("Invalid token")) {
            System.out.print("[Zabszk NetworkLogin] Error received from authentication server: " + resp);
            return -1;
        }
        if (resp.equalsIgnoreCase("Permitted to join")) return -2; //No password provided for some reason
        if (resp.equalsIgnoreCase("User not found")) return 0;
        if (resp.equalsIgnoreCase("Authenticated")) return 1;
        if (resp.equalsIgnoreCase("Rejected")) return 2;
        if (resp.equalsIgnoreCase("Not activated")) return 3;
        if (resp.equalsIgnoreCase("Banned")) return 4;
        System.out.println("Zabszk NetworkLogin] Unknown response from authentication server: " + resp);
        return -3;
    }

    private static String MakeQuery(String username, String password) {
        try {
            String httpsURL = Main.getInstance().config.getString("AuthenticationURL");

            String query = "username=" + URLEncoder.encode(username, "UTF-8");
            query += "&token=" + URLEncoder.encode(Main.getInstance().config.getString("ServerToken"), "UTF-8");
            if (password != null && password.length() > 0) {
                query += "&password=" + URLEncoder.encode(password, "UTF-8");
            }
            URL queryurl = new URL(httpsURL);
            HttpsURLConnection con = (HttpsURLConnection) queryurl.openConnection();
            con.setRequestMethod("POST");

            con.setRequestProperty("Content-length", String.valueOf(query.length()));
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Zabszk Network Login Plugin)");
            con.setDoOutput(true);
            con.setDoInput(true);

            DataOutputStream output = new DataOutputStream(con.getOutputStream());

            output.writeBytes(query);

            output.close();

            DataInputStream input = new DataInputStream(con.getInputStream());

            for (int c = input.read(); c != -1; c = input.read())
                System.out.print((char) c);
            input.close();

            return con.getResponseMessage();
        } catch (Exception e) {
            System.out.println("[Zabszk NetworkLogin] Can't connect to authentication server!");
            e.printStackTrace();
            return null;
        }
    }

    public static String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', Main.getInstance().config.getString("AlreadyLogged"));
    }

    public static void SendReminderAll() {
        Player[] online = getOnline();

        for (int i = 0; i < online.length; i++) {
            Main.getInstance().SendReminder(online[i]);
        }
    }
}

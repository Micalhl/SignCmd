package com.mcstarrysky.signcmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class SignCmd extends JavaPlugin implements Listener {

    private static String ALL_CHECK;
    private static String ALL_SHOW;
    private static String CMD_CHECK;
    private static String CMD_SHOW;
    private static boolean CMD_WHITELIST;
    private static boolean CMD_BLACKLIST;
    private static List<String> CMD_WHITE;
    private static List<String> CMD_BLACK;
    private static String CONSOLE_CHECK;
    private static String CONSOLE_SHOW;
    private static String LANG_FAKE_CMD_SIGN;
    private static String LANG_FAKE_LOG;
    private static String LANG_HELP;
    private static String LANG_SUCCESS_PLAYER;
    private static String LANG_SUCCESS_CONSOLE;
    private static String LANG_DENIED;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void loadConfig() {
        reloadConfig();
        ALL_CHECK = getConfig().getString("all.check");
        ALL_SHOW = getConfig().getString("all.show");
        CMD_CHECK = getConfig().getString("cmd.check");
        CMD_SHOW = getConfig().getString("cmd.show");
        CMD_WHITELIST = getConfig().getBoolean("cmd.whitelist");
        CMD_BLACKLIST = getConfig().getBoolean("cmd.blacklist");
        CMD_WHITE = getConfig().getStringList("cmd.white");
        CMD_BLACK = getConfig().getStringList("cmd.black");
        CONSOLE_CHECK = getConfig().getString("console.check");
        CONSOLE_SHOW = getConfig().getString("console.show");
        LANG_FAKE_CMD_SIGN = getConfig().getString("lang.fake-cmd-sign");
        LANG_FAKE_LOG = getConfig().getString("lang.fake-log");
        LANG_HELP = getConfig().getString("lang.help");
        LANG_SUCCESS_PLAYER = getConfig().getString("lang.success-player");
        LANG_SUCCESS_CONSOLE = getConfig().getString("lang.success-console");
        LANG_DENIED = getConfig().getString("lang.denied");
    }

    public static String colored(String origin) {
        return ChatColor.translateAlternateColorCodes('&', origin);
    }

    public static String uncolored(String origin) {
        return ChatColor.stripColor(origin);
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        final String[] sign = event.getLines();
        if (uncolored(sign[0]).equals(uncolored(colored(ALL_SHOW + CONSOLE_SHOW)))) {
            event.getPlayer().sendMessage(colored(LANG_FAKE_CMD_SIGN));
            getLogger().info(colored(LANG_FAKE_LOG.replace("{name}", event.getPlayer().getName())));
            event.setCancelled(true);
            return;
        }
        if (sign.length < 2) {
            return;
        }
        if (sign[0].contains(" ") && sign[0].split(" ").length == 2) {
            final String cmd = sign[0].split(" ")[0];
            final String type = sign[0].split(" ")[1];
            if (cmd.equals(ALL_CHECK)) {
                if (type.equals(CMD_CHECK)) {
                    if (!event.getPlayer().hasPermission("sign.cmd")) {
                        return;
                    }
                    if (!sign[1].startsWith("/")) {
                        event.getPlayer().sendMessage(colored(LANG_HELP));
                        return;
                    }
                    if (CMD_WHITELIST && !CMD_WHITE.contains(sign[1])) {
                        event.getPlayer().sendMessage(colored(LANG_DENIED));
                        return;
                    }
                    if (CMD_BLACKLIST && CMD_BLACK.contains(sign[1])) {
                        event.getPlayer().sendMessage(colored(LANG_DENIED));
                        return;
                    }
                    event.setLine(0, colored(ALL_SHOW + CMD_SHOW));
                    event.getPlayer().sendMessage(colored(LANG_SUCCESS_PLAYER));
                } else if (type.equals(CONSOLE_CHECK)) {
                    if (!event.getPlayer().hasPermission("sign.console")) {
                        return;
                    }
                    if (!sign[1].startsWith("/")) {
                        event.getPlayer().sendMessage(colored(LANG_HELP));
                        return;
                    }
                    event.setLine(0, colored(ALL_SHOW + CONSOLE_SHOW));
                    event.getPlayer().sendMessage(colored(LANG_SUCCESS_CONSOLE));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getHand().equals(EquipmentSlot.HAND)) {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (event.getClickedBlock() == null) {
                    return;
                }
                if (event.getClickedBlock().getType().name().toLowerCase().contains("sign")) {
                    final Sign sign = (Sign) event.getClickedBlock().getState();
                    if (sign.getLines().length < 2) {
                        return;
                    }
                    final String cmd = sign.getLine(1);
                    if (uncolored(sign.getLine(0)).equals(uncolored(colored(ALL_SHOW + CMD_SHOW)))) {
                        event.getPlayer().performCommand(cmd.substring(1).replace("%p", event.getPlayer().getName()));
                    } else if (uncolored(sign.getLine(0)).equals(uncolored(colored(ALL_SHOW + CONSOLE_SHOW)))) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.substring(1).replace("%p", event.getPlayer().getName()));
                    }
                }
            }
        }
    }
}

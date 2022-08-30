package com.rmjtromp.chatemojis;

import com.rmjtromp.chatemojis.exceptions.ConfigException;
import com.rmjtromp.chatemojis.utils.ComponentBuilder;
import com.rmjtromp.chatemojis.utils.Version;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

class CommandHandler implements CommandExecutor, TabCompleter {

    private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();

    @Getter(lazy = true)
    private static final CommandHandler handler = new CommandHandler();

    private static final String help_message = ChatColor.translateAlternateColorCodes('&', String.join("\n", Arrays.asList(
        "&6ChatEmojis &7- &fList of Commands",
        "&e/emoji [list] &e- &7Shows a list of all emojis",
        "&e/emoji help &e- &7Shows this list of commands",
        "&e/emoji reload &e- &7Reloads all emojis",
        "&e/emoji version &e- &7Shows the plugin version",
        "&e/emoji settings &e- &7Toggle plugin settings"
    )));

    CommandHandler() {}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender.hasPermission("chatemojis.command") || sender.hasPermission("chatemojis.list")) {
            if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("list"))) {
                if(sender instanceof Player) {
                    ComponentBuilder builder = new ComponentBuilder("&6ChatEmojis &7(v"+PLUGIN.getDescription().getVersion()+")\n");

                    BaseComponent[] hoverMessage = new ComponentBuilder("&6ChatEmojis\n&7Version: &e"+PLUGIN.getDescription().getVersion()+"\n&7Author: &eRMJTromp\n\n&eClick to open spigot resource page.").create();

                    // new Text(BaseComponent[]) is not added until 1.16
                    HoverEvent hoverEvent;
                    if(Version.getServerVersion().isOlderThan(Version.V1_16)) hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage);
                    else hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverMessage));
                    builder.event(hoverEvent);

                    Player player = (Player) sender;
                    if(Version.getServerVersion().isOlderThan(Version.V1_8)) {
                        // idk new-lines dont work on 1.7
                        player.spigot().sendMessage(builder.create());
                        PLUGIN.emojis.getComponents((Player) sender).forEach(baseComponents -> player.spigot().sendMessage(baseComponents));
                    } else {
                        List<BaseComponent[]> components = PLUGIN.emojis.getComponents((Player) sender);
                        for(int i = 0; i < components.size(); i++) {
                            builder.append(components.get(i), ComponentBuilder.FormatRetention.NONE);
                            if(i != components.size() - 1) builder.append("\n", ComponentBuilder.FormatRetention.NONE);
                        }

                        player.spigot().sendMessage(builder.create());
                    }
                } else sender.sendMessage(help_message);
            } else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("help")) sender.sendMessage(help_message);
                else if(args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission("chatemojis.reload")) {
                        long start = System.currentTimeMillis();
                        try {
                            PLUGIN.reloadConfig();
                            PLUGIN.emojis = EmojiGroup.init(PLUGIN.getConfig());
                        } catch (ConfigException e) {
                            e.printStackTrace();
                        }
                        long interval = System.currentTimeMillis() - start;
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eAll emojis and groups have been reloaded &7("+ interval +"ms)"));
                    } else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
                } else if(args[0].equalsIgnoreCase("settings")) {
                    if(sender instanceof Player) {
                        if(sender.hasPermission("chatemojis.admin")) ((Player) sender).openInventory(PLUGIN.settingsWindow.getInventory());
                        else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
                    } else sender.sendMessage(ChatColor.RED + "Emojis are only available to players.");
                } else if(args[0].toLowerCase().matches("^v(?:er(?:sion)?)?$")) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This server is currently running &eChatEmojis &7(v"+PLUGIN.getDescription().getVersion()+")"));
                else sender.sendMessage(ChatColor.RED + "Unknown argument. Try \"/emoji help\" for a list of commands.");
            } else sender.sendMessage(ChatColor.RED + "Too many arguments. Try \"/emoji help\" for a list of commands.");
        } else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}

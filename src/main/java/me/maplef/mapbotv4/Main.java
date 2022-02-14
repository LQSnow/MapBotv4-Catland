package me.maplef.mapbotv4;

import me.maplef.mapbotv4.commands.Mapbot;
import me.maplef.mapbotv4.listeners.GameListeners;
import me.maplef.mapbotv4.managers.LoopJobManager;
import me.maplef.mapbotv4.plugins.BotQQOperator;
import me.maplef.mapbotv4.utils.CU;
import me.maplef.mapbotv4.utils.DatabaseOperator;
import me.maplef.mapbotv4.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    private FileConfiguration messageConfig;
    private FileConfiguration onlineTimeConfig;
    private static Main instance;

    public final Long botAcc = getConfig().getLong("bot-account");
    public final Long opGroup = getConfig().getLong("op-group");

    private final String botPassword = getConfig().getString("bot-password");

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(); registerConfig();
        getMessageConfig().options().copyDefaults();

        Bukkit.getServer().getLogger().info(messageConfig.getString("enable-message.console"));

        if(!getDataFolder().exists()){
            getLogger().warning("请在生成的配置文件中修改相关配置再启动本插件");
            this.saveDefaultConfig();
            this.saveResource("messages.yml", false);
            this.saveResource("cat_images/catImageSample.jpg", false);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            getLogger().warning("找不到前置插件PlaceHolderAPI，请安装该插件！");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        instance = this;

        BotQQOperator.login();

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(new GameListeners(), this);

        Objects.requireNonNull(getCommand("mapbot")).setExecutor(new Mapbot());
        Objects.requireNonNull(getCommand("mapbot")).setTabCompleter(new Mapbot());

        try {
            DatabaseOperator.init();
            LoopJobManager.register();
        } catch (SQLException ignored){}

        getServer().broadcastMessage(CU.t(messageConfig.getString("message-prefix") + messageConfig.getString("enable-message.server")));
    }

    @Override
    public void onDisable() {
        try {
            DatabaseOperator.c.close();
            Scheduler.scheduler.shutdown();
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getClass().getName() + ": " + e.getMessage());
        }

        BotQQOperator.logout();

        getServer().broadcastMessage(CU.t(messageConfig.getString("message-prefix") + messageConfig.getString("disable-message")));
        getServer().removeRecipe(NamespacedKey.minecraft("newelytra"));
        getLogger().info(messageConfig.getString("disable-message.console"));
    }

    public static Main getInstance() {
        return instance;
    }

    public void registerConfig() {
        messageConfig = YamlConfiguration.loadConfiguration(new File(".\\plugins\\MapBot\\messages.yml"));
        onlineTimeConfig = YamlConfiguration.loadConfiguration(new File(".\\plugins\\PlayTimeTracker\\database.yml"));
    }

    public FileConfiguration getMessageConfig() {
        return messageConfig;
    }

    public FileConfiguration getOnlineTimeConfig() {
        return onlineTimeConfig;
    }
}
package com.rei.pointplugin;

import com.rei.pointplugin.command.RankingCommand;
import com.rei.pointplugin.listener.AdvancementListener;
import com.rei.pointplugin.listener.LoginBonusListener;
import com.rei.pointplugin.listener.ShopListener;
import com.rei.pointplugin.storage.DataStorage;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private PointManager pointManager;
    private DataStorage dataStorage;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.pointManager = new PointManager();
        this.dataStorage = new DataStorage(this);

        getServer().getPluginManager().registerEvents(new AdvancementListener(this, pointManager, dataStorage), this);
        getServer().getPluginManager().registerEvents(new LoginBonusListener(this, pointManager, dataStorage), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this, pointManager, dataStorage), this);

        final PluginCommand command = getCommand("ranking");
        if (command != null) {
            command.setExecutor(new RankingCommand(pointManager));
        }
    }

    @Override
    public void onDisable() {
        dataStorage.save();
    }
}

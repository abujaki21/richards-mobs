package io.github.richardyin.richardsmobs;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class RichardsMobs extends JavaPlugin {
	public static Plugin plugin;

	@Override
	public void onEnable() {
		plugin = this;
		getServer().getPluginManager().registerEvents(new KnightListener(),
				this);
		KnightListener.configureMeleeHorse();
		KnightListener.configureWitherKnight();
	}

	@Override
	public void onDisable() {

	}
}

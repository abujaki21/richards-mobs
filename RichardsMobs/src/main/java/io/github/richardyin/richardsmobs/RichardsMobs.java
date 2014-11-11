package io.github.richardyin.richardsmobs;

import org.bukkit.plugin.java.JavaPlugin;

public class RichardsMobs extends JavaPlugin {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new KnightListener(),
				this);
		KnightListener.configureMeleeHorse();
		KnightListener.configureWitherKnight();
	}

	@Override
	public void onDisable() {

	}
}

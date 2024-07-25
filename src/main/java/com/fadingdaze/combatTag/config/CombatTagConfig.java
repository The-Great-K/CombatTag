package com.fadingdaze.combatTag.config;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.fadingdaze.combatTag.CombatTag;

public class CombatTagConfig {
	private static final CombatTagConfig INSTANCE = new CombatTagConfig();

	private File file;
	private YamlConfiguration config;

	public static HashSet<String> restrictedPerms = new HashSet<>();

	public static CombatTagConfig getInstance() {
		return INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public void load() {
		file = new File(CombatTag.getInstance().getDataFolder(), "config.yml");

		if (!file.exists())
			CombatTag.getInstance().saveResource("config.yml", false);

		config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		CombatTag.defaultTagTime = config.getInt("tag-time");

		restrictedPerms.addAll((List<String>) config.getList("restricted"));
	}
}

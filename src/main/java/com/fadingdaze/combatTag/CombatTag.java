package com.fadingdaze.combatTag;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import com.fadingdaze.combatTag.config.CombatTagConfig;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class CombatTag extends JavaPlugin implements Listener {
	public static int defaultTagTime;

	// Player uuid, remaining tag time
	public HashMap<Player, Integer> taggedPlayers = new HashMap<>();
	public HashMap<UUID, PermissionAttachment> perms = new HashMap<>();

	private final Runnable tickFunction = () -> {
		taggedPlayers.forEach((player, remainingTime) -> {
			taggedPlayers.replace(player, taggedPlayers.get(player) - 1);
			if (remainingTime <= 0) {
				removeTagRestrictions(player);
				taggedPlayers.remove(player);
			}
		});
	};

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);

		CombatTagConfig.getInstance().load();

		getLogger().info("RESTRICTIONS DEBUG:");
		getLogger().info(CombatTagConfig.restrictedPerms.toString());

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, tickFunction, 0, 20); // every second

		getLogger().info("COMBAT TAG PLUGIN SUCCESSFULLY LOADED!");
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		taggedPlayers.clear();
		CombatTagConfig.restrictedPerms.clear();
		defaultTagTime = 0;

		getLogger().info("COMBAT TAG PLUGIN SUCCESSFULLY EXITED!");
	}

	@EventHandler
	public void onPlayerHit(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player player) {
			setTagRestrictions(player);
			taggedPlayers.put(player, defaultTagTime);
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (taggedPlayers.containsKey(event.getPlayer())) {
			event.getPlayer().setHealth(0.0d);
			getServer().broadcast(Component.text(event.getPlayer().getName() + " combat logged, shame them!",
					NamedTextColor.DARK_RED));
			taggedPlayers.remove(event.getPlayer());
		}
	}

	public void setTagRestrictions(Player player) {
		if (!taggedPlayers.containsKey(player)) {
			PermissionAttachment attachment = player.addAttachment(this);
			perms.put(player.getUniqueId(), attachment);

			CombatTagConfig.restrictedPerms.forEach(perm -> {
				PermissionAttachment permissionAttachment = perms.get(player.getUniqueId());
				permissionAttachment.setPermission(perm, true);
				getLogger().info("Setting permissions for " + player.getName());
				getLogger().info("Set permission: " + perm);
			});
		}
	}

	public void removeTagRestrictions(Player player) {
		if (taggedPlayers.containsKey(player)) {
			getLogger().info("UUID's match! Unsetting permissions for " + player.getName());

			CombatTagConfig.restrictedPerms.forEach(perm -> {
				perms.get(player.getUniqueId()).unsetPermission(perm);
				getLogger().info("Unset permission: " + perm);
			});

			perms.remove(player.getUniqueId());
		} else {
			getLogger().log(Level.WARNING, "Attempted to unset permissions for non tagged player!");
		}
	}

	public static CombatTag getInstance() {
		return getPlugin(CombatTag.class);
	}
}

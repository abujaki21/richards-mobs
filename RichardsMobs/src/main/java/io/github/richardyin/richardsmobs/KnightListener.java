package io.github.richardyin.richardsmobs;

import java.util.NoSuchElementException;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import emp.HellFire.Cmobs.CountedError;
import emp.HellFire.Cmobs.CustomMobSpawnEvent;
import emp.HellFire.Cmobs.CustomMobSpawnEvent.SpawnReason;
import emp.HellFire.Cmobs.CustomMobs;
import emp.HellFire.Cmobs.api.CustomMob;
import emp.HellFire.Cmobs.api.CustomMob.EquipmentSlot;
import emp.HellFire.Cmobs.api.CustomMobsAPI;
import emp.HellFire.Cmobs.api.FileCustomMob;
import emp.HellFire.Cmobs.api.edit.FileEditor;

public class KnightListener implements Listener {
	@EventHandler
	public void onCmobSpawn(CustomMobSpawnEvent event) {
		// Wither Knight
		if (event.getMobName().equals("WitherKnight")) {
			PlayerDisguise disguise = new PlayerDisguise("Wither_Knight");
			DisguiseAPI.disguiseEntity(event.getSpawned(), disguise);
			try {
				LivingEntity horse = CustomMobs.spawnCmob(event.getSpawned()
						.getLocation(), "MeleeHorse", SpawnReason.COMMMAND);
				horse.setPassenger(event.getSpawned());
			} catch (NoSuchElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (CountedError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		} else if (event.getMobName().equals("MeleeHorse")) {
			Horse horse = (Horse) event.getSpawned();
			horse.setColor(Color.BLACK);
			horse.getInventory().setArmor(new ItemStack(Material.IRON_BARDING));
		}
	}

	public static void configureWitherKnight() {
		if (CustomMobsAPI.loadCustomMob("WitherKnight") != null)
			return;
		CustomMob wknight = CustomMobsAPI.createNewCustomMob("WitherKnight",
				EntityType.ZOMBIE);
		FileEditor editor = CustomMobsAPI.createFileEditorFor(wknight);
		editor.setCustomName("Wither Knight");
		editor.addEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,
				Integer.MAX_VALUE, 2));
		editor.addEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
				Integer.MAX_VALUE, 1));
		editor.setHealth(60);
		editor.setEquipment(EquipmentSlot.BOOTS, new ItemStack(
				Material.IRON_BOOTS));
		editor.setEquipment(EquipmentSlot.LEGGINGS, new ItemStack(
				Material.IRON_LEGGINGS));
		editor.setEquipment(EquipmentSlot.CHESTPLATE, new ItemStack(
				Material.IRON_CHESTPLATE));
		editor.setEquipment(EquipmentSlot.HELMET, new ItemStack(
				Material.IRON_HELMET));
		ItemStack sword = new ItemStack(Material.IRON_SWORD);
		sword.addEnchantment(Enchantment.DAMAGE_ALL, 4);
		editor.setEquipment(EquipmentSlot.WEAPON, sword);
	}

	public static void configureMeleeHorse() {
		if (CustomMobsAPI.loadCustomMob("MeleeHorse") != null)
			return;
		FileCustomMob mhorse = CustomMobsAPI.createNewCustomMob("MeleeHorse",
				EntityType.HORSE);
		FileEditor editor = CustomMobsAPI.createFileEditorFor(mhorse);
		editor.setHealth(100);
		editor.addEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
				Integer.MAX_VALUE, 1));
	}
}

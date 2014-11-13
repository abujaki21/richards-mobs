package io.github.richardyin.richardsmobs;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import de.ntcomputer.minecraft.controllablemobs.api.ControllableMob;
import de.ntcomputer.minecraft.controllablemobs.api.ControllableMobs;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIAttackMelee;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIBehavior;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AIRandomStroll;
import de.ntcomputer.minecraft.controllablemobs.api.ai.behaviors.AITargetNearest;
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
			spawnWitherKnight(event);
			return;
		} else if (event.getMobName().equals("MeleeHorse")) {
			// Wither Knight's horse
			Horse horse = (Horse) event.getSpawned();
			horse.setColor(Color.BLACK);
			horse.getInventory().setArmor(new ItemStack(Material.IRON_BARDING));
			ControllableMob<Horse> control = ControllableMobs
					.putUnderControl(horse);

			// fast, can't be knocked back, attacks from a long distance
			control.getAttributes().getKnockbackResistanceAttribute()
					.setBasisValue(1);
			control.getAttributes().setAvoidWater(true);
			control.getAttributes().setMaximumNavigationDistance(80);

			// uses melee attack, does not buck rider
			control.getAI().clear();
			control.getAI().addBehavior(new AIAttackMelee(1, 2));
			control.getAI().addBehavior(new AIRandomStroll(2, 0.3));

			// targets players from a long way off
			AIBehavior<LivingEntity> target = new AITargetNearest(1, 80, false);
			control.getAI().addBehavior(target);
		}
	}

	private void spawnWitherKnight(CustomMobSpawnEvent event) {
		Zombie knight = (Zombie) event.getSpawned();
		PlayerDisguise disguise = new PlayerDisguise("");
		DisguiseAPI.disguiseEntity(knight, disguise);
		knight.setCustomName("Wither Knight");
		knight.setCustomNameVisible(false);
		try {
			LivingEntity horse = CustomMobs.spawnCmob(event.getSpawned()
					.getLocation(), "MeleeHorse", SpawnReason.COMMMAND);
			horse.setPassenger(knight);
			scheduleHorseAttackChecks(horse);
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CountedError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType() == EntityType.ZOMBIE) {
			if (new String("Wither Knight")
					.equals(((Zombie) event.getDamager()).getCustomName())) {
				LivingEntity target = (LivingEntity) event.getEntity();
				if (event.getDamager().isInsideVehicle()) { // horse attack
					Entity horse = event.getDamager().getVehicle();
					// short duration slow
					target.addPotionEffect(new PotionEffect(
							PotionEffectType.SLOW, 40, 4));
					// lots of knockback
					double x = target.getLocation().getX()
							- horse.getLocation().getX();
					double z = target.getLocation().getZ()
							- horse.getLocation().getZ();
					target.setVelocity(new Vector(x * 1.5, 0.5, z * 1.5));
				} else { // knight on foot
					event.setDamage(60.0); // lots of damage
				}
				// wither effect either way
				target.addPotionEffect(new PotionEffect(
						PotionEffectType.WITHER, 100, 1));
			}
		}
	}

	// I can't believe I have to do this
	// why doesn't vehicleEntityCollisionEvent work with horses =(
	private Map<LivingEntity, Integer> horseAttacks = new ConcurrentHashMap<>();

	private void scheduleHorseAttackChecks(final LivingEntity horse) {
		if (horse.getPassenger() == null) {
			return;
		}
		final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		horseAttacks.put(horse, scheduler.scheduleSyncRepeatingTask(
				RichardsMobs.plugin, new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (horse.getPassenger() == null) {
							horse.damage(1000.0); // kill it
							scheduler.cancelTask(horseAttacks.get(horse));
						}
						for (Entity e : horse.getNearbyEntities(1, 1, 1)) {
							if (e instanceof HumanEntity) {
								((HumanEntity) e).damage(20.0,
										horse.getPassenger());
							}
						}
					}
				}, 1, 1));
		// Bukkit.getServer().getLogger()
		// .info("Successfully scheduled horse attack checks");
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if (horseAttacks.containsKey(entity)) {
			Bukkit.getServer().getScheduler()
					.cancelTask(horseAttacks.get(entity));
			horseAttacks.remove(entity);
			return;
		}
		if (new String("Wither Knight").equals(entity.getCustomName())) {
			if (entity.getVehicle() instanceof LivingEntity) {
				LivingEntity vehicle = (LivingEntity) entity.getVehicle();
				vehicle.damage(1000.0, event.getEntity().getKiller());
			}
		}
	}

	// @EventHandler
	// public void onVehicleCollideWithEntity(VehicleEntityCollisionEvent event)
	// {
	// if (event.getVehicle() instanceof Horse)
	// if (event.getVehicle().getPassenger() instanceof LivingEntity
	// && event.getEntity() instanceof HumanEntity)
	// if (new String("Wither Knight").equals(((LivingEntity) event
	// .getVehicle().getPassenger()).getCustomName()))
	// ((LivingEntity) event.getEntity()).damage(20.0, event
	// .getVehicle().getPassenger());
	//
	// }

	public static void configureWitherKnight() {
		if (CustomMobsAPI.loadCustomMob("WitherKnight") != null)
			return;
		CustomMob wknight = CustomMobsAPI.createNewCustomMob("WitherKnight",
				EntityType.ZOMBIE);
		FileEditor editor = CustomMobsAPI.createFileEditorFor(wknight);
		editor.setCustomName("Wither Knight");
		editor.addEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
				Integer.MAX_VALUE, 1));
		editor.setHealth(40);
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
		editor.setHealth(80);
		editor.addEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
				Integer.MAX_VALUE, 1));
	}
}

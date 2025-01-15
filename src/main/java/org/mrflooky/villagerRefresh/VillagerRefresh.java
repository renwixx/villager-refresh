package org.mrflooky.villagerRefresh;

import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;

import java.util.ArrayList;
import java.util.List;

public final class VillagerRefresh extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Create a default config if it doesn't exist
        saveDefaultConfig();

        // Read the interval from the config (in minutes) and convert it to ticks
        long refreshInterval = getConfig().getLong("refresh-interval-minutes", 20) * 20 * 60;

        // Регистрируем слушатель событий
        Bukkit.getPluginManager().registerEvents(this, this);

        // Register the event listener
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            getServer().getWorlds().forEach(world -> world.getEntitiesByClass(Villager.class).forEach(villager -> {
                List<MerchantRecipe> newRecipes = new ArrayList<>();

                for (MerchantRecipe recipe : villager.getRecipes()) {
                    MerchantRecipe newRecipe = new MerchantRecipe(
                            recipe.getResult(),
                            0,
                            recipe.getMaxUses(),
                            recipe.hasExperienceReward(),
                            recipe.getVillagerExperience(),
                            recipe.getPriceMultiplier()
                    );
                    newRecipe.setIngredients(recipe.getIngredients());
                    newRecipes.add(newRecipe);
                }

                villager.setRecipes(newRecipes);
            }));
            getLogger().info("Trades refreshed for all villagers!");
        }, 0L, refreshInterval);

        getLogger().info("VillagerRefresh enabled! Refresh interval: " +
                getConfig().getLong("refresh-interval-minutes") + " minutes");
    }

    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        event.setCancelled(false);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }
}
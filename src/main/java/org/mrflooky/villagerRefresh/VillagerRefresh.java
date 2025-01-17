package org.mrflooky.villagerRefresh;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class VillagerRefresh extends JavaPlugin implements Listener, TabCompleter {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerVillagerRefreshTask();
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("vrefresh")).setTabCompleter(this);
        getLogger().info("VillagerRefresh enabled! Refresh interval: " +
                getConfig().getLong("refresh-interval-minutes") + " minutes");
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void registerVillagerRefreshTask() {
        long refreshInterval = getConfig().getLong("refresh-interval-minutes", 20) * 20 * 60;
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            getServer().getWorlds().forEach(world -> world.getEntitiesByClass(Villager.class).forEach(villager -> {
                if (villager.getProfession() != Villager.Profession.NONE &&
                        villager.getProfession() != Villager.Profession.NITWIT &&
                        villager.getMemory(MemoryKey.JOB_SITE) != null) {
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
                }
            }));
            getLogger().info("Trades refreshed for all villagers!");
        }, 0L, refreshInterval);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("vrefresh") || args.length == 0)
            return false;
        if (!sender.hasPermission("villagerrefresh.commands")) {
            sender.sendMessage("[VillagerRefresh] You do not have permission to use this command.");
            return true;
        }
        String task = args[0].toLowerCase();
        if (task.equals("reload")) {
            reloadConfig();
            Bukkit.getScheduler().cancelTasks(this);
            registerVillagerRefreshTask();
            sender.sendMessage("[VillagerRefresh] Config reloaded and tasks refreshed!");
            return true;
        }
        if (task.equals("set")) {
            if (args.length == 1)
                sender.sendMessage("[VillagerRefresh] Missing argument [minutes]!");
            try {
                int interval = Integer.parseInt(args[1]);
                getConfig().set("refresh-interval-minutes", interval);
                saveConfig();
                Bukkit.getScheduler().cancelTasks(this);
                registerVillagerRefreshTask();
                sender.sendMessage("[VillagerRefresh] Interval set to " + interval + " minutes and tasks refreshed!");
            } catch (NumberFormatException e) {
                sender.sendMessage("[VillagerRefresh] Invalid number! Use: /vrefresh set [minutes]");
            }
            return true;
        }
        sender.sendMessage("""
                [VillagerRefresh] Commands:
                /vrefresh reload: Reload config file.
                /vrefresh set [minutes]: Set interval to refresh.""");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("vrefresh")) {
            if (args.length == 1) {
                return Arrays.asList("reload", "set");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
                return Arrays.asList("1", "5", "10", "30", "60");
            }
        }
        return new ArrayList<>();
    }
}
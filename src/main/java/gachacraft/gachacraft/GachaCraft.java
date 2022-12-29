package gachacraft.gachacraft;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class GachaCraft extends JavaPlugin {

    // This map will store the different gacha games, with their names as keys and their reward lists as values
    private Map<String, List<ItemStack>> gachas = new HashMap<>();
    // This map will store the costs of the different gacha games, with their names as keys and their costs as values
    private Map<String, Integer> costs = new HashMap<>();
    // This map will store the currency materials of the different gacha games, with their names as keys and the materials as values
    private Map<String, ItemStack> currencies = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("gachahelp")) {
            // Check if the sender has the required permission
            if (!sender.hasPermission("gachacraft.gacha")) {
                sender.sendMessage("You do not have permission to use this command!");
                return true;
            }
            sender.sendMessage("GachaCraft commands:");
            sender.sendMessage("/gacha <gacha name> - Play a gacha game.");
            sender.sendMessage("/gachahelp - Display this help message.");
            if (sender.hasPermission("gachacraft.admin")) {
                sender.sendMessage("/creategacha <gacha name> <cost> <currency material> - Create a new gacha game.");
                sender.sendMessage("/addgachaitem <gacha name> <chance>- Add the item in your hand to the reward list of a gacha game with the specified chance.");
                sender.sendMessage("/setgachacost <gacha name> <amount> - Set the cost of a gacha game.");
                sender.sendMessage("/setgachacurrency <gacha name> - Set the currency of a gacha game to the item in your hand.");
                sender.sendMessage("/listgachas - List all existing gacha games.");
                sender.sendMessage("/removegacha <gacha name> - Remove a gacha game and all its rewards.");
                sender.sendMessage("/removegachaitem <gacha name> - Remove an item from a gacha game's reward list.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("gacha")) {
            // Check if the sender has the required permission
            if (!sender.hasPermission("gachacraft.gacha")) {
                sender.sendMessage("You do not have permission to use this command!");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            Player player = (Player) sender;

            // Check if the player specified a gacha game
            if (args.length < 1) {
                player.sendMessage("Usage: /gacha <gacha name>");
                return true;
            }
            String gachaName = args[0];
            List<ItemStack> rewards = gachas.get(gachaName);
            if (rewards == null) {
                player.sendMessage("Gacha game not found: " + gachaName);
                return true;
            }
            int cost = costs.get(gachaName);
            ItemStack currency = currencies.get(gachaName);

            // Check if the player has enough resources to play the gacha game
            if (player.getInventory().containsAtLeast(currency, cost)) {
                // Remove the resources from the player's inventory
                ItemStack currencyCopy = currency.clone();
                currencyCopy.setAmount(cost);
                player.getInventory().removeItem(currencyCopy);

                // Generate a random reward for the player
                ItemStack reward = getRandomReward(rewards);
                player.getInventory().addItem(reward);
                player.sendMessage("You received a " + reward.getType().name() + " from the " + gachaName + " gacha game!");
            } else {
                player.sendMessage("You don't have enough resources to play the " + gachaName + " gacha game!");
            }

            return true;
        } else if (command.getName().equalsIgnoreCase("creategacha")) {
            // Check if the sender has the required permission
            if (!sender.hasPermission("gachacraft.admin")) {
                sender.sendMessage("You do not have permission to use this command!");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            Player player = (Player) sender;
            if (args.length < 2) {
                player.sendMessage("Usage: /creategacha <gacha name> <cost> [currency material]");
                return true;
            }
            String gachaName = args[0];
            if (gachas.containsKey(gachaName)) {
                player.sendMessage("Gacha game already exists: " + gachaName);
                return true;
            }
            try {
                int cost = Integer.parseInt(args[1]);
                if (cost < 1) {
                    player.sendMessage("Cost must be at least 1!");
                    return true;
                }
                ItemStack currency = null;
                if (args.length >= 3) {
                    // The currency material argument was specified
                    try {
                        Material currencyMaterial = Material.getMaterial(args[2]);
                        if (currencyMaterial == null) {
                            player.sendMessage("Invalid currency material: " + args[2]);
                            return true;
                        }
                        currency = new ItemStack(currencyMaterial);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("Invalid currency material: " + args[2]);
                        return true;
                    }
                } else {
                    // The currency material argument was not specified, use the item in the player's hand
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (heldItem == null || heldItem.getType() == Material.AIR) {
                        player.sendMessage("You must be holding an item in your hand to use it as the currency for the gacha game!");
                        return true;
                    }
                    currency = heldItem.clone();
                    // Set the amount of the currency to 1, since we only need one of it to play the gacha game
                    currency.setAmount(1);
                }
                // Create a new empty reward list for the gacha game
                List<ItemStack> rewards = new ArrayList<>();
                // Add the gacha game to the maps
                gachas.put(gachaName, rewards);
                costs.put(gachaName, cost);
                currencies.put(gachaName, currency);
                player.sendMessage("Created gacha game: " + gachaName + " (cost: " + cost + ", currency: " + currency.getType().name() + ")");
                return true;
            } finally {

            }

        } else     if (command.getName().equalsIgnoreCase("addgachaitem")) {
            // Check if the sender has the required permission
            if (!sender.hasPermission("gachacraft.admin")) {
                sender.sendMessage("You do not have permission to use this command!");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            Player player = (Player) sender;
            if (args.length < 2) {
                player.sendMessage("Usage: /addgachaitem <gacha name> <chance>");
                return true;
            }
            String gachaName = args[0];
            List<ItemStack> rewards = gachas.get(gachaName);
            if (rewards == null) {
                player.sendMessage("Gacha game not found: " + gachaName);
                return true;
            }
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem == null || heldItem.getType() == Material.AIR) {
                player.sendMessage("You must be holding an item in your hand to add it to the gacha reward list!");
                return true;
            }
            try {
                double chance = Double.parseDouble(args[1]);
                if (chance < 0 || chance > 100) {
                    player.sendMessage("Chance must be between 0 and 100!");
                    return true;
                }
                int odds = (int) (100 / chance);
                ItemStack reward = heldItem.clone();
                reward.setAmount(odds);
                rewards.add(reward);
                player.sendMessage("Added " + heldItem.getType().name() + " to the reward list of the " + gachaName + " gacha game with a " + chance + "% chance of receiving it!");
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid chance: " + args[1]);
            }
            return true;
        }

        else if (command.getName().equalsIgnoreCase("setgachacost")) {
            // Check if the sender has the required permission
            if (!sender.hasPermission("gachacraft.admin")) {
                sender.sendMessage("You do not have permission to use this command!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("Usage: /setgachacost <gacha name> <amount>");
                return true;
            }
            String gachaName = args[0];
            List<ItemStack> rewards = gachas.get(gachaName);
            if (rewards == null) {
                sender.sendMessage("Gacha game not found: " + gachaName);
                return true;
            }
            try {
                int cost = Integer.parseInt(args[1]);
                costs.put(gachaName, cost);
                sender.sendMessage("Set the cost of the " + gachaName + " gacha game to " + cost + "!");
            } catch (NumberFormatException ex) {
                sender.sendMessage("Invalid cost: " + args[1]);
                return true;
            }

            return true;
        } else if (command.getName().equalsIgnoreCase("setgachacurrency")) {
            // Check if the sender has the required permission
            if (command.getName().equalsIgnoreCase("setgachacurrency")) {
                // Check if the sender has the required permission
                if (!sender.hasPermission("gachacraft.admin")) {
                    sender.sendMessage("You do not have permission to use this command!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command!");
                    return true;
                }
                Player player = (Player) sender;
                if (args.length < 1) {
                    player.sendMessage("Usage: /setgachacurrency <gacha name>");
                    return true;
                }
                String gachaName = args[0];
                List<ItemStack> rewards = gachas.get(gachaName);
                if (rewards == null) {
                    player.sendMessage("Gacha game not found: " + gachaName);
                    return true;
                }
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem == null || heldItem.getType() == Material.AIR) {
                    player.sendMessage("You must be holding an item in your hand to set it as the gacha currency!");
                    return true;
                }
                currencies.put(gachaName, heldItem);
                player.sendMessage("Set the currency of the " + gachaName + " gacha game to " + heldItem.getType().name() + "!");

                return true;
            }

            } else if (command.getName().equalsIgnoreCase("listgachas")) {
            // Check if the sender has the required permission
            if (!sender.hasPermission("gachacraft.gacha")) {
                sender.sendMessage("You do not have permission to use this command!");
                return true;
            }
            String gachaList = "Existing gacha games:";
            for (Map.Entry<String, List<ItemStack>> entry : gachas.entrySet()) {
                String gachaName = entry.getKey();
                List<ItemStack> rewards = entry.getValue();
                int cost = costs.get(gachaName);
                ItemStack currency = currencies.get(gachaName);
                gachaList += "\n - " + gachaName + ": Cost: " + cost + ", Currency: " + currency.getType().name() + ", Rewards: ";
                for (ItemStack reward : rewards) {
                    gachaList += reward.getType().name() + ", ";
                }
                gachaList = gachaList.substring(0, gachaList.length() - 2);
            }
            sender.sendMessage(gachaList);

            return true;
        } else if (command.getName().equalsIgnoreCase("removegacha")) {
            // Check if the sender has the required permission
            if (!sender.hasPermission("gachacraft.admin")) {
                sender.sendMessage("You do not have permission to use this command!");
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage("Usage: /removegacha <gacha name>");
                return true;
            }
            String gachaName = args[0];
            List<ItemStack> rewards = gachas.remove(gachaName);
            if (rewards == null) {
                sender.sendMessage("Gacha game not found: " + gachaName);
                return true;
            }
            costs.remove(gachaName);
            currencies.remove(gachaName);
            sender.sendMessage("Removed gacha game '" + gachaName + "' and all its rewards!");

            return true;
        } else if (command.getName().equalsIgnoreCase("removegachaitem")) {
            // Check if the sender has the required permission
            if (!sender.hasPermission("gachacraft.admin")) {
                sender.sendMessage("You do not have permission to use this command!");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }
            Player player = (Player) sender;
            if (args.length < 1) {
                player.sendMessage("Usage: /removegachaitem <gacha name>");
                return true;
            }
            String gachaName = args[0];
            List<ItemStack> rewards = gachas.get(gachaName);
            if (rewards == null) {
                player.sendMessage("Gacha game not found: " + gachaName);
                return true;
            }
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem == null || heldItem.getType() == Material.AIR) {
                player.sendMessage("You must be holding an item in your hand to remove it from the gacha reward list!");
                return true;
            }
            if (rewards.remove(heldItem)) {
                player.sendMessage("Removed " + heldItem.getType().name() + " from the " + gachaName + " gacha reward list!");
            } else {
                player.sendMessage("Item not found in the " + gachaName + " gacha reward list: " + heldItem.getType().name());
            }

            return true;
        }
        return false;
    }

    private ItemStack getRandomReward(List<ItemStack> rewards) {
        Random random = new Random();
        int index = random.nextInt(rewards.size());
        return rewards.get(index);
    }
}

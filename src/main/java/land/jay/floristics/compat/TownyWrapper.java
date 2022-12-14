/** Copyright (C) 2019 Jay Avery */
package land.jay.floristics.compat;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import land.jay.floristics.Floristics;

public class TownyWrapper {

    /** Custom field for growth permission. */
    private static final BooleanDataField FIELD = new BooleanDataField("floristics", false);

    /** @return Whether compatibility was successfully set up. */
    public static boolean onLoad() {

        Floristics.info("Towny is present, adding field to registry.");
        try {
            TownyAPI.getInstance().registerCustomDataField(FIELD);
            return true;
        } catch (KeyAlreadyRegisteredException ex) {
            Floristics.error("Someone has already registered a floristics field for Towny, this should never happen!\n" +
                    "Towny compatibility will be DISABLED.", ex);
            return false;
        }
    }

    //TODO Refactor function & remove commented code
    public static boolean canGrow(Location location) {

        return TownyAPI.getInstance().isWilderness(location);

        /*

        if (!town.hasMeta() || !town.getMetadata().contains(FIELD)) {
            town.addMetaData(FIELD);
        }

        BooleanDataField field = null;
        for (CustomDataField metadata : town.getMetadata()) {
            if (metadata.equals(FIELD)) {
                field = (BooleanDataField) metadata;
            }
        }

        return field.getValue();

         */
    }

    public static void handleCommand(CommandSender sender, String[] args) {

        boolean validInfo = args.length == 1;
        boolean validInfo2 = args.length == 2 && (args[1].equals("here"));
        boolean validChange = args.length == 2 && (args[1].equals("enable") || args[1].equals("disable"));
        boolean validChange2 = args.length == 3 && (args[2].equals("enable") || args[2].equals("disable"));

        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to use this command.");
            return;
        }

        if (!(validInfo||validInfo2) && !(validChange||validChange2)) {
            player.sendMessage("""
                §cUnknown command or invalid arguments.
                §Usage:
                    §6/floristics towny §f- §7Display whether growth is enabled in your Town.
                    §6/floristics towny <enable|disable> §f- §7Enable or disable growth your Town.
           """);
            if (player.hasPermission("floristics.admin.towny")) {
                player.sendMessage("""
                        §6/floristics towny here §f- §7Display whether growth is enabled in current Town.
                        §6/floristics towny here <enable|disable> §f- §7Enable or disable growth in current Town.
                """);
            }
            return;
        }

        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
        if (resident == null) {
            player.sendMessage("§cError: could not find your resident data!");
            return;
        }

        Town town;
        if (validInfo||validChange) {
            town = TownyAPI.getInstance().getResidentTownOrNull(resident);
            if (!resident.isMayor() || town == null) {
                player.sendMessage("§cYou are not a town mayor.");
                return;
            }
        } else if (player.hasPermission("floristics.towny.admin")) {
            town = TownyAPI.getInstance().getTown(player.getLocation());
            if (town == null) {
                player.sendMessage("§cCouldn't find any town in this location.");
                return;
            }
        } else return;

        if (!town.getMetadata().contains(FIELD)) {
            town.addMetaData(FIELD);
        }

        BooleanDataField field = null;
        for (CustomDataField<?> metadata : town.getMetadata()) {
            if (metadata.equals(FIELD)) {
                field = (BooleanDataField) metadata;
            }
        }

        if (field == null) {
            player.sendMessage("§cYou're not inside a Town.");

        } else if (validChange||validChange2) {
            String change = validChange ? args[1] : args[2];
            if (change.equals("enable")) {
                field.setValue(true);
                player.sendMessage("§2Growth enabled in this Town.");
            } else if (change.equals("disable")) {
                field.setValue(false);
                player.sendMessage("§cGrowth disabled in this Town.");
            }

        } else {
            player.sendMessage("§3Growth in this Town is currently " + (field.getValue() ? "§2enabled." : "§cdisabled."));
        }
    }
}

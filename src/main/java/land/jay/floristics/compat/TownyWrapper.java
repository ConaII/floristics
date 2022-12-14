/** Copyright (C) 2019 Jay Avery */
package land.jay.floristics.compat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.KeyAlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import land.jay.floristics.Floristics;

import java.util.Objects;

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
        boolean validChange = args.length == 2 && (args[1].equals("enable") || args[1].equals("disable"));
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to use this command.");
            return;
        }

        if (!validInfo && !validChange) {
            player.sendMessage("""
                    Unknown command or invalid arguments. Valid uses:
                    /floristics towny - display whether growth is enabled in this Town
                    /floristics towny <enable|disable> - enable or disable growth in this Town
                    Or replace /floristics with /flo for convenience.""");
            return;
        }
        
        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
        if (resident == null) {
            player.sendMessage(Component.text("Error: could not find your resident data!", NamedTextColor.RED));
            return;
        }

        Town town = TownyAPI.getInstance().getResidentTownOrNull(resident);
        if (!resident.isMayor() || town == null) {
            player.sendMessage(Component.text("You are not a town mayor.", NamedTextColor.RED));
        }

        if (!Objects.requireNonNull(town).getMetadata().contains(FIELD)) {
            town.addMetaData(FIELD);
        }

        BooleanDataField field = null;
        for (CustomDataField<?> metadata : town.getMetadata()) {
            if (metadata.equals(FIELD)) {
                field = (BooleanDataField) metadata;
            }
        }

        if (field == null) {
            player.sendMessage("You're not inside a Town.");

        } else if (validChange) {
            if (args[1].equals("enable")) {
                field.setValue(true);
                player.sendMessage("Growth enabled in this Town.");
            } else if (args[1].equals("disable")) {
                field.setValue(false);
                player.sendMessage("Growth disabled in this Town.");
            }
            
        } else {
            player.sendMessage("Growth in this Town is currently " + (field.getValue() ? "enabled." : "disabled."));
        }
    }
}

package fr.maxlego08.menu.hooks;

import fr.maxlego08.menu.api.annotations.AutoMaterialLoader;
import fr.maxlego08.menu.api.annotations.RequiresPlugin;
import fr.maxlego08.menu.api.loader.MaterialLoader;
import fr.maxlego08.menu.zcore.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * {@code material: "wpme:skyblock:key/umber_key"} — builds the item through WpmeCore's
 * {@code CustomItemService} (registered in Bukkit's ServicesManager by the CustomItems addon),
 * so the stack carries the real item-model, PDC identity and lore. Resolved reflectively:
 * zMenu must not compile against the private WpmeCore API.
 */
@AutoMaterialLoader
@RequiresPlugin("CustomItems")
public class WpmeCoreLoader extends MaterialLoader {

    private static final String SERVICE = "org.rafalohaki.wpmecore.api.item.CustomItemService";
    private Method create;

    public WpmeCoreLoader() {
        super("wpme");
    }

    @Override
    public @Nullable ItemStack load(@NotNull Player player, @Nullable YamlConfiguration configuration, @NotNull String path, @NotNull String materialString) {
        try {
            for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
                if (!SERVICE.equals(service.getName())) continue;
                Object provider = Bukkit.getServicesManager().load(service);
                if (provider == null) return null;
                if (create == null) create = service.getMethod("create", String.class);
                Optional<?> stack = (Optional<?>) create.invoke(provider, materialString);
                return stack.map(ItemStack.class::cast).map(ItemStack::clone).orElse(null);
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            Logger.info("WpmeCoreLoader: cannot build '" + materialString + "': " + e);
        }
        return null;
    }
}

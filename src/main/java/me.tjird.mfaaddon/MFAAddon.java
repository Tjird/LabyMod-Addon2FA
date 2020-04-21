package me.tjird.mfaaddon;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import me.tjird.mfaaddon.listeners.ReceiveMessageListener;
import me.tjird.mfaaddon.listeners.SendMessageListener;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.HeaderElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import java.util.List;

public class MFAAddon extends LabyModAddon {

    @Getter
    private static MFAAddon instance;

    @Getter
    @Setter
    private static JsonObject servers;

    private boolean enabled;

    @Override
    public void onEnable() {
        System.out.println("Enabled MFAAddon");

        instance = this;

        this.getApi().getEventManager().register(new ReceiveMessageListener());
        this.getApi().getEventManager().register(new SendMessageListener());
    }

    @Override
    public void loadConfig() {
        enabled = this.getConfig().has("enabled") ? this.getConfig().get("enabled").getAsBoolean() : true;

        JsonElement serversElement = this.getConfig().get("servers");

        if (!this.getConfig().has("servers")) {
            servers = new JsonObject();
        } else {
            servers = new JsonParser().parse(String.valueOf(serversElement)).getAsJsonObject();
        }

        reloadConfig();
    }

    public void reloadConfig() {
        this.getConfig().add("servers", new Gson().toJsonTree(servers));
        saveConfig();
    }

    protected void fillSettings(final List<SettingsElement> settings) {
        settings.add(new HeaderElement("General"));
        settings.add(new BooleanElement("Enabled", new ControlElement.IconData(Material.LEVER), value -> {
            this.getConfig().addProperty("enabled", value);
            enabled = value;
            saveConfig();
        }, enabled));
    }

    @Override
    public void onDisable() {

    }

    public static void setServer(String server, String key) {
        servers.addProperty(server, key);
    }

    public static void removeServer(String server) {
        servers.remove(server);
    }

}


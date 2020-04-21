package me.tjird.mfaaddon;

import net.labymod.api.LabyModAddon;
import net.labymod.settings.SettingsCategory;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;

import java.util.List;

public class Settings {
    private boolean enabled;
    private String mfatoken;

    static List<SettingsElement> subSettings;

    Settings(List<SettingsElement> subSettings){
        Settings.subSettings = subSettings;

        loadConfig();
        createSettings();
    }

    private final String ENABLED = "Enabled";
    private final String MFA_TOKEN = "MFA Token";
    private final String SERVERS_LIST = "Servers";

    private void loadConfig(){
        this.enabled = MFAAddon.getInstance().getConfig().has(ENABLED) && MFAAddon.getInstance().getConfig().get(ENABLED).getAsBoolean();
        this.mfatoken = MFAAddon.getInstance().getConfig().has(MFA_TOKEN) ? MFAAddon.getInstance().getConfig().get(MFA_TOKEN).getAsString() : "";
    }

    private void createSettings(){
        subSettings.add(new HeaderElement("General"));
        subSettings.add(new BooleanElement("Enabled", new ControlElement.IconData(Material.LEVER), aBoolean -> {
            enabled = aBoolean;
            saveConfig();
        }, enabled));
    }

    private void saveConfig(){
        MFAAddon.getInstance().getConfig().addProperty(ENABLED, this.enabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getMFAToken() {
        return mfatoken;
    }

    public static void setNewServer(String s) {
        ListContainerElement listExtras = new ListContainerElement(s, new ControlElement.IconData(Material.DIODE));
        listExtras.getSubSettings().add(new BooleanElement("Enabled", new ControlElement.IconData(Material.LEVER)));

        subSettings.add(listExtras);
    }

    public static List<SettingsElement> checkServer(String s) {
        return subSettings;
    }
}

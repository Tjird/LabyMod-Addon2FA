package me.tjird.mfaaddon.listeners;

import lombok.SneakyThrows;
import me.tjird.mfaaddon.MFAAddon;
import me.tjird.mfaaddon.TOTP;
import net.labymod.api.events.MessageReceiveEvent;
import net.minecraft.client.Minecraft;

import java.util.Objects;

public class ReceiveMessageListener implements MessageReceiveEvent {

    @SneakyThrows
    @Override
    public boolean onReceive(String s, String s1) {
        if (!s1.equalsIgnoreCase("Je moet inloggen met je Authenticator code /2fa <code>")) return false;
        if (!MFAAddon.getInstance().getConfig().get("enabled").getAsBoolean()) return false;
        if (!MFAAddon.getServers().has(Objects.requireNonNull(Minecraft.getMinecraft().getCurrentServerData()).serverIP)) return false;

        String SecretToken = MFAAddon.getServers().get(Objects.requireNonNull(Minecraft.getMinecraft().getCurrentServerData()).serverIP).getAsString();

        if (SecretToken == null || SecretToken == "") return false;

        try {
            Minecraft.getMinecraft().player.sendChatMessage("/2fa " + TOTP.getTOTPCode(SecretToken));
        } catch (Error e) {
            System.out.println(e);
        }

        return true;
    }

}

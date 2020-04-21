package me.tjird.mfaaddon.listeners;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.tjird.mfaaddon.MFAAddon;
import net.labymod.api.LabyModAPI;
import net.labymod.api.events.MessageSendEvent;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SendMessageListener implements MessageSendEvent {
    @Override
    public boolean onSend(String s) {
        String[] splittedMessage = s.split(" ");

        if (!splittedMessage[0].equalsIgnoreCase("/a2fa")) return false;

        LabyModAPI api = MFAAddon.getInstance().getApi();
        String prefix = ModColor.WHITE + "[" + ModColor.RED + "Addon-2FA" + ModColor.WHITE + "] ";

        if (splittedMessage.length < 2) {
            api.displayMessageInChat(prefix + "Typ \"/a2fa help\" om een help lijst te verkrijgen.");
            return true;
        }

        if (splittedMessage[1].equalsIgnoreCase("remove")) {
            if (splittedMessage.length < 3) {
                api.displayMessageInChat(prefix + "Dit command vraagt 1 argument. Het server ip is hiervoor nodig.");
                return true;
            }

            if (!MFAAddon.getServers().has(splittedMessage[2])) {
                api.displayMessageInChat(prefix + "De opgegeven server is niet gevonden. Een lijst met servers en de 2fa keys kan je verkrijgen via \"/a2fa list\"");
                return true;
            }

            String serverip = splittedMessage[2];
            MFAAddon.removeServer(serverip);
            MFAAddon.getInstance().getConfig().get("servers").getAsJsonObject().remove(serverip);
            MFAAddon.getInstance().saveConfig();
            api.displayMessageInChat(prefix + "De 2fa keys die aan de opgegeven server is verbonden is verwijderd uit de lijst.");
        } else if (splittedMessage[1].equalsIgnoreCase("list")) {
            JsonObject json = new JsonParser().parse(String.valueOf(MFAAddon.getInstance().getConfig().get("servers"))).getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = json.entrySet();

            if (entries.size() < 1) {
                api.displayMessageInChat(prefix + "Je server lijst is momenteel leeg, deze kan je aanvullen via het \"/a2fa add <2fa_key> [server_ip]\" command.");
                return true;
            }

            String message = "";

            for (Map.Entry<String, JsonElement> entry: entries) {
                message += prefix + entry.getKey() + " - " + entry.getValue() + "\n";
            }

            api.displayMessageInChat(prefix + "Hieronder zul je een lijst verkrijgen met alle servers en de bijbehorende 2fa keys.\n" + message);
        } else if (splittedMessage[1].equalsIgnoreCase("add")) {
            if (splittedMessage.length < 3) {
                api.displayMessageInChat(prefix + "Dit command heeft één nodig argument, dat is je 2fa key. Hierbij kan je ook nog een andere argument meesturen, dit is het server ip. Als je niks invult word automatisch het huidige server ip gepakt.");
                return true;
            }

            String serverip = "";
            String secret_key = splittedMessage[2];

            if (splittedMessage.length == 3) {
                serverip = Objects.requireNonNull(Minecraft.getMinecraft().getCurrentServerData()).serverIP;
            } else {
                serverip = splittedMessage[3];
            }

            if (MFAAddon.getServers().has(serverip)) {
                api.displayMessageInChat(prefix + "Je kan geen twee keys toevoegen aan dezelfde server. Graag eerst dit server ip verwijderen uit deze lijst, hierna kan jij deze weer toevoegen.");
            } else {
                MFAAddon.setServer(serverip, secret_key);
                MFAAddon.getInstance().getConfig().get("servers").getAsJsonObject().addProperty(serverip, secret_key);
                MFAAddon.getInstance().saveConfig();
                api.displayMessageInChat(prefix + "Je actie is succesvol uitgevoerd. Als de addon is enabled via het instellingen menu word je automatisch ingelogd. P.S. Als jij de verkeerde key heb meegegeven werkt dit natuurlijk niet ;P.");
            }
        }else if (splittedMessage[1].equalsIgnoreCase("help")) {
            api.displayMessageInChat(prefix + "Hier een lijst met alle commands die deze addon aanbied.\n"
                    + prefix + "add <2fa_key> [server_ip] - Hiermee kan je een 2fa key toevoegen.\n"
                    + prefix + "list - Verkrijg een lijst met alle server ip's met de bijbehorende 2fa key.\n"
                    + prefix + "remove <server_ip> - Verwijder een server ip uit jou lijst, hiermee verwijder je ook de 2fa key die aan het server ip is gekoppeld.");
        } else {
            api.displayMessageInChat(prefix + "Dit command is niet gevonden binnen deze addon. Gebruik \"/a2fa help\".");
        }

        return true;
    }
}

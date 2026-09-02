package io.github.akumetan.playerbridge.profile.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class PlayerDataSerializer {

    private PlayerDataSerializer() {
    }

    public static String serialize(PlayerData data) {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", data.uuid().toString());
        json.addProperty("inventory", Base64.getEncoder().encodeToString(data.inventory()));
        json.addProperty("enderChest", Base64.getEncoder().encodeToString(data.enderChest()));
        json.addProperty("health", data.health());
        json.addProperty("absorption", data.absorption());
        json.addProperty("foodLevel", data.foodLevel());
        json.addProperty("saturation", data.saturation());
        json.addProperty("experience", data.experience());
        json.addProperty("fireTicks", data.fireTicks());
        json.addProperty("gameMode", data.gameMode().name());
        json.add("activeEffects", serializeEffects(data.activeEffects()));
        return json.toString();
    }


    public static PlayerData deserialize(String rawJson) {
        JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();

        return new PlayerData(
                UUID.fromString(json.get("uuid").getAsString()),
                Base64.getDecoder().decode(json.get("inventory").getAsString()),
                Base64.getDecoder().decode(json.get("enderChest").getAsString()),
                json.get("health").getAsDouble(),
                json.get("absorption").getAsDouble(),
                json.get("foodLevel").getAsInt(),
                json.get("saturation").getAsFloat(),
                json.get("experience").getAsInt(),
                json.get("fireTicks").getAsInt(),
                GameMode.valueOf(json.get("gameMode").getAsString()),
                deserializeEffects(json.getAsJsonArray("activeEffects")));
    }

    private static JsonArray serializeEffects(Collection<PotionEffect> effects) {
        JsonArray array = new JsonArray();
        for (PotionEffect effect : effects) {
            JsonObject json = new JsonObject();
            json.addProperty("type", effect.getType().getKey().toString());
            json.addProperty("duration", effect.getDuration());
            json.addProperty("amplifier", effect.getAmplifier());
            json.addProperty("ambient", effect.isAmbient());
            json.addProperty("particles", effect.hasParticles());
            json.addProperty("icon", effect.hasIcon());
            array.add(json);
        }
        return array;
    }

    private static List<PotionEffect> deserializeEffects(JsonArray effects) {
        List<PotionEffect> list = new ArrayList<>();

        for (JsonElement effect : effects) {
            JsonObject object = effect.getAsJsonObject();
            NamespacedKey key = NamespacedKey.fromString(object.get("type").getAsString());
            if (key == null) continue;

            PotionEffectType type = Registry.EFFECT.get(key);
            if (type == null) continue;

            list.add(new PotionEffect(
                    type,
                    object.get("duration").getAsInt(),
                    object.get("amplifier").getAsInt(),
                    object.get("ambient").getAsBoolean(),
                    object.get("particles").getAsBoolean(),
                    object.get("icon").getAsBoolean()));
        }
        return list;
    }

}
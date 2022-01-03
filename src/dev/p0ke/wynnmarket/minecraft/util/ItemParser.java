package dev.p0ke.wynnmarket.minecraft.util;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;

import java.util.ArrayList;
import java.util.List;

public class ItemParser {

    public static String getName(ItemStack item) {
        if (item.getNbt() == null) return "";
        if (!item.getNbt().contains("display")) return "";
        CompoundTag displayTag = item.getNbt().get("display");

        if (!displayTag.contains("Name")) return "";
        StringTag name = displayTag.get("Name");

        return StringUtil.parseStringTag(name);
    }

    public static List<String> getLore(ItemStack item) {
        List<String> lore = new ArrayList<>();

        if (item.getNbt() == null) return lore;
        if (!item.getNbt().contains("display")) return lore;
        CompoundTag displayTag = item.getNbt().get("display");

        if (!displayTag.contains("Lore")) return lore;
        ListTag loreTag = displayTag.get("Lore");

        for (int i = 0; i < loreTag.size(); i++) {
            StringTag lineTag = loreTag.get(i);

            lore.add(StringUtil.parseStringTag(lineTag));
        }

        return lore;
    }

}

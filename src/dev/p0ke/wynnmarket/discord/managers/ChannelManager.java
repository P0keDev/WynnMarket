package dev.p0ke.wynnmarket.discord.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.instances.ItemFilter;
import dev.p0ke.wynnmarket.discord.instances.RegisteredChannel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChannelManager {

    private static final Type listType = new TypeToken<List<RegisteredChannel>>() {
    }.getType();

    private static List<RegisteredChannel> channelList = new ArrayList<>();
    private static File channelFile;

    private static List<String> followerChannels;
    private static List<String> logChannels;
    private static List<String> infoChannels;

    private static Gson gson;

    public static void setupList() {
        try {
            gson = new GsonBuilder().setPrettyPrinting().create();

            channelFile = new File("./channels.json");
            if (channelFile.exists()) {
                FileReader reader = new FileReader(channelFile);
                channelList = gson.fromJson(reader, listType);
                reader.close();
            } else {
                channelFile.createNewFile();
                saveList();
            }
            resetChannelCache();
        } catch (Exception e) {
            System.out.println("Error setting up channels file: ");
            e.printStackTrace();
        }
    }

    public static void addChannel(String id, boolean log, boolean info) {
        if (isRegistered(id)) return;
        channelList.add(new RegisteredChannel(id, log, info));
        saveList();
        resetChannelCache();
    }

    public static void removeChannel(String id) {
        if (!isRegistered(id)) return;
        channelList.remove(getChannel(id));
        saveList();
        resetChannelCache();
    }

    public static boolean addItem(String channelId, String userId, String item) {
        if (!isRegistered(channelId)) return false;
        boolean success = getChannel(channelId).addItem(userId, item);
        if (success) saveList();
        return success;
    }

    public static boolean removeItem(String channelId, String userId, String item) {
        if (!isRegistered(channelId)) return false;
        boolean success = getChannel(channelId).removeItem(userId, item);
        if (success) saveList();
        return success;
    }

    public static boolean blockItem(String channelId, String userId, MarketItem item) {
        if (!isRegistered(channelId)) return false;
        boolean success = getChannel(channelId).blockItem(userId, item);
        if (success) saveList();
        return success;
    }

    public static boolean addItemFilter(String channelId, String userId, String item, ItemFilter filter) {
        if (!isRegistered(channelId)) return false;
        boolean success = getChannel(channelId).addItemFilter(userId, item, filter);
        if (success) saveList();
        return success;
    }

    public static boolean clearItemFilters(String channelId, String userId, String item) {
        if (!isRegistered(channelId)) return false;
        boolean success = getChannel(channelId).clearItemFilters(userId, item);
        if (success) saveList();
        return success;
    }

    public static List<String> getUsersForItem(String channelId, MarketItem item) {
        if (!isRegistered(channelId)) return null;
        return getChannel(channelId).getUsersForItem(item);
    }

    public static List<String> getItemsForUser(String channelId, String userId) {
        if (!isRegistered(channelId)) return null;
        return getChannel(channelId).getItemsForUser(userId);
    }

    public static RegisteredChannel getChannel(String id) {
        for (RegisteredChannel c : channelList) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    public static boolean isRegistered(String id) {
        for (RegisteredChannel c : channelList) {
            if (c.getId().equals(id)) return true;
        }
        return false;
    }

    public static boolean isFollowerChannel(String id) {
        for (RegisteredChannel c : channelList) {
            if (c.getId().equals(id) && !c.isLog()) return true;
        }
        return false;
    }

    private static void resetChannelCache() {
        followerChannels = null;
        logChannels = null;
        infoChannels = null;
    }

    public static List<String> getFollowerChannels() {
        if (followerChannels != null) return followerChannels;

        followerChannels = new ArrayList<>();
        for (RegisteredChannel c : channelList) {
            if (!c.isLog() && !c.isInfo()) followerChannels.add(c.getId());
        }
        return followerChannels;
    }

    public static List<String> getLogChannels() {
        if (logChannels != null) return logChannels;

        logChannels = new ArrayList<>();
        for (RegisteredChannel c : channelList) {
            if (c.isLog()) logChannels.add(c.getId());
        }
        return logChannels;
    }

    public static List<String> getInfoChannels() {
        if (infoChannels != null) return infoChannels;

        infoChannels = new ArrayList<>();
        for (RegisteredChannel c : channelList) {
            if (c.isInfo()) infoChannels.add(c.getId());
        }
        return infoChannels;
    }

    public static void saveList() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(channelFile));
            writer.write(gson.toJson(channelList));
            writer.close();
        } catch (Exception e) {
            System.out.println("Error writing to channels file: ");
            e.printStackTrace();
        }
    }

}

package dev.p0ke.wynnmarket.data.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.p0ke.wynnmarket.data.instances.MarketEntry;
import dev.p0ke.wynnmarket.data.instances.MarketItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MarketLogManager {

    private static final int SAVE_INTERVAL = 5; // time, in minutes, between file writes

    private static final Type entryType = new TypeToken<List<MarketEntry>>() {
    }.getType();

    private static Gson gson = new Gson();

    private static File logFile;
    private static LocalDateTime currentDate;

    private static List<MarketEntry> entries;

    public static void start() {
        currentDate = LocalDateTime.now();
        createLog();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(MarketLogManager::saveLog, SAVE_INTERVAL, SAVE_INTERVAL, TimeUnit.MINUTES);
    }

    public static void logItem(MarketItem item) {
        entries.add(new MarketEntry(item));
    }

    private static void createLog() {
        try {
            logFile = new File("./marketdb/" + currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".json");
            logFile.getParentFile().mkdir(); // create marketdb if it doesn't exist

            if (!logFile.exists()) { // create file
                logFile.createNewFile();
                entries = new ArrayList<>();
                saveLog();
            } else { // read entries
                FileReader logReader = new FileReader(logFile);
                entries = gson.fromJson(logReader, entryType);
                logReader.close();
                loadLatestEntries();
            }
        } catch (Exception e) {
            System.out.println("Failed to create market log: ");
            e.printStackTrace();
        }
    }

    private static void saveLog() {
        try {
            BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile));
            logWriter.write(gson.toJson(new ArrayList<>(entries), entryType));
            logWriter.close();

            checkDate();
        } catch (Exception e) {
            System.out.println("Failed to save market log: ");
            e.printStackTrace();
        }
    }

    private static void loadLatestEntries() {
        if (entries.size() <= MarketItemManager.QUEUE_SIZE) {
            for (MarketEntry me : entries) {
                MarketItemManager.addItem(me.getItem());
            }
        } else {
            for (int i = entries.size() - MarketItemManager.QUEUE_SIZE; i < entries.size(); i++) {
                MarketItemManager.addItem(entries.get(i).getItem());
            }
        }
    }

    private static void checkDate() {
        if (LocalDateTime.now().getDayOfYear() != currentDate.getDayOfYear()) {
            currentDate = LocalDateTime.now();
            entries.clear();
            createLog();
        }
    }

}

package dev.p0ke.wynnmarket.discord.commands;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.managers.ChannelManager;
import dev.p0ke.wynnmarket.discord.util.ItemUtil;
import dev.p0ke.wynnmarket.minecraft.MinecraftManager;
import dev.p0ke.wynnmarket.minecraft.enums.RarityFilter;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchCommand implements Command {

    private static final String LEFT_ARROW = "\u2B05";
    private static final String RIGHT_ARROW = "\u27A1";
    private static final String RED_X = "\u274C";

    @Override
    public List<String> getNames() {
        return Arrays.asList("search");
    }

    @Override
    public void execute(MessageCreateEvent event, String[] args) {
        if (!ChannelManager.isFollowerChannel(event.getChannel().getIdAsString())) {
            event.getChannel().sendMessage("This channel is not registered!");
            return;
        }

        if (args.length <= 1) {
            event.getMessage().reply("Must specify an item and/or rarity!");
            return;
        }

        // small indicator
        event.getChannel().type();

        String input = "";
        for (int i = 1; i < args.length; i++) {
            input += args[i] + " ";
        }
        input = input.trim().replace("(", "[").replace(")", "]");

        RarityFilter rarity = null;
        if (input.contains("[") && input.contains("]")) {
            String rarityString = StringUtils.substringBefore(StringUtils.substringAfter(input, "["), "]");
            rarity = RarityFilter.parseString(rarityString);

            if (rarity == null) {
                event.getMessage().reply(rarityString + " is not a valid rarity!");
                return;
            }
        }

        String item = input.replaceAll("\\[.*\\]", "").trim();
        if (item.isEmpty()) item = null; // rarity-only search

        List<MarketItem> results = MinecraftManager.searchItem(item, rarity);
        if (results == null) {
            event.getMessage().reply("Search failed!");
            return;
        }
        if (results.isEmpty()) {
            event.getMessage().reply("No results for: " + item);
            return;
        }

        event.getChannel().sendMessage("1/" + results.size() + ":\n" + ItemUtil.getItemString(results.get(0)))
                .thenAccept(message -> {
                    message.addReaction(LEFT_ARROW).join();
                    message.addReaction(RIGHT_ARROW).join();
                    message.addReaction(RED_X);

                    message.addReactionAddListener(new SearchReactionListener(results, event.getMessageAuthor().getIdAsString())).removeAfter(5, TimeUnit.MINUTES);
                });
    }

    private class SearchReactionListener implements ReactionAddListener {

        private List<MarketItem> results;
        private String userId;
        private int index;

        public SearchReactionListener(List<MarketItem> r, String u) {
            results = r;
            userId = u;
            index = 0;
        }

        @Override
        public void onReactionAdd(ReactionAddEvent event) {
            User user = event.getUser().get();
            Message message = event.getMessage().get();
            if (!user.getIdAsString().equals(userId)) return;

            switch (event.getEmoji().asUnicodeEmoji().orElse("")) {
                case LEFT_ARROW:
                    index = Math.floorMod(index - 1, results.size());
                    break;
                case RIGHT_ARROW:
                    index = Math.floorMod(index + 1, results.size());
                    break;
                case RED_X:
                    message.removeAllReactions();
                    message.removeMessageAttachableListener(this);
                    return;
            }

            message.edit((index + 1) + "/" + results.size() + ":\n" + ItemUtil.getItemString(results.get(index))).join();
            event.getReaction().get().removeUser(user);
        }

    }

}

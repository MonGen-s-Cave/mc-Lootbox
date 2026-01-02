package com.mongenscave.mclootbox.suggestions;

import com.mongenscave.mclootbox.McLootbox;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;

import java.util.Collection;

public class LootboxesSuggestionProvider<A extends CommandActor> implements SuggestionProvider<A> {
    @Override
    public @NotNull Collection<String> getSuggestions(@NotNull ExecutionContext<A> context) {
        return McLootbox.getInstance()
                .getLootboxManager()
                .getLootboxIds();
    }
}
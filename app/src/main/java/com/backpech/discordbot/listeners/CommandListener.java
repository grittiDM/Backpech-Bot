package com.backpech.discordbot.listeners;

import org.jetbrains.annotations.NotNull;

import com.backpech.discordbot.commands.CommandManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
  private final CommandManager commandManager;

  public CommandListener(CommandManager commandManager) {
    this.commandManager = commandManager;
  }

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    commandManager.handle(event);
  }
}
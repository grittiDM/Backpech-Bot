package com.backpech.discordbot.commands.impl;

import com.backpech.discordbot.commands.ICommand;
import com.backpech.discordbot.services.GuildConfigService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Objects;

public class ConfigWelcomeRoleCommand implements ICommand {

  private final GuildConfigService configService;

  public ConfigWelcomeRoleCommand(GuildConfigService configService) {
    this.configService = configService;
  }

  @Override
  public String getName() {
    return "config-welcome-role";
  }

  @Override
  public String getDescription() {
    return "Define o cargo que será adicionado automaticamente a novos membros.";
  }

  @Override
  public CommandData getCommandData() {
    return Commands.slash(getName(), getDescription())
        .addOption(OptionType.ROLE, "cargo", "O cargo a ser atribuído aos novos membros.", true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    if (event.getGuild() == null) {
      event.reply("Este comando só pode ser usado em um servidor.").setEphemeral(true).queue();
      return;
    }

    Role roleToSet = Objects.requireNonNull(event.getOption("cargo")).getAsRole();
    String guildId = event.getGuild().getId();

    // Verifica se o bot pode interagir com o cargo (hierarquia)
    if (!event.getGuild().getSelfMember().canInteract(roleToSet)) {
      event.reply("Não posso atribuir o cargo '" + roleToSet.getName()
          + "' porque ele está em uma posição hierárquica superior à minha.").setEphemeral(true).queue();
      return;
    }

    configService.setWelcomeRole(guildId, roleToSet.getId());

    event.reply("Pronto! O cargo **" + roleToSet.getName() + "** será agora atribuído a todos os novos membros.")
        .setEphemeral(true).queue();
  }
}
package com.backpech.discordbot.commands.impl;

import com.backpech.discordbot.commands.ICommand;
import com.backpech.discordbot.config.BotConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MuteCommand implements ICommand {

  private static final Logger logger = LoggerFactory.getLogger(MuteCommand.class);
  private final BotConfig config;

  public MuteCommand(BotConfig config) {
    this.config = config;
  }

  @Override
  public String getName() {
    return "mute";
  }

  @Override
  public String getDescription() {
    return "Muta (castigo) um usuário por um tempo determinado.";
  }

  @Override
  public CommandData getCommandData() {
    return Commands.slash(getName(), getDescription())
        .addOption(OptionType.USER, "usuario", "O usuário a ser mutado.", true)
        .addOption(OptionType.INTEGER, "duracao", "Duração do mute em minutos.", true)
        .addOption(OptionType.STRING, "motivo", "O motivo do mute.", false)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    Member author = event.getMember();
    logger.debug("Comando /mute invocado por '{}' no servidor '{}'.", author.getEffectiveName(),
        event.getGuild().getName());

    // Validação de permissão por cargo
    List<String> moderatorRoles = config.moderatorRoles();
    boolean hasModRole = author.getRoles().stream().anyMatch(role -> moderatorRoles.contains(role.getId()));

    if (!author.hasPermission(Permission.MODERATE_MEMBERS) || !hasModRole) {
      logger.warn("Usuário '{}' tentou usar /mute sem permissão. Permissão Discord: {}, Cargo Mod: {}",
          author.getEffectiveName(), author.hasPermission(Permission.MODERATE_MEMBERS), hasModRole);
      event.reply("Você não tem permissão para usar este comando.").setEphemeral(true).queue();
      return;
    }

    Member targetMember = Objects.requireNonNull(event.getOption("usuario")).getAsMember();
    long duration = Objects.requireNonNull(event.getOption("duracao")).getAsLong();
    String reason = event.getOption("motivo", "Motivo não especificado.", OptionMapping::getAsString);

    // --- Validações de Segurança ---
    if (targetMember == null) {
      event.reply("Usuário não encontrado neste servidor. Ele precisa ser um membro para ser mutado.")
          .setEphemeral(true).queue();
      return;
    }
    if (!author.canInteract(targetMember)) {
      logger.warn("Moderador '{}' tentou mutar '{}', que tem cargo superior.", author.getEffectiveName(),
          targetMember.getEffectiveName());
      event.reply("Você não pode mutar este usuário. A hierarquia de cargos dele é maior ou igual à sua.")
          .setEphemeral(true).queue();
      return;
    }
    if (targetMember.isOwner()) {
      logger.warn("Moderador '{}' tentou mutar o dono do servidor.", author.getEffectiveName());
      event.reply("O dono do servidor não pode ser mutado.").setEphemeral(true).queue();
      return;
    }

    // --- Execução da Ação ---
    targetMember.timeoutFor(duration, TimeUnit.MINUTES).reason(reason).queue(
        success -> {
          logger.info("Usuário '{}' foi mutado por '{}' por {} minutos. Motivo: {}",
              targetMember.getEffectiveName(), author.getEffectiveName(), duration, reason);
          event.reply(String.format("**%s** foi mutado por %d minutos. Motivo: %s", targetMember.getEffectiveName(),
              duration, reason)).queue();
        },
        error -> {
          logger.error("Falha ao mutar '{}'. Causa: {}", targetMember.getEffectiveName(), error.getMessage(), error);
          event.reply("Falha ao mutar o usuário. Verifique minhas permissões e a hierarquia de cargos.")
              .setEphemeral(true).queue();
        });
  }
}
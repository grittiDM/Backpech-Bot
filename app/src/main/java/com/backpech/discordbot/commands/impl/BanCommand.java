package com.backpech.discordbot.commands.impl;

import com.backpech.discordbot.commands.ICommand;
import com.backpech.discordbot.config.BotConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
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

public class BanCommand implements ICommand {

  private static final Logger logger = LoggerFactory.getLogger(BanCommand.class);
  private final BotConfig config;

  public BanCommand(BotConfig config) {
    this.config = config;
  }

  @Override
  public String getName() {
    return "ban";
  }

  @Override
  public String getDescription() {
    return "Bane um usuário do servidor permanentemente.";
  }

  @Override
  public CommandData getCommandData() {
    return Commands.slash(getName(), getDescription())
        .addOption(OptionType.USER, "usuario", "O usuário a ser banido.", true)
        .addOption(OptionType.STRING, "motivo", "O motivo do banimento.", true)
        .addOption(OptionType.INTEGER, "dias_mensagens", "Dias de mensagens do usuário a serem apagadas (0-7).", false)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    Member author = event.getMember();
    logger.debug("Comando /ban invocado por '{}' no servidor '{}'.", author.getEffectiveName(),
        event.getGuild().getName());

    // Validação de permissão por cargo (além da permissão do Discord)
    List<String> moderatorRoles = config.moderatorRoles();
    boolean hasModRole = author.getRoles().stream().anyMatch(role -> moderatorRoles.contains(role.getId()));

    if (!author.hasPermission(Permission.BAN_MEMBERS) || !hasModRole) {
      logger.warn("Usuário '{}' tentou usar /ban sem permissão. Permissão Discord: {}, Cargo Mod: {}",
          author.getEffectiveName(), author.hasPermission(Permission.BAN_MEMBERS), hasModRole);
      event.reply("Você não tem permissão para usar este comando.").setEphemeral(true).queue();
      return;
    }

    User targetUser = Objects.requireNonNull(event.getOption("usuario")).getAsUser();
    Member targetMember = event.getOption("usuario").getAsMember(); // Pode ser nulo se o usuário não estiver no
                                                                    // servidor
    String reason = Objects.requireNonNull(event.getOption("motivo")).getAsString();
    int messageDeletionDays = event.getOption("dias_mensagens", 0, OptionMapping::getAsInt);
    if (messageDeletionDays < 0 || messageDeletionDays > 7) {
      event.reply("O número de dias para apagar mensagens deve ser entre 0 e 7.").setEphemeral(true).queue();
      return;
    }

    // --- Validações de Segurança ---
    if (targetUser.isBot() && targetUser.equals(event.getJDA().getSelfUser())) {
      logger.warn("Usuário '{}' tentou me banir.", author.getEffectiveName());
      event.reply("Operação impossível. Eu não posso me banir.").setEphemeral(true).queue();
      return;
    }

    if (targetUser.equals(author.getUser())) {
      logger.warn("Usuário '{}' tentou banir a si mesmo.", author.getEffectiveName());
      event.reply("Você não pode banir a si mesmo.").setEphemeral(true).queue();
      return;
    }

    if (targetMember != null && !author.canInteract(targetMember)) {
      logger.warn("Moderador '{}' tentou banir '{}', que tem cargo superior.", author.getEffectiveName(),
          targetMember.getEffectiveName());
      event.reply("Você não pode banir este usuário. A hierarquia de cargos dele é maior ou igual à sua.")
          .setEphemeral(true).queue();
      return;
    }

    // --- Execução da Ação ---
    event.getGuild().ban(targetUser, messageDeletionDays, TimeUnit.DAYS).reason(reason).queue(
        success -> {
          String logMessage = String.format(
              "Usuário '%s' (ID: %s) foi BANIDO por '%s'. Motivo: %s. Mensagens apagadas: %d dias.",
              targetUser.getAsTag(), targetUser.getId(), author.getEffectiveName(), reason, messageDeletionDays);
          logger.info(logMessage);
          event.reply(String.format("**%s** foi banido com sucesso. Motivo: %s", targetUser.getAsTag(), reason))
              .queue();
        },
        error -> {
          logger.error("Falha ao banir '{}'. Causa: {}", targetUser.getAsTag(), error.getMessage(), error);
          event.reply(
              String.format("Não foi possível banir **%s**. Verifique minhas permissões e a hierarquia de cargos.",
                  targetUser.getAsTag()))
              .setEphemeral(true).queue();
        });
  }
}
package com.backpech.discordbot.listeners;

import com.backpech.discordbot.services.GuildConfigService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class WelcomeListener extends ListenerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(WelcomeListener.class);
  private final GuildConfigService configService;

  public WelcomeListener(GuildConfigService configService) {
    this.configService = configService;
  }

  @Override
  public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
    Guild guild = event.getGuild();
    Member member = event.getMember();

    // 1. Fetch the role ID from the database for this specific guild
    Optional<String> welcomeRoleIdOpt = configService.getWelcomeRoleId(guild.getId());

    if (welcomeRoleIdOpt.isEmpty()) {
      logger.info("Nenhum cargo de boas-vindas configurado para o servidor '{}'. Ignorando.", guild.getName());
      return;
    }

    String welcomeRoleId = welcomeRoleIdOpt.get();
    Role roleToAssign = guild.getRoleById(welcomeRoleId);

    if (roleToAssign == null) {
      logger.error("O cargo de boas-vindas com ID '{}' não foi encontrado no servidor '{}'. Pode ter sido excluído.",
          welcomeRoleId, guild.getName());
      return;
    }

    logger.info("Tentando adicionar o cargo '{}' para o novo membro '{}' no servidor '{}'.", roleToAssign.getName(),
        member.getEffectiveName(), guild.getName());

    guild.addRoleToMember(member, roleToAssign).queue(
        success -> logger.info("Cargo '{}' adicionado com sucesso para '{}'.", roleToAssign.getName(),
            member.getEffectiveName()),
        error -> logger.error("Falha ao adicionar cargo '{}' para '{}'. Causa: {}", roleToAssign.getName(),
            member.getEffectiveName(), error.getMessage()));
  }
}
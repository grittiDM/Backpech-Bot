package com.backpech.discordbot.listeners;

import com.backpech.discordbot.config.BotConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WelcomeListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeListener.class);
    private final BotConfig config;

    /**
     * Constructs the listener, injecting the application's configuration.
     *
     * @param config The BotConfig instance containing necessary role IDs.
     */
    public WelcomeListener(BotConfig config) {
        this.config = config;
    }

    /**
     * This method is called by JDA whenever a new member joins a guild.
     *
     * @param event The event object containing information about the new member and
     *              the guild.
     */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        String newUserRoleId = config.newUserRole();

        // 1. Validate that the role ID is present in the configuration
        if (newUserRoleId == null || newUserRoleId.isBlank()) {
            logger.warn(
                    "O ID do cargo de boas-vindas (role.newUser) não está definido no config.properties para o servidor '{}'.",
                    guild.getName());
            return;
        }

        // 2. Find the role in the server using the ID
        Role roleToAssign = guild.getRoleById(newUserRoleId);

        if (roleToAssign == null) {
            logger.error(
                    "O cargo de boas-vindas com ID '{}' não foi encontrado no servidor '{}'. Verifique o ID e as permissões do bot.",
                    newUserRoleId, guild.getName());
            return;
        }

        // 3. Assign the role to the new member
        logger.info("Tentando adicionar o cargo '{}' para o novo membro '{}' no servidor '{}'.", roleToAssign.getName(),
                member.getEffectiveName(), guild.getName());

        guild.addRoleToMember(member, roleToAssign).queue(
                // Success Callback
                success -> logger.info("Cargo '{}' adicionado com sucesso para '{}'.", roleToAssign.getName(),
                        member.getEffectiveName()),
                // Failure Callback
                error -> logger.error("Falha ao adicionar cargo '{}' para '{}'. Causa: {}", roleToAssign.getName(),
                        member.getEffectiveName(), error.getMessage()));
    }
}
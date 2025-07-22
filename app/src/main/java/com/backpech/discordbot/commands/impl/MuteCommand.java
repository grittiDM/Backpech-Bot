package com.backpech.discordbot.commands.impl;

import com.backpech.discordbot.commands.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MuteCommand implements ICommand {

    // Nenhuma dependência de configuração necessária
    public MuteCommand() {
    }

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public String getDescription() {
        return "Muta (coloca em castigo) um usuário por um tempo determinado.";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.USER, "usuario", "O usuário a ser mutado.", true)
                .addOption(OptionType.INTEGER, "duracao", "Duração do mute em minutos.", true)
                .addOption(OptionType.STRING, "motivo", "O motivo do mute.", false)
                // O comando só será visível para quem tem a permissão de MODERAR MEMBROS
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("Você não tem a permissão 'Moderar Membros' para usar este comando.").setEphemeral(true)
                    .queue();
            return;
        }

        Member targetMember = Objects.requireNonNull(event.getOption("usuario")).getAsMember();
        long duration = Objects.requireNonNull(event.getOption("duracao")).getAsLong();
        String reason = event.getOption("motivo", "Motivo não especificado.", OptionMapping::getAsString);

        if (targetMember == null) {
            event.reply("Usuário não encontrado no servidor. Ele precisa estar presente para ser mutado.")
                    .setEphemeral(true).queue();
            return;
        }

        if (!member.canInteract(targetMember)) {
            event.reply("Você não pode mutar este usuário. Ele pode ter um cargo superior ao seu.").setEphemeral(true)
                    .queue();
            return;
        }

        if (targetMember.isOwner()) {
            event.reply("Você não pode mutar o dono do servidor.").setEphemeral(true).queue();
            return;
        }

        event.getGuild().timeoutFor(targetMember, duration, TimeUnit.MINUTES).reason(reason).queue(
                success -> event.reply(String.format("**%s** foi mutado por %d minutos. Motivo: %s",
                        targetMember.getEffectiveName(), duration, reason)).queue(),
                error -> event.reply(String.format("Falha ao mutar **%s**. Verifique minhas permissões.",
                        targetMember.getEffectiveName())).setEphemeral(true).queue());
    }
}
package com.backpech.discordbot.commands.impl;

import com.backpech.discordbot.commands.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BanCommand implements ICommand {

    // Não precisamos mais do BotConfig aqui!
    public BanCommand() {
    }

    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public String getDescription() {
        return "Bane um usuário do servidor.";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.USER, "usuario", "O usuário a ser banido.", true)
                .addOption(OptionType.STRING, "motivo", "O motivo do banimento.", false)
                // O comando só será visível para quem tem a permissão de BANIR MEMBROS
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // A verificação de permissão agora é feita pelo próprio Discord antes de
        // mostrar o comando.
        // Podemos adicionar uma dupla verificação por segurança.
        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("Você não tem a permissão 'Banir Membros' para usar este comando.").setEphemeral(true).queue();
            return;
        }

        User targetUser = Objects.requireNonNull(event.getOption("usuario")).getAsUser();
        Member targetMember = event.getOption("usuario").getAsMember(); // Pode ser nulo se o usuário não estiver no
                                                                        // servidor
        String reason = event.getOption("motivo", "Motivo não especificado.", OptionMapping::getAsString);

        if (targetUser.equals(member.getUser())) {
            event.reply("Você não pode banir a si mesmo.").setEphemeral(true).queue();
            return;
        }

        // Verifica se o autor do comando pode interagir com o alvo (hierarquia de
        // cargos)
        if (targetMember != null && !member.canInteract(targetMember)) {
            event.reply("Você não pode banir este usuário. Ele pode ter um cargo superior ao seu.").setEphemeral(true)
                    .queue();
            return;
        }

        // Impede que o bot se bana
        if (targetUser.equals(event.getJDA().getSelfUser())) {
            event.reply("Eu não posso me banir!").setEphemeral(true).queue();
            return;
        }

        event.getGuild().ban(targetUser, 0, TimeUnit.DAYS).reason(reason).queue(
                success -> event.reply(
                        String.format("**%s** foi banido com sucesso. Motivo: %s", targetUser.getAsTag(), reason))
                        .queue(),
                error -> event.reply(
                        String.format("Falha ao banir **%s**. Verifique minhas permissões.", targetUser.getAsTag()))
                        .setEphemeral(true).queue());
    }
}
package main.java.com.backpech.discordbot.commands.impl;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.mycompany.discordbot.commands.ICommand;
import com.mycompany.discordbot.config.BotConfig;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class BanCommand implements ICommand {

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
        return "Bane um usuário do servidor.";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.USER, "usuario", "O usuário a ser banido.", true)
                .addOption(OptionType.STRING, "motivo", "O motivo do banimento.", false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED); // Controlado por cargo
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Lógica de verificação de permissão
        if (!hasModeratorRole(event.getMember())) {
            event.reply("Você não tem permissão para usar este comando.").setEphemeral(true).queue();
            return;
        }

        User targetUser = Objects.requireNonNull(event.getOption("usuario")).getAsUser();
        Member targetMember = event.getOption("usuario").getAsMember();
        String reason = event.getOption("motivo") != null ? event.getOption("motivo").getAsString() : "Motivo não especificado.";

        if (targetUser.equals(event.getUser())) {
            event.reply("Você não pode banir a si mesmo.").setEphemeral(true).queue();
            return;
        }
        if (targetMember != null && (!event.getMember().canInteract(targetMember) || hasModeratorRole(targetMember))) {
            event.reply("Você não pode banir este usuário.").setEphemeral(true).queue();
            return;
        }

        event.getGuild().ban(targetUser, 0, TimeUnit.DAYS).reason(reason).queue(
            success -> event.reply(String.format("%s foi banido com sucesso. Motivo: %s", targetUser.getAsTag(), reason)).queue(),
            error -> event.reply(String.format("Falha ao banir %s.", targetUser.getAsTag())).setEphemeral(true).queue()
        );
    }

    private boolean hasModeratorRole(Member member) {
        if (member == null) return false;
        List<String> moderatorRoleIds = config.moderatorRoleIds();
        return member.getRoles().stream().anyMatch(role -> moderatorRoleIds.contains(role.getId()));
    }
}
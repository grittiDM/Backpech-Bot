package com.backpech.discordbot.commands.impl;

import com.backpech.discordbot.commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;
import java.util.Optional;

/**
 * Handles the /jam command, creating an interactive embed to start a synchronized listening party.
 */
public class JamCommand implements ICommand {

    @Override
    public String getName() {
        return "jam";
    }

    @Override
    public String getDescription() {
        return "Inicia uma JAM no Spotify, permitindo que outros ouçam junto com você.";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            event.reply("Ocorreu um erro ao obter suas informações.").setEphemeral(true).queue();
            return;
        }

        Optional<Activity> spotifyActivity = findSpotifyActivity(member);

        if (spotifyActivity.isPresent() && spotifyActivity.get().asRichPresence().getSyncId() != null) {
            RichPresence presence = spotifyActivity.get().asRichPresence();
            String trackName = presence.getDetails();
            String artist = presence.getState();
            String albumArtUrl = presence.getLargeImage().getUrl();

            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor(String.format("%s iniciou uma JAM!", member.getEffectiveName()), null, member.getEffectiveAvatarUrl())
                    .setColor(new Color(30, 215, 96))
                    .setTitle(trackName)
                    .setDescription(String.format("por **%s**", artist))
                    .setThumbnail(albumArtUrl)
                    .setFooter("Clique no botão abaixo para entrar na sessão e ouvir junto!");

            // Criamos um botão com um ID customizado. O ID contém o nome do comando e o ID do anfitrião.
            Button joinButton = Button.success("jam-join:" + member.getId(), "Entrar na JAM");

            event.replyEmbeds(embed.build()).setActionRow(joinButton).queue();

        } else {
            event.reply("Você não parece estar ouvindo uma música no Spotify que permita convites. Verifique seu status!").setEphemeral(true).queue();
        }
    }

    // Helper para encontrar a atividade, pode ser útil em outros lugares
    public static Optional<Activity> findSpotifyActivity(Member member) {
        if (member == null) return Optional.empty();
        return member.getActivities().stream()
                .filter(activity -> activity.getType() == Activity.ActivityType.LISTENING && activity.isRich())
                .filter(activity -> "spotify:1".equals(activity.asRichPresence().getApplicationId()))
                .findFirst();
    }
}
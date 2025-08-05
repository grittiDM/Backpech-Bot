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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.Optional;

public class JamCommand implements ICommand {

  private static final Logger logger = LoggerFactory.getLogger(JamCommand.class);

  @Override
  public String getName() {
    return "jam";
  }

  @Override
  public String getDescription() {
    return "Inicia uma JAM, compartilhando a música que você está ouvindo no Spotify.";
  }

  @Override
  public CommandData getCommandData() {
    return Commands.slash(getName(), getDescription());
  }

  @Override
  public void execute(SlashCommandInteractionEvent event) {
    Member member = event.getMember();
    if (member == null) {
      logger.error("Comando /jam falhou pois o membro não foi encontrado no evento. User ID: {}",
          event.getUser().getId());
      event.reply("Ocorreu um erro ao obter suas informações de membro.").setEphemeral(true).queue();
      return;
    }

    logger.debug("Comando /jam invocado por '{}' no servidor '{}'.", member.getEffectiveName(),
        event.getGuild().getName());

    Optional<Activity> spotifyActivity = findSpotifyActivity(member);

    if (spotifyActivity.isPresent() && spotifyActivity.get().asRichPresence().getSyncId() != null) {
      RichPresence presence = spotifyActivity.get().asRichPresence();
      String trackName = presence.getDetails();
      String artist = presence.getState();
      String albumArtUrl = presence.getLargeImage().getUrl();
      String trackId = presence.getSyncId();
      String trackUrl = "https://open.spotify.com/track/" + trackId;

      EmbedBuilder embed = new EmbedBuilder()
          .setAuthor(String.format("%s iniciou uma JAM!", member.getEffectiveName()), null,
              member.getEffectiveAvatarUrl())
          .setColor(new Color(30, 215, 96)) // Cor oficial do Spotify
          .setTitle(trackName, trackUrl)
          .setDescription(String.format("por **%s**", artist))
          .setThumbnail(albumArtUrl)
          .setFooter("Clique no botão para ouvir no Spotify!");

      Button joinButton = Button.link(trackUrl, "Ouvir Junto");

      event.replyEmbeds(embed.build()).setActionRow(joinButton).queue(
          success -> logger.info("JAM iniciada por '{}' com a música '{}'.", member.getEffectiveName(), trackName),
          error -> logger.error("Falha ao enviar resposta da JAM para '{}'.", member.getEffectiveName(), error));

    } else {
      logger.info("Usuário '{}' tentou usar /jam mas não estava ouvindo no Spotify.", member.getEffectiveName());
      event
          .reply(
              "Você não parece estar ouvindo uma música no Spotify. Verifique se seu status está visível no Discord!")
          .setEphemeral(true).queue();
    }
  }

  public static Optional<Activity> findSpotifyActivity(Member member) {
    if (member == null)
      return Optional.empty();
    return member.getActivities().stream()
        .filter(activity -> activity.getType() == Activity.ActivityType.LISTENING && activity.isRich())
        .filter(activity -> "spotify".equals(activity.getName())) // Maneira mais confiável de identificar Spotify
        .findFirst();
  }
}
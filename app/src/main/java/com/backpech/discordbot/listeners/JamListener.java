package com.backpech.discordbot.listeners;

import com.backpech.discordbot.commands.impl.JamCommand;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class JamListener extends ListenerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(JamListener.class);

  @Override
  public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
    String[] componentParts = event.getComponentId().split(":");
    if (!componentParts[0].equals("jam-join")) {
      return; // Ignora botões que não são da JAM
    }

    logger.debug("Botão 'jam-join' pressionado por '{}' no servidor '{}'.", event.getUser().getName(),
        event.getGuild().getName());

    // O ID do anfitrião foi armazenado no ID do botão
    String hostId = componentParts[1];
    Member host = event.getGuild().getMemberById(hostId);
    Member guest = event.getMember(); // O convidado é quem clicou no botão

    if (host == null) {
      logger.warn("Convidado '{}' tentou entrar na JAM de um host com ID '{}' que não foi encontrado no servidor '{}'.",
          guest.getEffectiveName(), hostId, event.getGuild().getName());
      event.reply("Parece que o anfitrião da JAM não está mais neste servidor.").setEphemeral(true).queue();
      return;
    }

    if (host.equals(guest)) {
      logger.info("Usuário '{}' tentou entrar em sua própria JAM.", guest.getEffectiveName());
      event.reply("Você não pode se juntar à sua própria JAM!").setEphemeral(true).queue();
      return;
    }

    // Encontra a atividade do Spotify do anfitrião
    Optional<Activity> spotifyActivity = JamCommand.findSpotifyActivity(host);

    if (spotifyActivity.isPresent()) {
      // Gera o convite de atividade
      spotifyActivity.get().createInvite()
          .setMaxAge(3600) // Convite válido por 1 hora
          .queue(
              invite -> {
                logger.info("Convite de JAM criado com sucesso para '{}' se juntar a '{}'.", guest.getEffectiveName(),
                    host.getEffectiveName());
                event.reply("Aqui está o seu convite para a JAM!\n" + invite.getUrl()).setEphemeral(true).queue();
              },
              error -> {
                logger.error("Falha ao criar convite de JAM para '{}' se juntar a '{}'. Causa: {}",
                    guest.getEffectiveName(),
                    host.getEffectiveName(),
                    error.getMessage(),
                    error);
                event
                    .reply(
                        "Não foi possível criar um convite para esta JAM. O anfitrião pode ter desativado os convites.")
                    .setEphemeral(true).queue();
              });
    } else {
      logger.warn("Convidado '{}' tentou entrar na JAM, mas o anfitrião '{}' não está mais ouvindo Spotify.",
          guest.getEffectiveName(), host.getEffectiveName());
      event.reply("O anfitrião não está mais em uma sessão de JAM que possa ser ingressada.").setEphemeral(true)
          .queue();
    }
  }
}
package com.backpech.discordbot.listeners;

import com.backpech.discordbot.commands.impl.JamCommand;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class JamListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String[] componentParts = event.getComponentId().split(":");
        if (!componentParts[0].equals("jam-join")) {
            return; // Ignora botões que não são da JAM
        }

        // O ID do anfitrião foi armazenado no ID do botão
        String hostId = componentParts[1];
        Member host = event.getGuild().getMemberById(hostId);
        Member guest = event.getMember(); // O convidado é quem clicou no botão

        if (host == null) {
            event.reply("Parece que o anfitrião da JAM não está mais neste servidor.").setEphemeral(true).queue();
            return;
        }

        if (host.equals(guest)) {
            event.reply("Você não pode se juntar à sua própria JAM!").setEphemeral(true).queue();
            return;
        }

        // Encontra a atividade do Spotify do anfitrião
        Optional<Activity> spotifyActivity = JamCommand.findSpotifyActivity(host);

        if (spotifyActivity.isPresent()) {
            // Gera o convite de atividade
            ReplyCallbackAction action = event.reply("Aqui está o seu convite para a JAM!");
            spotifyActivity.get().createInvite()
                    .setMaxAge(3600) // Convite válido por 1 hora
                    .queue(
                        invite -> action.addContent("\n" + invite.getUrl()).setEphemeral(true).queue(),
                        error -> event.reply("Não foi possível criar um convite para esta JAM.").setEphemeral(true).queue()
                    );
        } else {
            event.reply("O anfitrião não está mais em uma sessão de JAM que possa ser ingressada.").setEphemeral(true).queue();
        }
    }
}
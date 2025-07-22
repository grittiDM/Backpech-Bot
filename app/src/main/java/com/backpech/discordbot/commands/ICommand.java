package com.backpech.discordbot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface ICommand {
    /**
     * Retorna o nome do comando. Deve ser único.
     */
    String getName();

    /**
     * Retorna a descrição do comando.
     */
    String getDescription();

    /**
     * Executa a ação do comando.
     * @param event O evento de interação do comando.
     */
    void execute(SlashCommandInteractionEvent event);
}
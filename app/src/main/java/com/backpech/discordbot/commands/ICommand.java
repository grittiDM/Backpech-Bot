package com.backpech.discordbot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData; // <-- ESTA É A CORREÇÃO CRÍTICA

/**
 * Interface que define o contrato para todos os slash commands do bot.
 * Cada comando deve ser auto-contido, fornecendo seu nome, descrição,
 * estrutura de dados (opções, permissões) e lógica de execução.
 */
public interface ICommand {
  /**
   * Retorna o nome do comando. Deve ser único e em letras minúsculas.
   * 
   * @return O nome do comando.
   */
  String getName();

  /**
   * Retorna a descrição do comando, que será exibida na UI do Discord.
   * 
   * @return A descrição do comando.
   */
  String getDescription();

  /**
   * Constrói e retorna a estrutura de dados do comando para registro no Discord.
   * Inclui nome, descrição, opções e permissões padrão.
   * 
   * @return Um objeto {@link CommandData} para o JDA.
   */
  CommandData getCommandData();

  /**
   * Executa a lógica principal do comando.
   * 
   * @param event O evento de interação que acionou o comando.
   */
  void execute(SlashCommandInteractionEvent event);
}
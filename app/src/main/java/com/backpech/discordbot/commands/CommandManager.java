package com.backpech.discordbot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData; // A linha que corrige o erro
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Gerencia o registro e o despacho de todos os comandos do bot.
 * Esta classe é desacoplada da criação dos comandos, seguindo o Princípio de Responsabilidade Única.
 */
public class CommandManager {
  private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);
  private final Map<String, ICommand> commands = new ConcurrentHashMap<>();

  /**
   * O construtor agora é simples e não precisa de dependências.
   */
  public CommandManager() {
  }

  /**
   * Registra um comando no gerenciador. Este método agora é público para ser
   * chamado de fora da classe (pela classe Bot).
   *
   * @param command A instância do comando a ser adicionada.
   */
  public void addCommand(ICommand command) {
    if (commands.containsKey(command.getName())) {
      logger.error("Tentativa de registrar um comando duplicado: '{}'", command.getName());
      // Em um cenário real, poderia lançar uma exceção ou apenas logar e ignorar.
      return;
    }
    commands.put(command.getName(), command);
    logger.info("Comando '{}' registrado com sucesso.", command.getName());
  }

  public void handle(SlashCommandInteractionEvent event) {
    ICommand command = commands.get(event.getName());
    if (command != null) {
      command.execute(event);
    } else {
      logger.warn("Comando desconhecido recebido: '{}'", event.getName());
      event.reply("Comando desconhecido.").setEphemeral(true).queue();
    }
  }

  public Collection<CommandData> getAllCommandsAsData() {
    return commands.values().stream()
        .map(ICommand::getCommandData)
        .collect(Collectors.toList());
  }
}
package main.java.com.backpech.discordbot.commands;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import main.java.com.backpech.discordbot.commands.impl.BanCommand;
import main.java.com.backpech.discordbot.commands.impl.JamCommand;
import main.java.com.backpech.discordbot.commands.impl.MuteCommand;
import main.java.com.backpech.discordbot.config.BotConfig;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandManager {
    private final Map<String, ICommand> commands = new ConcurrentHashMap<>();

    public CommandManager(BotConfig config) {
        // Registrar todos os comandos aqui
        addCommand(new BanCommand(config));
        addCommand(new MuteCommand(config));
        addCommand(new JamCommand());
        // Adicione outros comandos aqui...
    }

    private void addCommand(ICommand command) {
        if (commands.containsKey(command.getName())) {
            throw new IllegalArgumentException("Command with name " + command.getName() + " already exists.");
        }
        commands.put(command.getName(), command);
    }

    public void handle(SlashCommandInteractionEvent event) {
        ICommand command = commands.get(event.getName());
        if (command != null) {
            command.execute(event);
        } else {
            event.reply("Comando desconhecido.").setEphemeral(true).queue();
        }
    }

    public Collection<CommandData> getAllCommandsAsData() {
        return commands.values().stream()
            .map(ICommand::getCommandData)
            .collect(Collectors.toList());
    }
}
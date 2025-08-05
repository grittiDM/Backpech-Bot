package com.backpech.discordbot;

import com.backpech.discordbot.commands.CommandManager;
import com.backpech.discordbot.commands.impl.*;
import com.backpech.discordbot.config.BotConfig;
import com.backpech.discordbot.listeners.CommandListener;
import com.backpech.discordbot.listeners.JamListener;
import com.backpech.discordbot.listeners.WelcomeListener;
import com.backpech.discordbot.services.GuildConfigService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] args) throws InterruptedException {
        BotConfig config = ConfigFactory.create(BotConfig.class);

        if (config.token() == null || config.token().isBlank() || config.token().equals("SEU_TOKEN_SECRETO_AQUI")) {
            logger.error("O token do bot não foi definido no arquivo config.properties. Encerrando.");
            return;
        }

        // --- 1. Inicialização dos Serviços e Gerenciadores ---
        GuildConfigService guildConfigService = new GuildConfigService();
        CommandManager commandManager = new CommandManager();

        // --- 2. Criação e Registro dos Comandos ---
        // A classe Bot agora é responsável por construir cada comando com suas dependências.
        commandManager.addCommand(new BanCommand(config));
        commandManager.addCommand(new MuteCommand(config));
        commandManager.addCommand(new JamCommand());
        commandManager.addCommand(new ConfigWelcomeRoleCommand(guildConfigService));
        // Para adicionar um novo comando, basta adicionar uma nova linha aqui.
        // ex: commandManager.addCommand(new HelpCommand());

        // --- 3. Inicialização dos Listeners ---
        WelcomeListener welcomeListener = new WelcomeListener(guildConfigService);
        CommandListener commandListener = new CommandListener(commandManager);
        JamListener jamListener = new JamListener();

        // --- 4. Construção e Inicialização do JDA ---
        JDA jda = JDABuilder.createDefault(config.token())
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .addEventListeners(
                    welcomeListener,
                    commandListener,
                    jamListener
                )
                .setActivity(Activity.watching("o servidor com atenção"))
                .build()
                .awaitReady();

        jda.updateCommands().addCommands(commandManager.getAllCommandsAsData()).queue(
            success -> logger.info("Comandos globais registrados com sucesso!"),
            error -> logger.error("Erro ao registrar comandos globais.", error)
        );

        logger.info("Bot está online e pronto!");
    }
}
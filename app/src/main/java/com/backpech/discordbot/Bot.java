package com.backpech.discordbot;

import com.backpech.discordbot.config.BotConfig;
import com.backpech.discordbot.commands.CommandManager;
import com.backpech.discordbot.listeners.CommandListener;
import com.backpech.discordbot.listeners.WelcomeListener;

import main.java.com.backpech.discordbot.listeners.JamListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.aeonbits.owner.ConfigFactory;

public class Bot {

    public static void main(String[] args) throws InterruptedException {
        // 1. Carregar a configuração de forma segura
        BotConfig config = ConfigFactory.create(BotConfig.class);

        // Validação do Token
        if (config.token() == null || config.token().equals("SEU_TOKEN_DO_BOT_AQUI")) {
            System.err.println("Erro: O token do bot não foi definido no arquivo config.properties.");
            return;
        }

        // 2. Instanciar o CommandManager, que agora gerencia todos os comandos
        CommandManager commandManager = new CommandManager(config);

        // 3. Instanciar os listeners necessários, injetando as dependências
        WelcomeListener welcomeListener = new WelcomeListener(config);
        CommandListener commandListener = new CommandListener(commandManager);
        JamListener jamListener = new JamListener();

        // 4. Construir o JDA com os listeners e a configuração de Intents
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

        // 5. Registrar/Atualizar os comandos no Discord
        // O CommandManager fornece os dados de todos os comandos registrados
        jda.updateCommands().addCommands(commandManager.getAllCommandsAsData()).queue(
                success -> System.out.println("Comandos registrados com sucesso!"),
                error -> System.err.println("Erro ao registrar comandos: " + error));

        System.out.println("Bot está online e pronto!");
    }
}
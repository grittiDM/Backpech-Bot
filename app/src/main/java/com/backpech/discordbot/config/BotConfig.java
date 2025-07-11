package main.java.com.backpech.discordbot.config;

import java.io.ObjectInputFilter.Config;
import java.util.List;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({ "file:./config.properties" })
public interface BotConfig extends Config {

    @Key("bot.token")
    String token();

    @Key("role.newUser")
    String newUserRole();

    @Key("role.moderator1")
    String moderatorRole1();

    @Key("role.moderator2")
    String moderatorRole2();

    // Método default para fornecer uma lista consolidada
    default List<String> moderatorRoleIds() {
        return List.of(moderatorRole1(), moderatorRole2());
    }
}
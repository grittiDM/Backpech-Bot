package com.backpech.discordbot.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GuildConfigServiceTest {

    private GuildConfigService configService;
    private String testDbUrl;

    // JUnit criará uma pasta temporária para cada teste, garantindo que não usemos
    // o DB de produção.
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws SQLException {
        // Configura um banco de dados de teste em um arquivo temporário
        File dbFile = tempDir.resolve("test_guild_configs.db").toFile();
        testDbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        // Inicializa o serviço com o nosso banco de dados de teste
        configService = new GuildConfigService(testDbUrl);
    }

    @Test
    void testSetAndGetWelcomeRole() {
        // --- Arrange (Preparação) ---
        String guildId = "1234567890";
        String roleId = "0987654321";

        // --- Act (Ação) ---
        configService.setWelcomeRole(guildId, roleId);
        Optional<String> retrievedRoleId = configService.getWelcomeRoleId(guildId);

        // --- Assert (Verificação) ---
        assertTrue(retrievedRoleId.isPresent(), "O ID do cargo deveria ter sido encontrado.");
        assertEquals(roleId, retrievedRoleId.get(), "O ID do cargo recuperado deve ser o mesmo que foi salvo.");
    }

    @Test
    void testGetWelcomeRole_WhenNotSet_ShouldReturnEmpty() {
        // --- Arrange ---
        String guildId = "non_existent_guild";

        // --- Act ---
        Optional<String> retrievedRoleId = configService.getWelcomeRoleId(guildId);

        // --- Assert ---
        assertFalse(retrievedRoleId.isPresent(),
                "Nenhum cargo deveria ser encontrado para um servidor não configurado.");
    }
}
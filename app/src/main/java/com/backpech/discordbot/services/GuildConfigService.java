package com.backpech.discordbot.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class GuildConfigService {

  private static final Logger logger = LoggerFactory.getLogger(GuildConfigService.class);
  private final String dbUrl;

  // Construtor padrão para produção
  public GuildConfigService() {
    this("jdbc:sqlite:guild_configs.db");
  }

  // Construtor para testes, permitindo um DB diferente
  public GuildConfigService(String dbUrl) {
    this.dbUrl = dbUrl;
    initializeDatabase();
  }

  private void initializeDatabase() {
    try (Connection conn = DriverManager.getConnection(dbUrl);
        Statement stmt = conn.createStatement()) {
      String sql = "CREATE TABLE IF NOT EXISTS guild_settings (" +
          "guild_id TEXT PRIMARY KEY NOT NULL," +
          "welcome_role_id TEXT" +
          ");";
      stmt.execute(sql);
    } catch (SQLException e) {
      logger.error("Failed to initialize database table at {}", dbUrl, e);
    }
  }

  public void setWelcomeRole(String guildId, String roleId) {
    String sql = "INSERT INTO guild_settings(guild_id, welcome_role_id) VALUES(?, ?) " +
        "ON CONFLICT(guild_id) DO UPDATE SET welcome_role_id = excluded.welcome_role_id;";

    try (Connection conn = DriverManager.getConnection(dbUrl);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, guildId);
      pstmt.setString(2, roleId);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to set welcome role for guild {}", guildId, e);
    }
  }

  public Optional<String> getWelcomeRoleId(String guildId) {
    String sql = "SELECT welcome_role_id FROM guild_settings WHERE guild_id = ?;";
    try (Connection conn = DriverManager.getConnection(dbUrl);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, guildId);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return Optional.ofNullable(rs.getString("welcome_role_id"));
      }
    } catch (SQLException e) {
      logger.error("Failed to get welcome role for guild {}", guildId, e);
    }
    return Optional.empty();
  }
}
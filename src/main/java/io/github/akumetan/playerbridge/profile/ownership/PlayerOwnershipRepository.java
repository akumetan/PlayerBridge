package io.github.akumetan.playerbridge.profile.ownership;

import io.github.akumetan.playerbridge.database.DatabaseManager;

import java.sql.*;
import java.time.Instant;
import java.util.UUID;

public final class PlayerOwnershipRepository {

    private final DatabaseManager dbManager;

    public PlayerOwnershipRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean tryAcquire(UUID uuid, String serverId, String token, Instant expiresAt) throws SQLException {
        String sql = """
                INSERT INTO player_profiles (
                    uuid,
                    player_data,
                    owner_server_id,
                    owner_token,
                    owner_until
                )
                VALUES (?, NULL, ?, ?, ?)
                
                ON DUPLICATE KEY UPDATE
                    owner_server_id = CASE 
                        WHEN owner_token IS NULL OR owner_until IS NULL OR owner_until <= CURRENT_TIMESTAMP(6) 
                        THEN VALUES(owner_server_id) 
                        ELSE owner_server_id 
                    END,
                    owner_token = CASE 
                        WHEN owner_token IS NULL OR owner_until IS NULL OR owner_until <= CURRENT_TIMESTAMP(6) 
                        THEN VALUES(owner_token) 
                        ELSE owner_token 
                    END,
                    owner_until = CASE 
                        WHEN owner_token IS NULL OR owner_until IS NULL OR owner_until <= CURRENT_TIMESTAMP(6) 
                        THEN VALUES(owner_until) 
                        ELSE owner_until 
                    END
                """;

        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, serverId);
            statement.setString(3, token);
            statement.setTimestamp(4, Timestamp.from(expiresAt));

            statement.executeUpdate();
        }

        return owns(uuid, serverId, token);
    }

    public PlayerOwnership find(UUID uuid) throws SQLException {
        String sql = """
                SELECT owner_server_id, owner_token, owner_until
                FROM player_profiles
                WHERE uuid = ?
                """;

        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next())
                    return null;

                String serverId = result.getString("owner_server_id");
                String token = result.getString("owner_token");
                Timestamp until = result.getTimestamp("owner_until");

                if (serverId == null || token == null || until == null)
                    return null;

                return new PlayerOwnership(serverId, token, until.toInstant());
            }
        }
    }

    public boolean owns(UUID uuid, String serverId, String token) throws SQLException {
        String sql = """
                SELECT 1
                FROM player_profiles
                WHERE uuid = ?
                  AND owner_server_id = ?
                  AND owner_token = ?
                """;

        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, serverId);
            statement.setString(3, token);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean renew(UUID uuid, String serverId, String token, Instant newExpiresAt) throws SQLException {
        String sql = """
                UPDATE player_profiles
                SET owner_until = ?
                WHERE uuid = ?
                  AND owner_server_id = ?
                  AND owner_token = ?
                  AND owner_until > CURRENT_TIMESTAMP(6)
                """;

        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.from(newExpiresAt));
            statement.setString(2, uuid.toString());
            statement.setString(3, serverId);
            statement.setString(4, token);

            return statement.executeUpdate() == 1;
        }
    }

    public boolean release(UUID uuid, String serverID, String token) throws SQLException {
        String sql = """
                UPDATE player_profiles
                SET
                    owner_server_id = NULL,
                    owner_token = NULL,
                    owner_until = NULL
                WHERE uuid = ?
                  AND owner_server_id = ?
                  AND owner_token = ?
                """;

        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, serverID);
            statement.setString(3, token);

            return statement.executeUpdate() == 1;
        }
    }
}
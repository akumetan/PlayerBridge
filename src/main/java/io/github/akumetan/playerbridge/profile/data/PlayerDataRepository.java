package io.github.akumetan.playerbridge.profile.data;

import io.github.akumetan.playerbridge.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class PlayerDataRepository {

    private final DatabaseManager dbManager;

    public PlayerDataRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public PlayerData load(UUID uuid) throws SQLException {
        String sql = """
                SELECT player_data
                FROM player_profiles
                WHERE uuid = ?
                """;

        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next())
                    return null;

                String serialized = result.getString("player_data");
                if (serialized == null || serialized.isBlank())
                    return null;

                return PlayerDataSerializer.deserialize(serialized);
            }
        }
    }

    public boolean save(PlayerData data, String serverId, String token) throws SQLException {
        String sql = """
                UPDATE player_profiles
                SET player_data = ?
                WHERE uuid = ?
                  AND owner_server_id = ?
                  AND owner_token = ?
                """;

        return this.performSaveSQL(data, serverId, token, sql);
    }

    public boolean saveAndRelease(PlayerData data, String serverId, String token) throws SQLException {
        String sql = """
                UPDATE player_profiles
                SET
                    player_data = ?,
                    owner_server_id = NULL,
                    owner_token = NULL,
                    owner_until = NULL
                WHERE uuid = ?
                  AND owner_server_id = ?
                  AND owner_token = ?
                """;

        return this.performSaveSQL(data, serverId, token, sql);
    }

    private boolean performSaveSQL(PlayerData data, String serverId, String token, String sql) throws SQLException {
        try (Connection connection = dbManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, PlayerDataSerializer.serialize(data));
            statement.setString(2, data.uuid().toString());
            statement.setString(3, serverId);
            statement.setString(4, token);

            return statement.executeUpdate() == 1;
        }
    }

}
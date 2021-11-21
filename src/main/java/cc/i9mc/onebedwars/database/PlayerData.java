package cc.i9mc.onebedwars.database;

import cc.i9mc.gameutils.BukkitGameUtils;
import cc.i9mc.onebedwars.game.GamePlayer;
import cc.i9mc.onebedwars.types.ModeType;
import lombok.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class PlayerData {
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

    private GamePlayer gamePlayer;
    private ModeType modeType;
    private int kills;
    private int deaths;
    private int destroyedBeds;
    private int wins;
    private int loses;
    private int games;

    public PlayerData(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM bw_stats_players Where Name=?");
            preparedStatement.setString(1, gamePlayer.getName());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                this.modeType = ModeType.valueOf(resultSet.getString("Mode"));
                this.kills = resultSet.getInt("kills");
                this.deaths = resultSet.getInt("deaths");
                this.destroyedBeds = resultSet.getInt("destroyedBeds");
                this.wins = resultSet.getInt("wins");
                this.loses = resultSet.getInt("loses");
                this.games = resultSet.getInt("games");
            } else {
                this.modeType = ModeType.DEFAULT;
                preparedStatement = connection.prepareStatement("INSERT INTO bw_stats_players (Name,Mode,kills,deaths,destroyedBeds,wins,loses,games) VALUES (?,?,0,0,0,0,0,0)");
                preparedStatement.setString(1, gamePlayer.getName());
                preparedStatement.setString(2, ModeType.DEFAULT.toString());
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setModeType(ModeType modeType) {
        if (this.modeType == modeType) {
            return;
        }

        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET Mode=? Where Name=?");
                preparedStatement.setString(1, modeType.toString());
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        this.modeType = modeType;
    }

    public void addKills() {
        kills += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET kills=? Where Name=?");
                preparedStatement.setInt(1, kills);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addFinalKills() {
        //finalKills += 1;
    }

    public void addDeaths() {
        deaths += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET deaths=? Where Name=?");
                preparedStatement.setInt(1, deaths);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addDestroyedBeds() {
        destroyedBeds += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET destroyedBeds=? Where Name=?");
                preparedStatement.setInt(1, destroyedBeds);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addWins() {
        wins += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET wins=? Where Name=?");
                preparedStatement.setInt(1, wins);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addLoses() {
        loses += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET loses=? Where Name=?");
                preparedStatement.setInt(1, loses);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    public void addGames() {
        games += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET games=? Where Name=?");
                preparedStatement.setInt(1, games);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
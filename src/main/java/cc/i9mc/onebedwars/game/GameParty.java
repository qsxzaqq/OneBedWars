package cc.i9mc.onebedwars.game;

import java.util.List;

/**
 * Created by JinVan on 2021-01-01.
 */
public class GameParty {
    private final Game game;
    private final GamePlayer leader;
    private final List<GamePlayer> players;

    public GameParty(Game game, GamePlayer leader, List<GamePlayer> gamePlayers) {
        this.game = game;
        this.leader = leader;
        players = gamePlayers;
        players.add(leader);
        game.addParty(this);
    }

    public GamePlayer getLeader() {
        return leader;
    }

    public boolean isLeader(GamePlayer p) {
        return p.equals(leader);
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }

    public boolean isInTeam(GamePlayer player) {
        return players.contains(player);
    }

    public void removePlayer(GamePlayer p) {
        if (!isInTeam(p)) {
            return;
        }
        players.remove(p);
        if (players.isEmpty()) {
            game.removeParty(this);
        }
    }
}

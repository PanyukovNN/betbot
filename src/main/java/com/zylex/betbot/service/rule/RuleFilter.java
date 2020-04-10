package com.zylex.betbot.service.rule;

import com.zylex.betbot.model.bet.BetCoefficient;
import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.rule.Rule;
import com.zylex.betbot.model.rule.RuleCondition;
import com.zylex.betbot.service.repository.LeagueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters games by rule conditions.
 */
@Service
public class RuleFilter {

    private LeagueRepository leagueRepository;

    @Autowired
    public RuleFilter(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    /**
     * Filters games by rule conditions, which are taken from database,
     * also filters by selected leagues and exclude leagues.
     * @param games - list of games to filter.
     * @param rule - specified rule.
     * @return - list of filtered games.
     */
    List<Game> filter(List<Game> games, Rule rule) {
        List<Game> eligibleGames = new ArrayList<>();
        List<String> excludeLeagues = leagueRepository.getExcludeLeagues(rule);
        List<String> selectedLeagues = leagueRepository.getAllSelectedLeagues();
        for (Game game : games) {
            if (rule.isSelectedLeagues()) {
                if (!selectedLeagues.contains(game.getLeague().getLink())) {
                    continue;
                }
            }
            if (excludeLeagues.contains(game.getLeague().getLink())) {
                continue;
            }
            boolean appropriate = true;
            for (RuleCondition ruleCondition : rule.getRuleConditions()) {
                if (!isAppropriate(game, ruleCondition)) {
                    appropriate = false;
                    break;
                }
            }
            if (appropriate) {
                game.getRules().add(rule);
                eligibleGames.add(game);
            }
        }
        return eligibleGames;
    }

    private boolean isAppropriate(Game game, RuleCondition ruleCondition) {
        if (ruleCondition.getCoefficient().equals(BetCoefficient.FIRST_WIN.toString())) {
            return defineOperator(game.getGameInfo().getFirstWin(), ruleCondition);
        } else if (ruleCondition.getCoefficient().equals(BetCoefficient.TIE.toString())) {
            return defineOperator(game.getGameInfo().getTie(), ruleCondition);
        } else if (ruleCondition.getCoefficient().equals(BetCoefficient.SECOND_WIN.toString())) {
            return defineOperator(game.getGameInfo().getSecondWin(), ruleCondition);
        } else if (ruleCondition.getCoefficient().equals(BetCoefficient.ONE_X.toString())) {
            return defineOperator(game.getGameInfo().getOneX(), ruleCondition);
        } else if (ruleCondition.getCoefficient().equals(BetCoefficient.X_TWO.toString())) {
            return defineOperator(game.getGameInfo().getXTwo(), ruleCondition);
        }
        return false;
    }

    private boolean defineOperator(double gameValue, RuleCondition ruleCondition) {
        if ("MORE".equals(ruleCondition.getOperator())) {
            return gameValue > ruleCondition.getValue();
        } else if ("MORE_EVEN".equals(ruleCondition.getOperator())) {
            return gameValue >= ruleCondition.getValue();
        } else if ("LESS".equals(ruleCondition.getOperator())) {
            return gameValue < ruleCondition.getValue();
        } else if ("LESS_EVEN".equals(ruleCondition.getOperator())) {
            return gameValue <= ruleCondition.getValue();
        }
        return false;
    }
}

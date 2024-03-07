package at.fhtw.mtcgapp.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class TradingDeal {
    @JsonAlias({"Id"})
    private String trading_id;
    @JsonAlias({"CardToTrade"})
    private String card_to_trade;
    @JsonAlias({"Type"})
    private String card_type;
    @JsonAlias({"MinimumDamage"})
    private Integer minimum_damage;

    // Jackson needs the default constructor
    public TradingDeal() {}

    public TradingDeal(String trading_id, String card_to_trade, String card_type, Integer minimum_damage) {
        this.trading_id = trading_id;
        this.card_to_trade = card_to_trade;
        this.card_type = card_type;
        this.minimum_damage = minimum_damage;
    }

    public String getTrading_id() {
        return trading_id;
    }

    public void setTrading_id(String trading_id) {
        this.trading_id = trading_id;
    }

    public String getCard_to_trade() {
        return card_to_trade;
    }

    public void setCard_to_trade(String card_to_trade) {
        this.card_to_trade = card_to_trade;
    }

    public String getCard_type() {
        return card_type;
    }

    public void setCard_type(String card_type) {
        this.card_type = card_type;
    }

    public Integer getMinimum_damage() {
        return minimum_damage;
    }

    public void setMinimum_damage(Integer minimum_damage) {
        this.minimum_damage = minimum_damage;
    }
}

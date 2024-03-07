package at.fhtw.mtcgapp.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Card {
    @JsonAlias({"Id"})
    private String card_id;
    @JsonAlias({"Name"})
    private String name;
    @JsonAlias({"Damage"})
    private Integer damage;
    @JsonIgnore
    private String card_type;
    @JsonIgnore
    private String element_type;
    @JsonIgnore
    private String battle_log;


    // Jackson needs the default constructor
    public Card(@JsonProperty("Name") String name)
    {
        this.name = name;
        setCardType();
        setElementType();
        battle_log = "";
    }

    public Card(String card_id, String name, Integer damage) {
        this.card_id = card_id;
        this.name = name;
        this.damage = damage;
        setCardType();
        setElementType();
        battle_log = "";
    }

    public String getCard_id() {
        return card_id;
    }

    public void setCard_id(String card_id) {
        this.card_id = card_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDamage() {
        return damage;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

    public String getCardType() { return card_type; }

    public String getElementType() { return element_type; }

    private void setCardType()
    {
        if(this.name.toLowerCase().endsWith("spell"))
        {
            this.card_type = "spell";
        }
        else
        {
            this.card_type = "monster";
        }
    }

    private void setElementType()
    {
        if(this.name.toLowerCase().startsWith("fire"))
        {
            this.element_type = "fire";
        }
        else if(this.name.toLowerCase().startsWith("water"))
        {
            this.element_type = "water";
        }
        else
        {
            this.element_type = "normal";
        }
    }
}

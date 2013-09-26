package com.tesco.adapters.sonetto.Elements;


import com.googlecode.totallylazy.Predicate;


import javax.xml.bind.annotation.*;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.hamcrest.CoreMatchers.is;

@XmlRootElement (name="Promotions")
public class Promotions {

    @XmlElement(name = "Promotion")
    private List<Promotion> promotionList;

    public List<Promotion> getPromotions(){
        return promotionList;
    }

    public List<Promotion> getInternetPromotions()
    {
        return filter(having(on(Promotion.class).isInternetExclusive(), is(true)), promotionList);
    }

    public List<Promotion> getStorePromotions() {
        return sequence(promotionList).filter(new Predicate<Promotion>() {
            @Override
            public boolean matches(Promotion promotion) {
                return !promotion.isInternetExclusive();
            }
        }).toList();
    }
}

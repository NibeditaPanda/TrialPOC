package com.tesco.adapters.sonetto.Elements;


import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;


import javax.xml.bind.annotation.*;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.hamcrest.CoreMatchers.is;

@XmlRootElement (name="Promotions")
public class Promotions {

    @XmlElement(name = "Promotion")
    private List<Promotion> foo;

    public List<Promotion> getPromotions(){
        return foo;
    }

    public List<Promotion> getInternetPromotions()
    {
        return filter(having(on(Promotion.class).isInternetExclusive(), is(true)), foo);
    }

    public List<Promotion> getStorePromotions() {
        return sequence(foo).filter(new Predicate<Promotion>() {
            @Override
            public boolean matches(Promotion promotion) {
                return !promotion.isInternetExclusive();
            }
        }).toList();
    }
}

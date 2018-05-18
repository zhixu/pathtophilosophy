/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bento.sessionbeans;

import com.bento.entitybeans.Articles;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author wing
 */
@Stateless
public class AddArticlesBean implements AddArticlesBeanLocal {

    @PersistenceContext(unitName = "com.bento_pathtophilosophy-ejb_ejb_1.0PU")
    private EntityManager em;

//    @Override
//    protected EntityManager getEntityManager() {
//        return em;
//    }

    public Articles execute(String title,
                   Articles nextArticleTitle) {
        Articles a = new Articles();
        a.setTitle(title);
        a.setLastUpdate(new Date());
        a.setNextArticleTitle(nextArticleTitle);
        
        try {
                em.merge(a);
                em.flush();
                return a;
            } catch (Exception e) {
                // TODO: stuff
                System.out.println("WE RAN INTO AN EXCEPTION: " + e.getMessage() + e.toString());
                return null;
            }
    }
    
}

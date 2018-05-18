/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bento.sessionbeans;

import com.bento.entitybeans.Articles;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author wing
 */
@Stateless
public class GetArticlesBean implements GetArticlesBeanLocal {

    @PersistenceContext(unitName = "com.bento_pathtophilosophy-ejb_ejb_1.0PU")
    private EntityManager em;

//    @Override
//    protected EntityManager getEntityManager() {
//        return em;
//    }

    public Articles execute(String title) {
        Query q = em.createNamedQuery("Articles.findByTitle");
        q.setParameter("title", title);
        
        try {
            Articles a = (Articles) q.getSingleResult();
            return a;
        } catch (NoResultException e) {
            return null;
        }
    }
    
}

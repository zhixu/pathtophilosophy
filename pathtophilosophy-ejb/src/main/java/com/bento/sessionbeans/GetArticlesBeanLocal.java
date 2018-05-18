/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bento.sessionbeans;

import com.bento.entitybeans.Articles;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author wing
 */
@Local
public interface GetArticlesBeanLocal {

    Articles execute(String title);
    
}

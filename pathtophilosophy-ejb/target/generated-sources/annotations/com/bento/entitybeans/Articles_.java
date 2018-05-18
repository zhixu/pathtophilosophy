package com.bento.entitybeans;

import com.bento.entitybeans.Articles;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2018-05-17T23:47:27")
@StaticMetamodel(Articles.class)
public class Articles_ { 

    public static volatile CollectionAttribute<Articles, Articles> articlesCollection;
    public static volatile SingularAttribute<Articles, Date> lastUpdate;
    public static volatile SingularAttribute<Articles, Articles> nextArticleTitle;
    public static volatile SingularAttribute<Articles, String> title;

}
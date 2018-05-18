/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bento.utils;

import com.bento.entitybeans.Articles;
import com.bento.sessionbeans.AddArticlesBeanLocal;
import com.bento.sessionbeans.GetArticlesBeanLocal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author wing
 */
public class Crawler {
    
    // Session beans
    @EJB private AddArticlesBeanLocal addArticlesBean;
    @EJB private GetArticlesBeanLocal getArticlesBean;
    
    // Message we're sending back to the front end
    private String message;
    
    // Set of titles of articles that we've already visited to avoid cycles
    private Set<String> visited = new HashSet();
    
    // Keeps the order of the articles we've visited
    private List<String> visitedOrder = new ArrayList<>();
    
    // Keeps the articles we already have in our DB 
    private List<Articles> dbArticles = new ArrayList<>();
    
    public Crawler() throws NamingException {
        // bean lookup
        Context c = new InitialContext();
        getArticlesBean = (GetArticlesBeanLocal) c.lookup("java:global/pathtophilosophy-ear-1.0/pathtophilosophy-ejb-1.0/GetArticlesBean!com.bento.sessionbeans.GetArticlesBeanLocal");
        addArticlesBean = (AddArticlesBeanLocal) c.lookup("java:global/pathtophilosophy-ear-1.0/pathtophilosophy-ejb-1.0/AddArticlesBean!com.bento.sessionbeans.AddArticlesBeanLocal");
    }
    
    /**
     * 
     * @param url - the input URL in the form of "https://en.wikipedia.org/wiki/....."
     * @return - String representation of JSON of form
     *  { 
     *      "path" : [ "Article 1", "Article 2", ... ],
     *      "message": "You can reach 'Philosophy' in 6 hops."
     *  }
     */
    public String getPath(String url) {
        try {
            
            getPathHelper(url);
            
            JsonArrayBuilder pathJson = Json.createArrayBuilder();
            for (String item : visitedOrder) {
                pathJson.add(item);
            }
            
            for (Articles a : dbArticles) {
                pathJson.add(a.getTitle());
            }

            String json = Json.createObjectBuilder()
                .add("path", pathJson.build())
                .add("message", message)
                .build()
                .toString();
            
            System.out.println(json);
            return json;
            
        } finally {
            addInDB();
        }
    }
    
    /**
     * Main logic for getting the path to philosophy.
     * @param url - the input URL in the form of "https://en.wikipedia.org/wiki/....."
     * @return 
     */
    private boolean getPathHelper(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            String title = doc.getElementById("firstHeading").text();
            Element next = getNextArticleDetails(title, doc);
            
            while (!title.equals("Philosophy")) {

                if (visited.contains(title)) {
                    visitedOrder.add(title);
                    message = "Can't reach 'Philosophy'--We\'re in a loop!"; // System.out.println("We hit a loop with: " + title);
                    return false;
                }
                // mark current page as visited, add to the visitedOrder
                visited.add(title);
                visitedOrder.add(title);
                
                // check if this article info is already stored in DB
                // if it is, then get the path from there
                Articles a = getArticlesBean.execute(title);
                if (a != null && a.getNextArticleTitle() != null) {
                    return getPathFromDB(a);
                } else if (a != null && a.getNextArticleTitle() == null) {
                    message = "Can't reach 'Philosophy' -- We hit a dead end!";
                    return false;
                }
                
                // crawl the next article for info
                if (next == null) {
                    message = title + "does not have any links to other Wikipedia pages!";
                    return false;
                }

                doc = Jsoup.connect("https://en.wikipedia.org" + next.attr("href")).get();
                title = doc.getElementById("firstHeading").text();
                next = getNextArticleDetails(title, doc);
            }
            
            // add Philosophy at the end
            visitedOrder.add(title);
            int hops = visitedOrder.size() - 1;
            message = "You can reach 'Philosophy' in " + hops + " hops.";
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    
    /**
     * This function looks through the current Wikipedia article's page and returns
     * the next link (as defined by 'Getting to Philosophy' page on this article's page.
     * @param currTitle - the current Document's title
     * @param document - the Document created by Jsoup of the current page
     * @return - Jsoup Element representation of the anchor link for the next article
     */
    private Element getNextArticleDetails(String currTitle, Document document) {
        // Link criterias (according to https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy)
        // First non-parenthesized, non-italicized link
        // Ignore external links, links to the current page, or red links (links to non-existent pages)        
        Elements content = document.select("#mw-content-text");
        content.select(".hatnote").empty();
        content.select(".thumb").empty();
        content.select("#toc").empty();
        content.select("sup").empty();
        content.select("table").empty();
        content.select(".portal").empty();
        content.select("span").empty();

        for (Element paragraph : document.select("#mw-content-text").select("p, li")) {
            String htmlString = removeParentheses(paragraph.html());
            Document pDoc = Jsoup.parse(htmlString);
            Elements links = pDoc.select("a");
            
            for (Element link : links) {
                if (link.attr("href").startsWith("/wiki/") && link.attr("title") != currTitle) {
                    return link;
                }
            }
        }
        return null;
    }
    
    /**
     * This function parses out parentheses and ignores parentheses in links 
     * (i.e. href="/wiki/Element_(mathematics)")
     * @param s - String representation of the HTML document we're parsing
     * @return - parentheses-free String representation of the HTML document
     */
    private String removeParentheses(String s) {
        int start = s.indexOf('(');
        if (start == -1) return s;
        
        int openParensCount = 1;
        for (int i = start + 1; i < s.length(); i++) {
            if (s.charAt(i) == '(') openParensCount++;
            if (s.charAt(i) == ')') {
                openParensCount--;
                if (openParensCount == 0) {
                    if (s.charAt(i+1) == '"' && s.charAt(start-1) == '_') return s;
                    else {
                        if (i+1 == s.length()) return s.substring(0, start);
                        else return s.substring(0, start) + removeParentheses(s.substring(i+1));//s.substring(i+1, s.length());
                    }
                }
            }
        }
        return s;
    }
    
    /**
     * This function returns the path that has already been stored in the DB.
     * @param a - the starting article
     * @return - boolean of whether or not the database path ends in Philosophy
     */
    private boolean getPathFromDB(Articles a) {
        Articles curr = a.getNextArticleTitle();
        while (curr.getNextArticleTitle() != null) {
            String title = curr.getTitle();
            dbArticles.add(curr);
            if (visited.contains(title)) {
                message = "Can't reach 'Philosophy'--We\'re in a loop!";
                return false;
            }
            visited.add(title);
            curr = curr.getNextArticleTitle();
        }
        
        dbArticles.add(curr);
        String title = curr.getTitle();
        if (title.equals("Philosophy")) {
            int hops = visitedOrder.size() + dbArticles.size() - 1;
            message = "You can reach 'Philosophy' in " + hops + " hops.";
            return true;
        } else {
            message = "Can't reach 'Philosophy' -- We hit a dead end!";
            return false;
        }
    }
    
    /**
     * Stores the articles we've crawled through into the DB.
     */
    private void addInDB() {

        int end = visitedOrder.size() - 1;
        Articles b = new Articles();
        if (!dbArticles.isEmpty()) {
            b = dbArticles.get(0);
        } else {
            if (end >= 0) {
                String title = visitedOrder.get(end);
                b = addArticlesBean.execute(title, null);
                end = end - 1;
            }
        }
        
        for (int i = end; i >= 0; i--) {
            String prevTitle = (b.getTitle() == null) ? "null" : b.getTitle();
            b = addArticlesBean.execute(visitedOrder.get(i), b);
        }
    }
}

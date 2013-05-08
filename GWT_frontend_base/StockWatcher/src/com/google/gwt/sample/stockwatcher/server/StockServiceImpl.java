package com.google.gwt.sample.stockwatcher.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.sample.stockwatcher.client.NotLoggedInException;
import com.google.gwt.sample.stockwatcher.client.StockService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class StockServiceImpl extends RemoteServiceServlet implements
StockService {

  private static final Logger LOG = Logger.getLogger(StockServiceImpl.class.getName());
  private static final PersistenceManagerFactory PMF =
      JDOHelper.getPersistenceManagerFactory("transactions-optional");

  public void addStock(String symbol, String price, String date) throws NotLoggedInException {
    checkLoggedIn();
    PersistenceManager pm = getPersistenceManager();
    try {
      pm.makePersistent(new Stock(getUser(), symbol, price, date));
    } finally {
      pm.close();
    }
  }

  public void removeStock(String symbol) throws NotLoggedInException {
    checkLoggedIn();
    PersistenceManager pm = getPersistenceManager();
    try {
      long deleteCount = 0;
      Query q = pm.newQuery(Stock.class, "user == u");
      q.declareParameters("com.google.appengine.api.users.User u");
      List<Stock> stocks = (List<Stock>) q.execute(getUser());
      for (Stock stock : stocks) {
        if (symbol.equals(stock.getSymbol())) {
          deleteCount++;
          pm.deletePersistent(stock);
        }
      }
      if (deleteCount != 1) {
        LOG.log(Level.WARNING, "removeStock deleted "+deleteCount+" Stocks");
      }
    } finally {
      pm.close();
    }
  }

  public String[][] getStocks() throws NotLoggedInException {
    checkLoggedIn();
    PersistenceManager pm = getPersistenceManager();
    List<String> symbols = new ArrayList<String>();
    List<String> prices= new ArrayList<String>();
    List<String> dates = new ArrayList<String>();
    try {
      Query q = pm.newQuery(Stock.class, "user == u");
      q.declareParameters("com.google.appengine.api.users.User u");
      q.setOrdering("createDate");
      List<Stock> stocks = (List<Stock>) q.execute(getUser());
      for (Stock stock : stocks) {
        symbols.add(stock.getSymbol());
        prices.add(stock.getPrice());
        dates.add(stock.getDate());
      }
    } finally {
      pm.close();
    }
    String[] sy = (String[]) symbols.toArray(new String[0]);
    String[] pr = (String[]) prices.toArray(new String[0]);
    String[] da = (String[]) dates.toArray(new String[0]);
    String[][] colle = new String[sy.length][3];
    for(int i = 0; i < sy.length;i++){
    	colle[i][0] = sy[i];
    	colle[i][1] = pr[i];
    	colle[i][2] = da[i];
    }
    
    return colle;
  }
  
  public String[][] queryStocks(String symbol) throws NotLoggedInException {
	    checkLoggedIn();
	    PersistenceManager pm = getPersistenceManager();
	    List<String> symbols = new ArrayList<String>();
	    List<String> prices= new ArrayList<String>();
	    List<String> dates = new ArrayList<String>();
	    try {
	    	Query q = pm.newQuery(Stock.class, "user == u");
	        q.declareParameters("com.google.appengine.api.users.User u");
	      
	      List<Stock> stocks = (List<Stock>) q.execute(getUser());
	      for (Stock stock : stocks) {
//	        symbols.add(stock.getSymbol());
//	        prices.add(stock.getPrice());
//	        dates.add(stock.getDate());
	        if (symbol.equals(stock.getSymbol())) {
	        	symbols.add(stock.getSymbol());
	        	prices.add(stock.getPrice());
	        	dates.add(stock.getDate());
	        }
	      }
	    } finally {
	      pm.close();
	    }
	    String[] sy = (String[]) symbols.toArray(new String[0]);
	    String[] pr = (String[]) prices.toArray(new String[0]);
	    String[] da = (String[]) dates.toArray(new String[0]);
	    String[][] colle = new String[sy.length][3];
	    for(int i = 0; i < sy.length;i++){
	    	colle[i][0] = sy[i];
	    	colle[i][1] = pr[i];
	    	colle[i][2] = da[i];
	    }
	    
	    return colle;
	  }

  private void checkLoggedIn() throws NotLoggedInException {
    if (getUser() == null) {
      throw new NotLoggedInException("Not logged in.");
    }
  }

  private User getUser() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
  }

  private PersistenceManager getPersistenceManager() {
    return PMF.getPersistenceManager();
  }
}
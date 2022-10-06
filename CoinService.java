import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;



public class CoinService {
	
   //Map of all fetched coins	
   private Map<String,Coin> coinMap = new ConcurrentHashMap<String,Coin>(); 
   private int cach_ttl = 15000; 
  
   public CoinService(){
	  
	   this.populateCoinMap();
   } 
   
   // returns all coin names
   public List<String> getCoinNames(){
	   
	   List<String> names = new LinkedList<String>();
	   
	   coinMap.values().forEach(coin -> names.add(coin.getCoinName()));
	   
	   System.out.println("names" + coinMap.size());
	   
	   return names;
   }
   
   //returns coins with desired algorithm
   public List<Coin> getCoinsForAlgorithm(String algorithm) {
	 
	   return
	   coinMap.values().stream()
	          .filter(coin -> coin.getAlgorithm().equals(algorithm)).collect(Collectors.toList());
	   
   }
   
   //returns coins with desired symbol
   public List<Coin> getCoinsForSymbols(String symbolString) {
	   
	  List<String> symbols = Arrays.asList(symbolString.split(","));
	   
	 
	 
	   return
		 coinMap.values().stream()
	            .filter(coin -> symbols.contains(coin.getSymbol())).collect(Collectors.toList());

   }
   
   //returns single coin with all properties, including currency
   public Coin getSingleCoinForSymbol(String symbol){
	 
	  String[] str = {symbol};
	  populateToUSDField(str);
	  
	  return coinMap.get(symbol);
   }
   
   //call to rest api
   private String callREST(String url, String method){

           StringBuffer jsonResponseData = new StringBuffer();

            try{
                URL usersUrl = new URL(url);

                //set user url
                HttpsURLConnection connection = (HttpsURLConnection)usersUrl.openConnection();

                // Set request method
                connection.setRequestMethod(method);

                // Getting response code
                int statusCode = connection.getResponseCode();

                // If responseCode is not 200, data fetch unsuccessful
                if (statusCode != HttpsURLConnection.HTTP_OK){
                    System.out.println("url/connection problem, statusCode------>" + statusCode);
                } else {
                    // read input string
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    jsonResponseData.append(in.readLine());
                    in.close();
                }

            } catch(Exception ex){

                System.out.println("callREST exception---->" + ex.toString());
            }
       return jsonResponseData.toString();
   }
   
  
   
   private void populateCoinMapWithInterval(){
	   
	  System.out.println("populateCoinMapWithInterval()");
	   
	  Runnable task = () -> {
		   
		   try {
			   
		       while(true) {
   				
   			     
   			     Thread.sleep(cach_ttl);
   			      this.populateCoinMap();
		       }
		       
   		} catch(InterruptedException e){
   			
   			e.printStackTrace();
   		}
	  };
	  
	  Thread thread = new Thread(task);
	 // thread.setDaemon(true);
	  thread.start();
	  thread.yield();
   }  
	   
   private void populateCoinMap() {   
		   
	   System.out.println("populateCoinMap()");
	   
	   String url = "https://min-api.cryptocompare.com/data/all/coinlist";
	   String[] str = callREST(url, "GET").split("},");
       StringBuffer buf;
       JSONObject obj;

       // fill up properties
       for(String s : str) {
        
           buf = new StringBuffer("{");
           if (s.indexOf("\"Id\"") > -1){
        	   
               buf.append(s.substring(s.indexOf("\"Id\""))).append("}}");
               obj = new JSONObject(buf.toString());
               
               //coinMap.put(url, null)
            
               coinMap.put(obj.getString("Symbol"),
            		       new Coin(
            				   obj.getInt("Id"),
            				   obj.getString("Symbol"),
            				   obj.getString("CoinName"),
            				   obj.getString("Algorithm")
            		        ));
           }
       };
       
       System.out.println(" end of populateCoinMap()" + coinMap.size());
   }
   
   
   private void populateToUSDField(String[] symbols){
	   
	
	   StringBuffer buf = new StringBuffer("https://min-api.cryptocompare.com/data/pricemulti?fsyms=");
	   JSONObject obj;
       String url;
       
       for(int i= 0; i < symbols.length; i++){
    	   
    	   buf.append(symbols[i]).append(",");
    	   
    	   // populate toUSD fields in bulk
           if(buf.length() > 300 || i==symbols.length-1){
        	   
        	  url = buf.replace(buf.lastIndexOf(","), buf.length(), "&tsyms= USD").toString();
        	   
              // api call
              obj = new JSONObject(callREST(url, "GET"));
               
              for(String symbol : symbols)
                	  
                   if( obj.keySet().contains(symbol) )
                	   
                	   coinMap.get(symbol).setToUsd(obj.getJSONObject(symbol).getFloat("USD"));
                          
               //reset buffer        
               buf = new StringBuffer("https://min-api.cryptocompare.com/data/pricemulti?fsyms=");
               
          }
      }
   }
   
   public static void main(String[] args) {
	   

      CoinService main = new CoinService();



       System.out.println("-------------Test GET-------------------------------");

          //list of all currencies
           String st1 = "https://min-api.cryptocompare.com/data/all/coinlist";
           String st2 = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=ETH,BTC,USDI&tsyms=USD";
         //  StringBuffer buf1 = new StringBuffer("https://min-api.cryptocompare.com/data/pricemulti?fsyms=");

           String[] str = main.callREST(st1, "GET").split("},");
           List<Coin> coins = new LinkedList<Coin>();
           StringBuilder buf, buf1;
           Coin coin;
           JSONObject obj;

           for(String s : str) {
               coin = new Coin();
               buf = new StringBuilder("{");
               if (s.indexOf("\"Id\"") > -1){
                   buf.append(s.substring(s.indexOf("\"Id\""))).append("}}");
                   obj = new JSONObject(buf.toString());
                   coin.Id = obj.getInt("Id");
                  //System.out.println(coin.Id);
                   coin.symbol = obj.getString("Symbol");
                   coin.coinName = obj.getString("CoinName");
                   coin.algorithm = obj.getString("Algorithm");
                   coins.add(coin);
               }
           };

           // populate toUSD field
           buf1 = new StringBuilder("https://min-api.cryptocompare.com/data/pricemulti?fsyms=");
           int counter = 0;
           while(counter < coins.size()){
               if(buf1.length() < 300){
                   buf1.append(coins.get(counter).symbol).append(",");
                   counter++;
               } else {
                   String url = buf1.replace(buf1.lastIndexOf(","),buf1.length(), "&tsyms=USD").toString();
                  // System.out.println(url);
                   obj = new JSONObject(main.callREST(url, "GET"));
                   Set<String> keys = obj.keySet();
                   for(String key : keys) {
                       for(Coin c : coins)
                           if(c.symbol.equals(key)){
                               c.toUsd = obj.getJSONObject(key).getFloat("USD");
                               break;
                           }
                   }

                   buf1 = new StringBuilder("https://min-api.cryptocompare.com/data/pricemulti?fsyms=");
                  // System.out.println(main.callREST(url, "GET"));

               }
           }

           coins.forEach(c -> System.out.println(c.coinName + " ---> " + c.toUsd ));
           System.out.println("\tsize---> " + coins.size());






   }
}

package com.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;

/**
 * A sample resource that provides access to 
 * server configuration properties
 */

@Path("/coins")
public class CryptoCurrencyService {
	
	
	//Map of all fetched coins	
	private static Map<String, Coin> coinMap = new ConcurrentHashMap<String,Coin>(); 
	private static int cache_ttl = 15000; 
	
	static {
		
      System.out.println("Static block starts");
        
      try{      
        
    	//initial single run
    	populateCoinMap();  
        
        Timer timer = new Timer();
        
        TimerTask tt = new TimerTask(){
 		   
 		      public void run() {
 			
 			   try {
 				  System.out.println("scheduler runs");
 				 //consecutive runs
 			     populateCoinMap();  
 			   }  catch(Exception ex){
				  //run_flag = false;
				  ex.printStackTrace();
		       };
 		    };
 	     }; 
 	     
 	    timer.schedule(tt, 0, cache_ttl);    
 	   
 	   }catch(Exception ex){
		  ex.printStackTrace();
	   }
    }
	
	public CryptoCurrencyService(){
    	System.out.println("-----> new instance <------");
    }
  
    @GET
    @Produces(value="text/plain")
    public String listCoinNames(@Context UriInfo urlInfo){
    		
    	MultivaluedMap<String,String> param = urlInfo.getQueryParameters();   
    
        StringBuffer buffer = new StringBuffer();
        List<String> names = new LinkedList<String>();
        
        if(param.containsKey("algorithm")){
        
           this.getCoinsForAlgorithm(param.getFirst("algorithm"))
               .forEach(entry -> names.add(entry.coinName)); 
           
          // buffer.append("\n\t"+entry.coinName)
           
        } else if(param.containsKey("symbol")){
        
        	this.getCoinsForSymbols(param.getFirst("symbol"))
                .forEach(entry -> names.add(entry.coinName)); 
           
          // buffer.append("\n\t"+entry.coinName)
        	
        } else {
        	
        	names.addAll(getCoinNames());
        
         //   this.getCoinNames()
           //     .forEach(entry -> buffer.append("\n\t"+entry));
        	
           // buffer.append(assembleHtmlform(this.getCoinNames()));
     
        }
        
        names.forEach(entry -> buffer.append("\n\t"+entry));
        
       // buffer.append(assembleHtmlform(names));
        
        return buffer.toString();
    }

    @GET
    @Produces(value="application/json")
    @Path("/:{symbol}")
    public JSONObject getPropety(@PathParam("symbol") String symbol) {
    	
         Coin coin =  this.getSingleCoinForSymbol(symbol);
         JSONObject obj = new JSONObject();
         
         try {
        	 
           
            obj.put("Id", coin.getId());
            obj.put("symbol", coin.getSymbol());
            obj.put("coinName", coin.getCoinName());
            obj.put("algorithm", coin.getAlgorithm());
            obj.put("toUSD", coin.getToUsd());
            
            
         } catch(Exception ex) {
        	 
        	 ex.printStackTrace();
         }
         
        return obj;  
    }
    
    @GET
    @Produces(value="text/html")
    @Path("ttl_form")
    public String getHTMLList(){
    	
    StringBuffer buf = new StringBuffer();
    buf.append("\n <script>");
    buf.append("\n var s = document.createElement('script'); ");
    buf.append("\n s.text = 'http://code.jquery.com/jquery-1.7.2.min.js';");
    buf.append("\n s.type = 'text/javascript' ");
    buf.append("\n document.getElementsByTagName('head')[0].appendChild(s); ");
    buf.append("\n </script>");
   
    buf.append("<form id='myForm' method='post' action='/apiproxy/rest/coins/cachettl'>");
    buf.append("<label for='name'>Enter ttl:</label><br>");
    buf.append("<input type='text' id='ttl' name='ttl' /><br>");
    buf.append("<input id='tokenField' type='hidden' name='headers[Authorization]' value='Bearer token'/><br/>");
    buf.append("<input type='submit' id='submitButton' value='Submit'>");
    buf.append("</form>");
    
    
    buf.append("\n <script>");
    buf.append("$(document).ready(function(){\r\n"
    		+ "  $('ttl').click(function(){\r\n"
    		+ "    $(this).hide();\r\n"
    		+ "  });\r\n"
    		+ "});");
    buf.append("</script>");
 
    /*
     src='list.js'
    buf.append("\n var v = $('#tokenField').val(); alert(v);");
    buf.append(
     " $('#submitButton').on('click',function(){"    +
     "   $.ajax({"                                   +
              "url:'/apiproxy/rest/coins/cachettl'," +
             " type: 'POST',"                        +
             "data: { ttl: $('#ttl').val()},"        +
             "contentType: 'application/json',"      +
             "headers: {'Authorization': 'Bearer ' + $('#tokenField').val() },"+
             "async: false" +
             "   })"        +
     " });");
   
    */	        
     return buf.toString();
    }
    
    @POST
    @Produces(value="text/html")
    @Path("/cachettl")
    public String handlePost(@FormParam("ttl") int ttl ) {
    	
    	//to do: validate for int
    	int old_ttl = cache_ttl;
    	cache_ttl = ttl;
    	
    	return "<html><body><p>oldvalue: " +  old_ttl + "<p>newValue: "+ cache_ttl;
    }
    
   
    private String assembleHtmlform(List<String> entries){
    	
    	
    	//"<div id='wrap'>"
    	
    	StringBuffer buf = new StringBuffer();
    	
    //	buf.append("<script src='list.js'></script>");
    //	buf.append("\n <script type='text/javascript' src='/apiroxy/list.js'>");
    	
    	//var head = document.getElementByTagName('head')[0];
    //	var s = document.createElement('style');
    	//<link rel="stylesheet" href="listbox.css"> || document.getElementByTagName('head')[0]
    	//buf.append("\n var head = document.head; ");
    	buf.append("\n <script>");
    	buf.append("\n var link = document.createElement('link'); ");
    	buf.append("\n link.id = 'cssId'; ");
    	buf.append("\n link.rel='stylesheet';  ");
    	buf.append("\n link.type='text/css'; ");
        buf.append("\n link.href='listbox.css'; ");
    	buf.append("\n link.media='screen, print'; ");
    //	buf.append("\n link.text = rel='stylesheet' href='listbox.css'");
    	buf.append("\n document.getElementsByTagName('head')[0].appendChild(link); ");
    	buf.append("\n </script>");
    	
    //	buf.append("\n head.appendChild(link); ");
    	
    	// + document.getElementByTagName('head')[0].innerHTML
    	//buf.append(" alert('header content: ' + head.innerHTML)");
    	
    	
    	
    	buf.append("\n <p>MASTER/DETAIL VIEW</p>");
    	buf.append("<div class='listbox-area'>");
    	buf.append("<div class='row'>");
    	buf.append("<div class='column1'>");
    	buf.append("<span id='ss_elem' class='listbox-label'>Search by:symbol/algorithm</span>");
    	buf.append("<input type='text' id='fname' name='fname'>");
    	buf.append("<ul id='ss_elem_list' tabindex='0' role='listbox' aria-labelledby='ss_elem'>");
    	
    	entries.forEach(entry -> buf.append("<li id='ss_elem_Np' role='option'>"+entry+"</li>"));
    	
    	buf.append(" </ul>");
    	buf.append("</div>");
    	buf.append("</div>");
    	buf.append("<div>DETAILS:</div>");   
    	buf.append("</div>");
    	
    //	buf.append("</div>");  // wrap
    	
    	return buf.toString();
    }
    
    
    // returns all coin names
    private List<String> getCoinNames(){
 	   
 	   List<String> names = new LinkedList<String>();
 	   
 	   coinMap.values().forEach(coin -> names.add(coin.getCoinName()+ "=" + coin.getSymbol()));
 	   
 	 //  System.out.println("names" + coinMap.size());
 	   
 	   return names;
    }
    
    //returns coins with desired algorithm
    private List<Coin> getCoinsForAlgorithm(String algorithm) {
    
 	  return
 	   coinMap.values().stream()
 	          .filter(coin -> coin.getAlgorithm().equalsIgnoreCase(algorithm)).collect(Collectors.toList());
    }
    
    //returns coins with desired symbol
    private List<Coin> getCoinsForSymbols(String symbolString) {
 	   
 	  List<String> symbols = Arrays.asList(symbolString.split(","));
 	   
      return
 		 coinMap.values().stream()
 	            .filter(coin -> symbols.contains(coin.getSymbol())).collect(Collectors.toList());

    }
    
    
   //returns single coin with all properties, including currency
   private Coin getSingleCoinForSymbol(String symbol){
 	 
 	  String[] str = new String[1];
 	  str[0]=symbol;
 	  
 	  try {
 	     populateToUSDField(str);
 	  } catch(Exception ex){
      	System.out.println("getSingleCoinForSymbol exception ---> " + ex.getMessage());
      }
 	  
 	  return coinMap.get(symbol);
    }
   
   
    private static void populateCoinMap() throws Exception {   
		   
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
    
    
    private void populateToUSDField(String[] symbols) throws Exception{
 	   
    	
 	    StringBuffer buf = new StringBuffer("https://min-api.cryptocompare.com/data/pricemulti?fsyms=");
 	    JSONObject obj;
        String url;
        
        for(int i=0; i < symbols.length; i++){
     	   
           if(i > 0)  buf.append(",");
        	  
     	   buf.append(symbols[i]);
        } 
     	   // populate toUSD fields in bulk
          //  if(buf.length() > 300 || i==symbols.length-1){
         	   buf.append("&tsyms= USD");
         	   
         	   url = buf.toString();
         	   
               // api call
               obj = new JSONObject(callREST(url, "GET"));
                
               for(String symbol : symbols) {
            	   
            	   System.out.println("-------------------->" + obj.keys().toString());
            	   
            	   if(obj.get(symbol) != null) {
            		   
            		   
                 	  
                    //if( obj.keys().toString().contains(symbol) ) {
                 	   
                 	   coinMap.get(symbol).setToUsd(obj.getJSONObject(symbol).getLong("USD"));
                 	   //.getLong("USD")
                 	   System.out.println("toUsd----->" + obj.getJSONObject(symbol).getLong("USD"));
                 	   
                    }   
                           
                //reset buffer        
               // buf = new StringBuffer("https://min-api.cryptocompare.com/data/pricemulti?fsyms=");
                
           }
      
    }
    
    //call to rest api
    private static String callREST(String url, String method){

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

}

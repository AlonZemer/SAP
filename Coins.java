

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * Servlet implementation class Coins
 */
@WebServlet(urlPatterns = {"/coins","/coins/:"})

public class Coins extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private CoinService service = null;
	
	
    public Coins() {
    	
        super();
        
        //start service on load
        service = new CoinService();
    
        
    }
    
    
	/**
	 *  service get request
	 * @param <T>
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		Map<String, String[]> pMap = request.getParameterMap();
		
		if(pMap.isEmpty()) {
		   
		   System.out.println("service.getCoinNames()");
		   
           // list all coin names	
		   this.service.getCoinNames().stream().forEach(System.out::println);
			
	     } else {
		   
		     String[] values;
			
			 if(pMap.containsKey("symbol")){
				
				values = pMap.get("symbol");
				 
				service.getCoinsForSymbols(values[0]).stream().forEach(System.out::println);
				 
			 } else if( pMap.containsKey("algorithm") ){
			
				values = pMap.get("algorithm");
				
				System.out.println("VALUES " + values[0]);
				 
				service.getCoinsForAlgorithm(values[0]).stream().forEach(System.out::println);
			 }
			
		}
		
		
		
		
		//response.getWriter().append("Served at: " +);   //.append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

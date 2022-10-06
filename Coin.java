
public class Coin {
	
	
	       int    Id;
	       String symbol;
	       String coinName;
	       String algorithm;
	       double  toUsd;

	    public Coin(int Id, String symbol, String coinName, String algorithm){
	    	
	    	this.Id=Id;
		    this.symbol=symbol;
		    this.coinName=coinName;
		    this.algorithm=algorithm;
	    }
	    
	    public Coin(){}

		public int getId() {
			return Id;
		}

		public void setId(int id) {
			Id = id;
		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}

		public String getCoinName() {
			return coinName;
		}

		public void setCoinName(String coinName) {
			this.coinName = coinName;
		}

		public String getAlgorithm() {
			return algorithm;
		}

		public void setAlgorithm(String algorithm) {
			this.algorithm = algorithm;
		}

		public double getToUsd() {
			return toUsd;
		}

		public void setToUsd(float toUsd) {
			this.toUsd = toUsd;
		}
}



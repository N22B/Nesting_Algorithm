package share;



public class PieceVirtual 
{
	public int[] coordX;  
	public int[] coordY;
	private int vertices;  
	private int area;
	private int xmin;
	private int xmax;
	private int ymin;
	private int ymax;
	
	public PieceVirtual(int[] coordenadas){
		this.vertices = coordenadas.length/2;
		int n = this.vertices;
		this.coordX = new int[n];
		this.coordY = new int[n];
		for(int i=0;i<n*2;i+=2){
			this.coordX[i/2]=coordenadas[i];
			this.coordY[i/2]=coordenadas[i+1];
		}
		this.xmax = ArrayOperations.Mayor(coordX);      
		this.ymax = ArrayOperations.Mayor(coordY);
		this.xmin = ArrayOperations.Menor(coordX);   
		this.ymin = ArrayOperations.Menor(coordY);
		this.area = this.calculaArea();
	}

	public int getvertices(){   
		return vertices;
	}

	public int getxsize(){
		return xmax - xmin;
	}

	public int getysize(){
		return ymax - ymin;
	}

	public int getTotalSize(){
		return area;
	}

	public int getXmin(){
		return ArrayOperations.Menor(this.coordX);
	}

	public int getXmax(){
		return ArrayOperations.Mayor(this.coordX);
	}

	public int getYmin(){
		return ArrayOperations.Menor(this.coordY);
	}

	public int getYmax(){
		return ArrayOperations.Mayor(this.coordY);  
	}

	private int calculaArea(){
		int n = this.vertices;
		int suma = 0;
		for(int i=0;i<n-1;i++) {
			suma+=this.coordX[i]*this.coordY[i+1]-
			this.coordY[i]*this.coordX[i+1];
		}
		int i=n-1;
		suma+=this.coordX[i]*this.coordY[0]-
		this.coordY[i]*this.coordX[0];
		suma = Math.abs(suma)/2;
		return suma;
	}
	
	
}
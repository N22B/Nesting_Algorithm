package share;

import java.util.*;
import static java.lang.Math.*;


public class Sheet implements Comparable<Sheet>
{
	private int ancho;
	private int alto;
	private int numero_Objeto;  // 底板编号
	private int arealibre;      // free area
	int areaocupada;            // no free area
	private List<Piece> listaPiezas;
	private static List<int[]> posiciones;
	private static List<Double> angulos;
	private int Nopiezas;  //已经放置的零件个数


	public Sheet(int XSize, int YSize, int NoObjeto){			
		ancho=XSize;
		alto=YSize;
		arealibre=gettotalsize();
		areaocupada = 0;
		numero_Objeto = NoObjeto;
		listaPiezas = new LinkedList<Piece>();
		posiciones = new LinkedList<int[]>();
		angulos = new LinkedList<Double>();
		Nopiezas = 0;

		// ???????
		int[] posicionA = {0, 0};
		int[] posicionB = {ancho, 0};
		int[] posicionC = {ancho, alto}; 
		int[] posicionD = {0, alto}; 
		posiciones.add(posicionA);
		posiciones.add(posicionB);
		posiciones.add(posicionC);
		posiciones.add(posicionD);

		angulos.add((double)0);
		angulos.add((double)90);
		angulos.add((double)180);
		angulos.add((double)270); 
	}

	// X max of the object  
	public int getXmax(){
		return ancho;
	}

	// Y max of the object  
	public int getYmax(){
		return alto;
	}

	// From all pieces inside the object, the maximum X coordinate.  
	public int getMaximaX(){
		int numpzas = listaPiezas.size();
		if(numpzas==0)
			return 0;
		Piece pza = (Piece)listaPiezas.get(0);
		int maximo = pza.getXmax();
		for (int i = 1; i < numpzas; i++){
			pza = (Piece)listaPiezas.get(i);
			int maxtemp = pza.getXmax();
			if(maxtemp>maximo)
				maximo=maxtemp;
		}
		return maximo;
	}

	// From all pieces inside the object, the maximum Y coordinate.    
	public int getMaximaY(){
		int numpzas = listaPiezas.size();
		if(numpzas==0)
			return 0;
		Piece pza  = (Piece)listaPiezas.get(0);
		int maximo = pza.getYmax();
		for (int i = 1; i < numpzas; i++){
			pza = (Piece)listaPiezas.get(i);
			int maxtemp = pza.getYmax();
			if(maxtemp>maximo)
				maximo=maxtemp;
		}
		return maximo;
	}   

	public int gettotalsize(){
		return (ancho*alto);
	}

	public void addPieza(Piece pieza){
		arealibre -= pieza.getTotalSize();
		listaPiezas.add(pieza);
		areaocupada+= pieza.getTotalSize();
		Nopiezas += 1;
		addPosiciones(pieza);
		addAngulos(pieza);
	}


	public void addPreliminarPieza(Piece pieza){
		listaPiezas.add(pieza);
	}


	// Removes a preliminary piece
	// ????
	public void removePreliminarPieza(Piece pieza){
		listaPiezas.remove(pieza);
	}


	private void addPosiciones(Piece pieza)
	{
		int[] posicion1 = {0, pieza.getYmax()}; 
		int[] posicion2 = {pieza.getXmin(),pieza.getYmax()}; 
		int[] posicion3 = {pieza.getXmax(),pieza.getYmax()}; 
		int[] posicion4 = {pieza.getXmax(),pieza.getYmin()}; 
		int[] posicion5 = {pieza.getXmax(),0};

		if (!revisar(posicion1))
			posiciones.add(posicion1);
		if (!revisar(posicion2))
			posiciones.add(posicion2);
		if (!revisar(posicion3))
			posiciones.add(posicion3);
		if (!revisar(posicion4))
			posiciones.add(posicion4);
		if (!revisar(posicion5))
			posiciones.add(posicion5);

		// ??????????????????????????????
		for(int i = 0; i < pieza.getvertices(); i++){
			int[] point = {pieza.coordX[i], pieza.coordY[i]};
			if (!revisar(point))
				posiciones.add(point);
		}
	}


	// This is useful for implementing rotation schemes
	// ????
	private void addAngulos(Piece pieza)
	{
		int n = pieza.getvertices();
		for(int i=0;i<n-1;i++){
			double angulon = atan( (double)(pieza.coordY[i+1]-pieza.coordY[i])/(double)(pieza.coordX[i+1]-pieza.coordX[i]) );
			angulon = Math.toDegrees(angulon);
			angulon = angulo0a360(angulon);
			if(!revisarAngulo(angulon)){
				angulos.add(angulon);
				double angulonc = anguloComplemento(angulon);
				angulos.add(angulonc);
			}
		}
		double angulofinal = atan( (double)(pieza.coordY[0]-pieza.coordY[n-1])/(double)(pieza.coordX[0]-pieza.coordX[n-1]) );
		angulofinal = Math.toDegrees(angulofinal);
		angulofinal = angulo0a360(angulofinal);
		if(!revisarAngulo(angulofinal)){
			angulos.add(angulofinal);
			double angulonc = anguloComplemento(angulofinal);
			angulos.add(angulonc);
		}
	}


	public int getFreeArea()
	{
		return arealibre;
	}

	public int getUsedArea()
	{
		return areaocupada;
	}

	public int getNoPiezas()
	{
		return Nopiezas;
	}

	public int getNumObjeto()
	{
		return numero_Objeto;
	}

	public List<Piece> getPzasInside()
	{
		return listaPiezas;
	}


	public List<int[]> getPosiciones()
	{	
		return posiciones;
	}

	public List<Double> getAngulos()
	{	
		return angulos;
	}


	private static boolean revisar(int[] posicion)
	{
		if (posiciones.isEmpty())
			return false;
		for (int i = posiciones.size()-1; i >= 0; i--)
		{
			int[] temp = new int[2];
			temp = (int[])(posiciones.get(i));
			if(posicion[0] == temp[0] && posicion[1] == temp[1])
				return true;
		} 
		return false;
	} 

	private static boolean revisarAngulo(double angulo)
	{
		if (angulos.isEmpty())
			return false;
		for (int i = angulos.size()-1; i >= 0; i--){
			double temp = (double)angulos.get(i);
			if(angulo == temp)
				return true;
		} 
		return false;
	} 

	private static double angulo0a360(double angulo)
	{
		while(angulo<0)
			angulo += 360;
		while(angulo>=360)
			angulo -= 360;
		return angulo;
	}

	private static double anguloComplemento(double angulo)
	{
		if(angulo<180)
			angulo += 180;
		else
			angulo -= 180;
		return angulo;
	}


	// ??????????
	@Override
	public int compareTo(Sheet o) {
		double area0 = (double)this.getUsedArea()/(double)this.gettotalsize();
		double area1 = (double)o.getUsedArea()/(double)o.gettotalsize();
		if(area0 > area1 ){
			return 1;
		}
		else if(area0 == area1){
			return 0;
		}
		return -1;
	}
}



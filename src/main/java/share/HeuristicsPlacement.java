package share;

import java.util.List;
import java.util.LinkedList;
import java.util.Random;

import static java.lang.Math.*;


public class HeuristicsPlacement{

	public HeuristicsPlacement(){}

	public boolean HAcomodo(Sheet objeto, Piece pieza, String Heur_Acom)
	{
		boolean acomodo=false;
		if(Heur_Acom.equals("BL"))
			acomodo = BLHeuristic(objeto, pieza);
		else if(Heur_Acom.equals("EC"))
			acomodo = ECHeuristic(objeto, pieza);
		else if(Heur_Acom.equals("EC2"))
			acomodo = EC2Heuristic(objeto, pieza);  
		else if(Heur_Acom.equals("MA"))
			acomodo = MAHeuristic(objeto, pieza);
		else if(Heur_Acom.equals("MABR"))
			acomodo = MAHeuristicBR(objeto, pieza);
		else if(Heur_Acom.equals("MALBR"))
			acomodo = MAHeuristicLBR(objeto, pieza);

		return acomodo;
	}


	private boolean BLHeuristic(Sheet objeto, Piece pieza)
	{

		pieza.moveToXY( objeto.getXmax(), objeto.getYmax(), 1);
		
		this.runIntoBottomLeftPlacement(objeto, pieza); 
	    
		if(pieza.getYmax() <= objeto.getYmax() && pieza.getXmax() <= objeto.getXmax()&& pieza.getXmin()>=0 && pieza.getYmin()>=0) 
		{
			if( posicionValida(objeto, pieza) )
			{
				return true;
			}
		}
		return false;
	}



	  
	private boolean ECHeuristic(Sheet objeto, Piece pieza)
	{
		boolean value;
		int[] posicion = new int[2]; 
		double angulo;
		double anguloPrevio = 0;
		double mejorRotacion = 0;

		int mejorX = objeto.getXmax();   
		int mejorY = objeto.getYmax();  

		List<Piece> listapzas = new LinkedList<Piece>();
		List<int[]> listapuntos = new LinkedList<int[]>();
		List<Double> rotaciones = new LinkedList<Double>();
		int X;
		int Y;

		listapzas = objeto.getPzasInside();
		listapuntos = objeto.getPosiciones();


		rotaciones = rotacionesAProbar4();  

		if (listapzas.isEmpty())
		{
			for (int i = 0; i < rotaciones.size(); i++)			{
				angulo = (double)rotaciones.get(i);
				if(i == 0)   
					pieza.rotate(angulo);
				else
					pieza.rotate(angulo-anguloPrevio);
				value = BLHeuristic(objeto, pieza);
				if( value ){
					X = pieza.getXmin();
					Y = pieza.getYmin();
					if(X < mejorX){
						mejorX = X;
						mejorY = Y;
						mejorRotacion = angulo;
					}
					if(X == mejorX && Y < mejorY){
						mejorY = Y;
						mejorRotacion = angulo;
					}
				}
				anguloPrevio = angulo;
			}

			if(mejorX == objeto.getXmax() && mejorY == objeto.getYmax())
			{
				return false;
			}

			pieza.desRotar();
			pieza.rotate(mejorRotacion);
			pieza.moveToXY(mejorX, mejorY, 2);
			return true;	
		} 



		for (int i = 0; i < rotaciones.size(); i++)
		{
			angulo = (double)rotaciones.get(i);
			if(i== 0)   
				pieza.rotate(angulo);
			else
				pieza.rotate(angulo-anguloPrevio);
			for (int j = 0; j < listapuntos.size(); j++){
				posicion = (int[])listapuntos.get(j);
				pieza.moveToXY(posicion[0], posicion[1], 2);
				runIntoBottomLeftPlacement(objeto, pieza);

				if( posicionValida(objeto, pieza) == false) 
					continue;

				X = pieza.getXmin();
				Y = pieza.getYmin();

				if(X < mejorX){
					mejorX = X;
					mejorY = Y;
					mejorRotacion = angulo;
					continue;
				}
				if(X == mejorX && Y < mejorY){
					mejorY = Y;
					mejorRotacion = angulo;
				}
			}
			anguloPrevio = angulo;
		}
		if(mejorX == objeto.getXmax() && mejorY == objeto.getYmax())
		{
			return false;
		}

		pieza.desRotar();
		pieza.rotate(mejorRotacion);
		pieza.moveToXY(mejorX, mejorY, 2);
		return true;	
	}


	private boolean EC2Heuristic(Sheet objeto, Piece pieza)
	{  
		boolean value;
		int[] posicion = new int[2]; 
		double angulo;
		double anguloPrevio = 0;


		int menorAreaOcupada = objeto.getXmax() * objeto.getYmax() + 1;
		int mejorX = objeto.getXmax();   
		int mejorY = objeto.getYmax(); 
		double mejorRotacion = 0;

		List<Piece> listapzas = new LinkedList<Piece>();
		List<int[]> listapuntos = new LinkedList<int[]>();
		List<Double> rotaciones = new LinkedList<Double>();
		int X;
		int Y;

		listapzas = objeto.getPzasInside();
		listapuntos = objeto.getPosiciones();

		rotaciones = rotacionesAProbar4();  



		if (listapzas.isEmpty())
		{
			for (int i = 0; i < rotaciones.size(); i++)
			{
				angulo = (double)rotaciones.get(i);

				if(i == 0)   
				{ 
					pieza.rotate(angulo);
				}else
				{
					pieza.rotate(angulo-anguloPrevio);
				}

				value = BLHeuristic(objeto, pieza);

				if( value ) 
				{
					X = pieza.getXmax();      
					Y = pieza.getYmax();      
					if(X*Y < menorAreaOcupada)
					{
						mejorX = pieza.getXmin();
						mejorY = pieza.getYmin();
						menorAreaOcupada = X*Y;
						mejorRotacion = angulo;
					}
				}

				anguloPrevio = angulo;

			}


			if(mejorX == objeto.getXmax() && mejorY == objeto.getYmax())
			{
				return false;
			}

			pieza.desRotar();
			pieza.rotate(mejorRotacion);
			pieza.moveToXY(mejorX, mejorY, 2);
			return true;	
		}  




		for (int i = 0; i < rotaciones.size(); i++)
		{   
			angulo = (double)rotaciones.get(i);

			if(i== 0)   
			{ 
				pieza.rotate(angulo);
			}else
			{
				pieza.rotate(angulo-anguloPrevio);
			}

			for (int j = 0; j < listapuntos.size(); j++)
			{
				posicion = (int[])listapuntos.get(j);
				pieza.moveToXY(posicion[0], posicion[1], 2);

				runIntoBottomLeftPlacement(objeto, pieza);

				if( posicionValida(objeto, pieza) ) 
				{
					X = max(pieza.getXmax(), objeto.getMaximaX() );
					Y = max(pieza.getYmax(), objeto.getMaximaY() );
					if(X*Y < menorAreaOcupada)
					{
						mejorX = pieza.getXmin();
						mejorY = pieza.getYmin();
						menorAreaOcupada = X*Y;
						mejorRotacion = angulo;
					}
				}
			}

			anguloPrevio = angulo;

		}


		if(mejorX == objeto.getXmax() && mejorY == objeto.getYmax())
		{
			return false;
		}

		pieza.desRotar();
		pieza.rotate(mejorRotacion);
		pieza.moveToXY(mejorX, mejorY, 2);
		return true;	
	}



	
	private boolean MAHeuristic(Sheet objeto, Piece pieza)
	{
		boolean temp;
		int[] posicion = new int[2]; 
		int mejorAdyacencia = 0;
		int adyacencia = 0;
		double angulo;
		double anguloPrevio = 0;


		int mejorX = objeto.getXmax();
		int mejorY = objeto.getYmax();  
		double mejorRotacion = 0;

		List<int[]> listapuntos = new LinkedList<int[]>();
		List<Double> rotaciones = new LinkedList<Double>();
		int X;
		int Y;
		listapuntos = objeto.getPosiciones();


		rotaciones = rotacionesAProbar4();  


		for (int i = 0; i < rotaciones.size(); i++)
		{
			angulo = (double)rotaciones.get(i);

			if(i== 0)   
			{ 
				pieza.rotate(angulo);
			}else
			{
				pieza.rotate(angulo-anguloPrevio);
			}

			for (int j = 0; j < listapuntos.size(); j++)
			{
				posicion = (int[])listapuntos.get(j);


				if(j == 1)
				{
					pieza.moveToXY(posicion[0], posicion[1], 1);
				}else if (j == 2)
				{
					pieza.moveToXY(posicion[0], posicion[1], 3);
				}else if (j == 3)
				{
					pieza.moveToXY(posicion[0], posicion[1], 4);
				}else 
				{
					pieza.moveToXY(posicion[0], posicion[1], 2);
				}


				if(posicionValida(objeto, pieza))
				{
					adyacencia = adyacenciaOP(objeto, pieza);
					if( adyacencia == mejorAdyacencia) 
					{
						X = pieza.getXmin();      
						Y = pieza.getYmin();
						if(X < mejorX)
						{
							mejorX = X;
							mejorY = Y;
							mejorRotacion = angulo;
						}
						if(X == mejorX && Y < mejorY)
						{
							mejorY = Y;
							mejorRotacion = angulo;
						}
					}
					else if( adyacencia > mejorAdyacencia) 
					{
						mejorX = pieza.getXmin();      
						mejorY = pieza.getYmin();
						mejorAdyacencia = adyacencia;    
						mejorRotacion = angulo;
					}  
				}

				temp = this.runIntoBottomLeftPlacement(objeto, pieza);
				if(temp)
				{
					adyacencia = adyacenciaOP(objeto, pieza);   
					if(posicionValida(objeto, pieza))
					{
						if( adyacencia == mejorAdyacencia) 
						{
							X = pieza.getXmin();      
							Y = pieza.getYmin();
							if(X < mejorX)
							{
								mejorX = X;
								mejorY = Y;
								mejorRotacion = angulo;
							}
							if(X == mejorX && Y < mejorY)
							{
								mejorY = Y;
								mejorRotacion = angulo;
							}
						}
						else if( adyacencia > mejorAdyacencia) 
						{
							mejorX = pieza.getXmin();      
							mejorY = pieza.getYmin();
							mejorAdyacencia = adyacencia;
							mejorRotacion = angulo;
						}  
					}
				}
			}

			anguloPrevio = angulo;

		}


		if(mejorX == objeto.getXmax() && mejorY == objeto.getYmax())
		{
			return false;
		}

		pieza.desRotar();
		pieza.rotate(mejorRotacion);
		pieza.moveToXY(mejorX, mejorY, 2);
		return true;	
	}

	
	private boolean MAHeuristicBR(Sheet objeto, Piece pieza)
	{
		boolean temp;
		int[] posicion = new int[2];
		int mejorAdyacencia = 0;
		int adyacencia = 0;
		double angulo;
		double anguloPrevio = 0;


		int mejorX = 0;
		int mejorY = objeto.getYmax();
		double mejorRotacion = 0;

		List<int[]> listapuntos = new LinkedList<int[]>();
		List<Double> rotaciones = new LinkedList<Double>();
		int X;
		int Y;


		listapuntos = objeto.getPosiciones();

		rotaciones = rotacionesAProbar4();


		for (int i = 0; i < rotaciones.size(); i++)
		{
			angulo = (double)rotaciones.get(i);

			if(i == 0)
			{
				pieza.rotate(angulo);
			}else
			{
				pieza.rotate(angulo-anguloPrevio);
			}

			for (int j = 0; j < listapuntos.size(); j++)
			{
				posicion = (int[])listapuntos.get(j);







				if(j == 0)
				{
					pieza.moveToXY(posicion[0], posicion[1], 2);
				}else if (j == 2)
				{
					pieza.moveToXY(posicion[0], posicion[1], 3);
				}






				else if(j == 1){
					pieza.moveToXY(posicion[0], posicion[1], 1);
				}else if(j == 3){
					pieza.moveToXY(posicion[0], posicion[1], 4);
				}else{
					for(int ref = 1; ref <= 3; ref++){
						pieza.moveToXY(posicion[0], posicion[1], ref);
						if(posicionValida(objeto, pieza))
						{
							adyacencia = adyacenciaOP(objeto, pieza);
							if( adyacencia == mejorAdyacencia)
							{
								X = pieza.getXmax();
								Y = pieza.getYmin();
								if(X > mejorX)
								{
									mejorX = X;
									mejorY = Y;
									mejorRotacion = angulo;
								}
								if(X == mejorX && Y < mejorY)
								{
									mejorY = Y;
									mejorRotacion = angulo;
								}
							}
							else if( adyacencia > mejorAdyacencia)
							{
								mejorX = pieza.getXmax();
								mejorY = pieza.getYmin();
								mejorAdyacencia = adyacencia;
								mejorRotacion = angulo;
							}
						}

						temp = this.runIntoBottomRightPlacement(objeto, pieza);
						if(temp)
						{
							adyacencia = adyacenciaOP(objeto, pieza);
							if(posicionValida(objeto, pieza))
							{
								if( adyacencia == mejorAdyacencia)
								{
									X = pieza.getXmax();
									Y = pieza.getYmin();
									if(X > mejorX)
									{
										mejorX = X;
										mejorY = Y;
										mejorRotacion = angulo;
									}
									if(X == mejorX && Y < mejorY)
									{
										mejorY = Y;
										mejorRotacion = angulo;
									}
								}
								else if( adyacencia > mejorAdyacencia)
								{
									mejorX = pieza.getXmax();
									mejorY = pieza.getYmin();
									mejorAdyacencia = adyacencia;
									mejorRotacion = angulo;
								}
							}
						}
					}
					pieza.moveToXY(posicion[0], posicion[1], 4);
				}


				if(posicionValida(objeto, pieza))
				{
					adyacencia = adyacenciaOP(objeto, pieza);
					if( adyacencia == mejorAdyacencia)
					{

						X = pieza.getXmax();
						Y = pieza.getYmin();
						if(X > mejorX)
						{
							mejorX = X;
							mejorY = Y;
							mejorRotacion = angulo;
						}
						if(X == mejorX && Y < mejorY)
						{
							mejorY = Y;
							mejorRotacion = angulo;
						}
					}
					else if( adyacencia > mejorAdyacencia)
					{

						mejorX = pieza.getXmax();
						mejorY = pieza.getYmin();
						mejorAdyacencia = adyacencia;
						mejorRotacion = angulo;
					}
				}


				temp = this.runIntoBottomRightPlacement(objeto, pieza);
				if(temp)
				{
					adyacencia = adyacenciaOP(objeto, pieza);
					if(posicionValida(objeto, pieza))
					{
						if( adyacencia == mejorAdyacencia)
						{

							X = pieza.getXmax();
							Y = pieza.getYmin();
							if(X > mejorX)
							{
								mejorX = X;
								mejorY = Y;
								mejorRotacion = angulo;
							}
							if(X == mejorX && Y < mejorY)
							{
								mejorY = Y;
								mejorRotacion = angulo;
							}
						}
						else if( adyacencia > mejorAdyacencia)
						{

							mejorX = pieza.getXmax();
							mejorY = pieza.getYmin();
							mejorAdyacencia = adyacencia;
							mejorRotacion = angulo;
						}
					}
				}
			}

			anguloPrevio = angulo;

		}


		if(mejorX == 0 && mejorY == objeto.getYmax())
		{
			return false;
		}

		pieza.desRotar();
		pieza.rotate(mejorRotacion);

		pieza.moveToXY(mejorX, mejorY, 1);
		return true;
	}

	
	private boolean MAHeuristicLBR(Sheet objeto, Piece pieza)
	{
		boolean tempLeft;
		int[] posicion = new int[2];
		int mejorAdyacenciaLeft = 0;
		int adyacencia = 0;
		double angulo;
		double anguloPrevio = 0;
		boolean flagLeft = true;


		int mejorXLeft = objeto.getXmax();
		int mejorYLeft = objeto.getYmax();
		double mejorRotacionLeft = 0;

		List<int[]> listapuntos = new LinkedList<int[]>();
		List<Double> rotaciones = new LinkedList<Double>();
		int X;
		int Y;
		listapuntos = objeto.getPosiciones();


		rotaciones = rotacionesAProbar4();


		for (int i = 0; i < rotaciones.size(); i++)
		{
			angulo = (double)rotaciones.get(i);

			if(i== 0)
			{
				pieza.rotate(angulo);
			}else
			{
				pieza.rotate(angulo-anguloPrevio);
			}

			for (int j = 0; j < listapuntos.size(); j++)
			{
				posicion = (int[])listapuntos.get(j);


				if(j == 1)
				{
					pieza.moveToXY(posicion[0], posicion[1], 1);
				}else if (j == 2)
				{
					pieza.moveToXY(posicion[0], posicion[1], 3);
				}else if (j == 3)
				{
					pieza.moveToXY(posicion[0], posicion[1], 4);
				}else
				{
					pieza.moveToXY(posicion[0], posicion[1], 2);
				}


				if(posicionValida(objeto, pieza))
				{
					adyacencia = adyacenciaOP(objeto, pieza);
					if( adyacencia == mejorAdyacenciaLeft)
					{
						X = pieza.getXmin();
						Y = pieza.getYmin();
						if(X < mejorXLeft)
						{
							mejorXLeft = X;
							mejorYLeft = Y;
							mejorRotacionLeft = angulo;
						}
						if(X == mejorXLeft && Y < mejorYLeft)
						{
							mejorYLeft = Y;
							mejorRotacionLeft = angulo;
						}
					}
					else if( adyacencia > mejorAdyacenciaLeft)
					{
						mejorXLeft = pieza.getXmin();
						mejorYLeft = pieza.getYmin();
						mejorAdyacenciaLeft = adyacencia;
						mejorRotacionLeft = angulo;
					}
				}

				tempLeft = this.runIntoBottomLeftPlacement(objeto, pieza);
				if(tempLeft)
				{
					adyacencia = adyacenciaOP(objeto, pieza);
					if(posicionValida(objeto, pieza))
					{
						if( adyacencia == mejorAdyacenciaLeft)
						{
							X = pieza.getXmin();
							Y = pieza.getYmin();
							if(X < mejorXLeft)
							{
								mejorXLeft = X;
								mejorYLeft = Y;
								mejorRotacionLeft = angulo;
							}
							if(X == mejorXLeft && Y < mejorYLeft)
							{
								mejorYLeft = Y;
								mejorRotacionLeft = angulo;
							}
						}
						else if( adyacencia > mejorAdyacenciaLeft)
						{
							mejorXLeft = pieza.getXmin();
							mejorYLeft = pieza.getYmin();
							mejorAdyacenciaLeft = adyacencia;
							mejorRotacionLeft = angulo;
						}
					}
				}
			}

			anguloPrevio = angulo;

		}


		
		boolean temp;

		int mejorAdyacencia = 0;
		adyacencia = 0;

		anguloPrevio = 0;
		boolean flagRight = true;


		int mejorX = 0;
		int mejorY = objeto.getYmax();
		double mejorRotacion = 0;
		pieza.desRotar();







		listapuntos = objeto.getPosiciones();
		rotaciones = rotacionesAProbar4();


		for (int i = 0; i < rotaciones.size(); i++)
		{
			angulo = (double)rotaciones.get(i);

			if(i == 0)
			{
				pieza.rotate(angulo);
			}else
			{
				pieza.rotate(angulo-anguloPrevio);
			}

			for (int j = 0; j < listapuntos.size(); j++)
			{
				posicion = (int[])listapuntos.get(j);


				if(j == 0)
				{
					pieza.moveToXY(posicion[0], posicion[1], 2);
				}else if (j == 2)
				{
					pieza.moveToXY(posicion[0], posicion[1], 3);
				}
				else if(j == 1){
					pieza.moveToXY(posicion[0], posicion[1], 1);
				}else if(j == 3){
					pieza.moveToXY(posicion[0], posicion[1], 4);
				}else{
					for(int ref = 1; ref <= 3; ref++){
						pieza.moveToXY(posicion[0], posicion[1], ref);
						if(posicionValida(objeto, pieza))
						{
							adyacencia = adyacenciaOP(objeto, pieza);
							if( adyacencia == mejorAdyacencia)
							{
								X = pieza.getXmax();
								Y = pieza.getYmin();
								if(X > mejorX)
								{
									mejorX = X;
									mejorY = Y;
									mejorRotacion = angulo;
								}
								if(X == mejorX && Y < mejorY)
								{
									mejorY = Y;
									mejorRotacion = angulo;
								}
							}
							else if( adyacencia > mejorAdyacencia)
							{
								mejorX = pieza.getXmax();
								mejorY = pieza.getYmin();
								mejorAdyacencia = adyacencia;
								mejorRotacion = angulo;
							}
						}

						temp = this.runIntoBottomRightPlacement(objeto, pieza);
						if(temp)
						{
							adyacencia = adyacenciaOP(objeto, pieza);
							if(posicionValida(objeto, pieza))
							{
								if( adyacencia == mejorAdyacencia)
								{
									X = pieza.getXmax();
									Y = pieza.getYmin();
									if(X > mejorX)
									{
										mejorX = X;
										mejorY = Y;
										mejorRotacion = angulo;
									}
									if(X == mejorX && Y < mejorY)
									{
										mejorY = Y;
										mejorRotacion = angulo;
									}
								}
								else if( adyacencia > mejorAdyacencia)
								{
									mejorX = pieza.getXmax();
									mejorY = pieza.getYmin();
									mejorAdyacencia = adyacencia;
									mejorRotacion = angulo;
								}
							}
						}
					}
					pieza.moveToXY(posicion[0], posicion[1], 4);
				}


				if(posicionValida(objeto, pieza))
				{
					adyacencia = adyacenciaOP(objeto, pieza);
					if( adyacencia == mejorAdyacencia)
					{
						X = pieza.getXmax();
						Y = pieza.getYmin();
						if(X > mejorX)
						{
							mejorX = X;
							mejorY = Y;
							mejorRotacion = angulo;
						}
						if(X == mejorX && Y < mejorY)
						{
							mejorY = Y;
							mejorRotacion = angulo;
						}
					}
					else if( adyacencia > mejorAdyacencia)
					{
						mejorX = pieza.getXmax();
						mejorY = pieza.getYmin();
						mejorAdyacencia = adyacencia;
						mejorRotacion = angulo;
					}
				}

				temp = this.runIntoBottomRightPlacement(objeto, pieza);
				if(temp)
				{
					adyacencia = adyacenciaOP(objeto, pieza);
					if(posicionValida(objeto, pieza))
					{
						if( adyacencia == mejorAdyacencia)
						{
							X = pieza.getXmax();
							Y = pieza.getYmin();
							if(X > mejorX)
							{
								mejorX = X;
								mejorY = Y;
								mejorRotacion = angulo;
							}
							if(X == mejorX && Y < mejorY)
							{
								mejorY = Y;
								mejorRotacion = angulo;
							}
						}
						else if( adyacencia > mejorAdyacencia)
						{
							mejorX = pieza.getXmax();
							mejorY = pieza.getYmin();
							mejorAdyacencia = adyacencia;
							mejorRotacion = angulo;
						}
					}
				}
			}

			anguloPrevio = angulo;

		}


		if(mejorXLeft == objeto.getXmax() && mejorYLeft == objeto.getYmax() && mejorX == 0 && mejorY == objeto.getYmax())
		{
			return false;
		}


		if(mejorAdyacenciaLeft < mejorAdyacencia){
			flagLeft = false;
		}

		if(mejorAdyacenciaLeft == mejorAdyacencia){
			Random random = new Random();
			if(random.nextDouble() >= 0.5){
				flagLeft = false;
			}
		}


		if(flagLeft){
			pieza.desRotar();
			pieza.rotate(mejorRotacionLeft);
			pieza.moveToXY(mejorXLeft, mejorYLeft, 2);
		}else{
			pieza.desRotar();
			pieza.rotate(mejorRotacion);
			pieza.moveToXY(mejorX, mejorY, 1);
		}


		if(!posicionValida(objeto, pieza)){
			return false;
		}

		return true;
	}


	
	private boolean backupLBR(Sheet objeto, Piece pieza){
			boolean tempLeft;
			int[] posicion = new int[2];
			int mejorAdyacenciaLeft = 0;
			int adyacencia = 0;
			double angulo;
			double anguloPrevio = 0;
			boolean flagLeft = true;


			int mejorXLeft = objeto.getXmax();
			int mejorYLeft = objeto.getYmax();
			double mejorRotacionLeft = 0;

			List<int[]> listapuntos = new LinkedList<int[]>();
			List<Double> rotaciones = new LinkedList<Double>();
			int X;
			int Y;
			listapuntos = objeto.getPosiciones();


			rotaciones = rotacionesAProbar4();


			for (int i = 0; i < rotaciones.size(); i++)
			{
				angulo = (double)rotaciones.get(i);

				if(i== 0)
				{
					pieza.rotate(angulo);
				}else
				{
					pieza.rotate(angulo-anguloPrevio);
				}

				for (int j = 0; j < listapuntos.size(); j++)
				{
					posicion = (int[])listapuntos.get(j);


					if(j == 1)
					{
						pieza.moveToXY(posicion[0], posicion[1], 1);
					}else if (j == 2)
					{
						pieza.moveToXY(posicion[0], posicion[1], 3);
					}else if (j == 3)
					{
						pieza.moveToXY(posicion[0], posicion[1], 4);
					}else if(j == 0){
						pieza.moveToXY(posicion[0], posicion[1], 2);
					}
					else
					{
						for(int ref = 1; ref <=4; ref++){
							if(ref == 2){
								continue;
							}
							pieza.moveToXY(posicion[0], posicion[1], ref);
							if(posicionValida(objeto, pieza))
							{
								adyacencia = adyacenciaOP(objeto, pieza);
								if( adyacencia == mejorAdyacenciaLeft)
								{
									X = pieza.getXmin();
									Y = pieza.getYmin();
									if(X < mejorXLeft)
									{
										mejorXLeft = X;
										mejorYLeft = Y;
										mejorRotacionLeft = angulo;
									}
									if(X == mejorXLeft && Y < mejorYLeft)
									{
										mejorYLeft = Y;
										mejorRotacionLeft = angulo;
									}
								}
								else if( adyacencia > mejorAdyacenciaLeft)
								{
									mejorXLeft = pieza.getXmin();
									mejorYLeft = pieza.getYmin();
									mejorAdyacenciaLeft = adyacencia;
									mejorRotacionLeft = angulo;
								}
							}

							tempLeft = this.runIntoBottomLeftPlacement(objeto, pieza);
							if(tempLeft)
							{
								adyacencia = adyacenciaOP(objeto, pieza);
								if(posicionValida(objeto, pieza))
								{
									if( adyacencia == mejorAdyacenciaLeft)
									{
										X = pieza.getXmin();
										Y = pieza.getYmin();
										if(X < mejorXLeft)
										{
											mejorXLeft = X;
											mejorYLeft = Y;
											mejorRotacionLeft = angulo;
										}
										if(X == mejorXLeft && Y < mejorYLeft)
										{
											mejorYLeft = Y;
											mejorRotacionLeft = angulo;
										}
									}
									else if( adyacencia > mejorAdyacenciaLeft)
									{
										mejorXLeft = pieza.getXmin();
										mejorYLeft = pieza.getYmin();
										mejorAdyacenciaLeft = adyacencia;
										mejorRotacionLeft = angulo;
									}
								}
							}
						}
						pieza.moveToXY(posicion[0], posicion[1], 2);
					}


					if(posicionValida(objeto, pieza))
					{
						adyacencia = adyacenciaOP(objeto, pieza);
						if( adyacencia == mejorAdyacenciaLeft)
						{
							X = pieza.getXmin();
							Y = pieza.getYmin();
							if(X < mejorXLeft)
							{
								mejorXLeft = X;
								mejorYLeft = Y;
								mejorRotacionLeft = angulo;
							}
							if(X == mejorXLeft && Y < mejorYLeft)
							{
								mejorYLeft = Y;
								mejorRotacionLeft = angulo;
							}
						}
						else if( adyacencia > mejorAdyacenciaLeft)
						{
							mejorXLeft = pieza.getXmin();
							mejorYLeft = pieza.getYmin();
							mejorAdyacenciaLeft = adyacencia;
							mejorRotacionLeft = angulo;
						}
					}

					tempLeft = this.runIntoBottomLeftPlacement(objeto, pieza);
					if(tempLeft)
					{
						adyacencia = adyacenciaOP(objeto, pieza);
						if(posicionValida(objeto, pieza))
						{
							if( adyacencia == mejorAdyacenciaLeft)
							{
								X = pieza.getXmin();
								Y = pieza.getYmin();
								if(X < mejorXLeft)
								{
									mejorXLeft = X;
									mejorYLeft = Y;
									mejorRotacionLeft = angulo;
								}
								if(X == mejorXLeft && Y < mejorYLeft)
								{
									mejorYLeft = Y;
									mejorRotacionLeft = angulo;
								}
							}
							else if( adyacencia > mejorAdyacenciaLeft)
							{
								mejorXLeft = pieza.getXmin();
								mejorYLeft = pieza.getYmin();
								mejorAdyacenciaLeft = adyacencia;
								mejorRotacionLeft = angulo;
							}
						}
					}
				}

				anguloPrevio = angulo;

			}


			
			boolean temp;

			int mejorAdyacencia = 0;
			adyacencia = 0;

			anguloPrevio = 0;
			boolean flagRight = true;


			int mejorX = 0;
			int mejorY = objeto.getYmax();
			double mejorRotacion = 0;
			pieza.desRotar();







			listapuntos = objeto.getPosiciones();
			rotaciones = rotacionesAProbar4();


			for (int i = 0; i < rotaciones.size(); i++)
			{
				angulo = (double)rotaciones.get(i);

				if(i == 0)
				{
					pieza.rotate(angulo);
				}else
				{
					pieza.rotate(angulo-anguloPrevio);
				}

				for (int j = 0; j < listapuntos.size(); j++)
				{
					posicion = (int[])listapuntos.get(j);


					if(j == 0)
					{
						pieza.moveToXY(posicion[0], posicion[1], 2);
					}else if (j == 2)
					{
						pieza.moveToXY(posicion[0], posicion[1], 3);
					}
					else if(j == 1){
						pieza.moveToXY(posicion[0], posicion[1], 1);
					}else if(j == 3){
						pieza.moveToXY(posicion[0], posicion[1], 4);
					}else{
						for(int ref = 1; ref <= 3; ref++){
							pieza.moveToXY(posicion[0], posicion[1], ref);
							if(posicionValida(objeto, pieza))
							{
								adyacencia = adyacenciaOP(objeto, pieza);
								if( adyacencia == mejorAdyacencia)
								{
									X = pieza.getXmax();
									Y = pieza.getYmin();
									if(X > mejorX)
									{
										mejorX = X;
										mejorY = Y;
										mejorRotacion = angulo;
									}
									if(X == mejorX && Y < mejorY)
									{
										mejorY = Y;
										mejorRotacion = angulo;
									}
								}
								else if( adyacencia > mejorAdyacencia)
								{
									mejorX = pieza.getXmax();
									mejorY = pieza.getYmin();
									mejorAdyacencia = adyacencia;
									mejorRotacion = angulo;
								}
							}

							temp = this.runIntoBottomRightPlacement(objeto, pieza);
							if(temp)
							{
								adyacencia = adyacenciaOP(objeto, pieza);
								if(posicionValida(objeto, pieza))
								{
									if( adyacencia == mejorAdyacencia)
									{
										X = pieza.getXmax();
										Y = pieza.getYmin();
										if(X > mejorX)
										{
											mejorX = X;
											mejorY = Y;
											mejorRotacion = angulo;
										}
										if(X == mejorX && Y < mejorY)
										{
											mejorY = Y;
											mejorRotacion = angulo;
										}
									}
									else if( adyacencia > mejorAdyacencia)
									{
										mejorX = pieza.getXmax();
										mejorY = pieza.getYmin();
										mejorAdyacencia = adyacencia;
										mejorRotacion = angulo;
									}
								}
							}
						}
						pieza.moveToXY(posicion[0], posicion[1], 4);
					}


					if(posicionValida(objeto, pieza))
					{
						adyacencia = adyacenciaOP(objeto, pieza);
						if( adyacencia == mejorAdyacencia)
						{
							X = pieza.getXmax();
							Y = pieza.getYmin();
							if(X > mejorX)
							{
								mejorX = X;
								mejorY = Y;
								mejorRotacion = angulo;
							}
							if(X == mejorX && Y < mejorY)
							{
								mejorY = Y;
								mejorRotacion = angulo;
							}
						}
						else if( adyacencia > mejorAdyacencia)
						{
							mejorX = pieza.getXmax();
							mejorY = pieza.getYmin();
							mejorAdyacencia = adyacencia;
							mejorRotacion = angulo;
						}
					}

					temp = this.runIntoBottomRightPlacement(objeto, pieza);
					if(temp)
					{
						adyacencia = adyacenciaOP(objeto, pieza);
						if(posicionValida(objeto, pieza))
						{
							if( adyacencia == mejorAdyacencia)
							{
								X = pieza.getXmax();
								Y = pieza.getYmin();
								if(X > mejorX)
								{
									mejorX = X;
									mejorY = Y;
									mejorRotacion = angulo;
								}
								if(X == mejorX && Y < mejorY)
								{
									mejorY = Y;
									mejorRotacion = angulo;
								}
							}
							else if( adyacencia > mejorAdyacencia)
							{
								mejorX = pieza.getXmax();
								mejorY = pieza.getYmin();
								mejorAdyacencia = adyacencia;
								mejorRotacion = angulo;
							}
						}
					}
				}

				anguloPrevio = angulo;

			}


			if(mejorXLeft == objeto.getXmax() && mejorYLeft == objeto.getYmax() && mejorX == 0 && mejorY == objeto.getYmax())
			{
				return false;
			}


			if(mejorAdyacenciaLeft < mejorAdyacencia){
				flagLeft = false;
			}

			if(mejorAdyacenciaLeft == mejorAdyacencia){
				Random random = new Random();
				if(random.nextDouble() >= 0.5){
					flagLeft = false;
				}
			}


			if(flagLeft){
				pieza.desRotar();
				pieza.rotate(mejorRotacionLeft);
				pieza.moveToXY(mejorXLeft, mejorYLeft, 2);
			}else{
				pieza.desRotar();
				pieza.rotate(mejorRotacion);
				pieza.moveToXY(mejorX, mejorY, 1);
			}


			if(!posicionValida(objeto, pieza)){
				return false;
			}

			return true;

	}











	
	private boolean runIntoBottomLeftPlacement(Sheet objeto, Piece pieza)
	{
		int distVertical;
		int distHorizontal;
		int xpos = pieza.getXmin();
		int ypos = pieza.getYmin();
		int numgrande = 100000;

		do 
		{
			distVertical = cercaniaVerOP(objeto, pieza);
			if(distVertical > 0 && distVertical < numgrande)
			{
				pieza.moveDistance(distVertical, 2);
			}

			distHorizontal = cercaniaHorOP(objeto, pieza);
			if(distHorizontal > 0 && distHorizontal < numgrande) 
			{
				pieza.moveDistance(distHorizontal, 3);
			}

		}while( (distHorizontal > 0 && distHorizontal < numgrande)  
				|| (distVertical > 0 && distVertical < numgrande)  );  

		if (xpos == pieza.getXmin() && ypos == pieza.getYmin())
		{
			return false;
		}
		return true;
	}

	
	private boolean runIntoBottomRightPlacement(Sheet objeto, Piece pieza)
	{
		int distVertical;
		int distHorizontal;
		int xpos = pieza.getXmin();
		int ypos = pieza.getYmin();
		int numgrande = 100000;

		do
		{
			distVertical = cercaniaVerOP(objeto, pieza);
			if(distVertical > 0 && distVertical < numgrande)
			{
				pieza.moveDistance(distVertical, 2);
			}


			distHorizontal = cercaniaHorRightOP(objeto, pieza);
			if(distHorizontal > 0 && distHorizontal < numgrande)
			{
				pieza.moveDistance(distHorizontal, 4);
			}

		}while( (distHorizontal > 0 && distHorizontal < numgrande)
				|| (distVertical > 0 && distVertical < numgrande)  );

		if (xpos == pieza.getXmin() && ypos == pieza.getYmin())
		{
			return false;
		}
		return true;
	}

	
	
	
	
	private static PieceVirtual piezaAbajo(Piece pza)
	{
		int[] punto1 = new int[2];
		int[] punto2 = new int[2];
		int vert1, vert2, vertices;
		int puntos = 0;
		PieceVirtual pzatemp;
		
		vertices = pza.getvertices();
		int[] coord1 = new int[vertices*2];
	    int[] coord2 = new int[vertices*2];
		for (int i=0; i < vertices; i++)
		{
			coord1[i]= pza.coordX[i];
			coord2[i]= pza.coordY[i];
			coord1[i+vertices]= pza.coordX[i];
			coord2[i+vertices]= pza.coordY[i];
		}
		punto1[0] = pza.getXmin();
		punto1[1] = coordenadaLB(pza);
		punto2[0] = pza.getXmax();
		punto2[1] = coordenadaRB(pza);

		vert1 = pza.numVertice(punto1);
		vert2 = pza.numVertice(punto2);
		for (int i=0; i < vertices; i++)
		{
			coord1[i]= pza.coordX[i];
			coord2[i]= pza.coordY[i];
			coord1[i+vertices]= pza.coordX[i];
			coord2[i+vertices]= pza.coordY[i];
		}

		if(vert1 > vert2)  
		{
			puntos = (vert2 + vertices) - vert1 + 3;
		}else
		{
			puntos = vert2 - vert1 + 3;
		}
		
		
		int[] coordenadas = new int[puntos*2];
		coordenadas[0] = punto1[0];
		coordenadas[1] = punto1[1];
		coordenadas[2] = punto1[0];
		coordenadas[3] = 0;
		coordenadas[4] = punto2[0];
		coordenadas[5] = 0;
		coordenadas[6] = punto2[0];
		coordenadas[7] = punto2[1];
		if(puntos > 4){
			for (int i=4; i < puntos; i++)
			{
				coordenadas[i*2]  = coord1[vert1+(puntos-4)];
				coordenadas[i*2+1]= coord2[vert1+(puntos-4)];
			}
		}
		pzatemp  = new PieceVirtual(coordenadas); 
        return pzatemp;
	}


	
	
	private static PieceVirtual piezaIzq(Piece pza)
	{
		int[] punto1 = new int[2];
		int[] punto2 = new int[2];
		int vert1, vert2, vertices;
		int puntos = 0;
		PieceVirtual pzatemp;
		
		vertices = pza.getvertices();
		int[] coord1 = new int[vertices*2];
	    int[] coord2 = new int[vertices*2];
		for (int i=0; i < vertices; i++)
		{
			coord1[i]= pza.coordX[i];
			coord2[i]= pza.coordY[i];
			coord1[i+vertices]= pza.coordX[i];
			coord2[i+vertices]= pza.coordY[i];
		}
		punto1[0] = coordenadaTL(pza);
		punto1[1] = pza.getYmax();
		punto2[0] = coordenadaBL(pza);
		punto2[1] = pza.getYmin();

		vert1 = pza.numVertice(punto1);
		vert2 = pza.numVertice(punto2);
		for (int i=0; i < vertices; i++)
		{
			coord1[i]= pza.coordX[i];
			coord2[i]= pza.coordY[i];
			coord1[i+vertices]= pza.coordX[i];
			coord2[i+vertices]= pza.coordY[i];
		}

		if(vert1 > vert2)  
		{
			puntos = (vert2 + vertices) - vert1 + 3;
		}else
		{
			puntos = vert2 - vert1 + 3;
		}
		
		if(puntos == 4)
		{
			int[] coordenadas = {0, punto1[1], 0, punto2[1], punto2[0],punto2[1],
					             punto1[0],punto1[1]};  
			pzatemp  = new PieceVirtual(coordenadas);  
		}else if(puntos == 5)
		{
			int[] coordenadas = {0, punto1[1], 0, punto2[1],
					             punto2[0], punto2[1], 
					             coord1[vert1+1],coord2[vert1+1],
                                 punto1[0],punto1[1]};  
			pzatemp  = new PieceVirtual(coordenadas);
		}else
		{
			int[] coordenadas = new int[puntos*2];
			coordenadas[0] = 0;
			coordenadas[1] = punto1[1];
			coordenadas[2] = 0;
			coordenadas[3] = punto2[1];
			coordenadas[4] = punto2[0];
			coordenadas[5] = punto2[1];
			int j = puntos - 4;
			for(int i=3; i<puntos-1; i++)
			{
				coordenadas[i*2]   = coord1[vert1+j];
				coordenadas[i*2+1] = coord2[vert1+j];
				j--;
			}
			coordenadas[puntos*2-2] = punto1[0];
			coordenadas[puntos*2-1] = punto1[1];
			pzatemp  = new PieceVirtual(coordenadas);
		}
		return pzatemp;
		
	}

	
	private static PieceVirtual piezaRight(Piece pza)
	{
		int[] punto1 = new int[2];
		int[] punto2 = new int[2];
		int vert1, vert2, vertices;
		int puntos = 0;
		PieceVirtual pzatemp;

		vertices = pza.getvertices();
		int[] coord1 = new int[vertices*2];
		int[] coord2 = new int[vertices*2];

		for (int i=0; i < vertices; i++)
		{
			coord1[i]= pza.coordX[i];
			coord2[i]= pza.coordY[i];
			coord1[i+vertices]= pza.coordX[i];
			coord2[i+vertices]= pza.coordY[i];
		}





		punto1[0] = coordenadaTR(pza);
		punto1[1] = pza.getYmax();
		punto2[0] = coordenadaBR(pza);
		punto2[1] = pza.getYmin();

		vert1 = pza.numVertice(punto1);
		vert2 = pza.numVertice(punto2);
		for (int i=0; i < vertices; i++)
		{
			coord1[i]= pza.coordX[i];
			coord2[i]= pza.coordY[i];
			coord1[i+vertices]= pza.coordX[i];
			coord2[i+vertices]= pza.coordY[i];
		}









		if(vert1 < vert2)
		{
			puntos = (vert1 + vertices) - vert2 + 3;
		}else
		{
			puntos = vert1 - vert2 + 3;
		}

		if(puntos == 4)
		{


			int[] coordenadas = {punto1[0], punto1[1], punto2[0], punto2[1], 1000, punto2[1],
					1000, punto1[1]};
			pzatemp  = new PieceVirtual(coordenadas);
		}else if(puntos == 5)
		{




			int[] coordenadas = {1000, punto2[1], 1000, punto1[1],
					punto1[0], punto1[1],
					coord1[vert2+1], coord2[vert2+1],
					punto2[0], punto2[1]};
			pzatemp  = new PieceVirtual(coordenadas);
		}else
		{

















			int[] coordenadas = new int[puntos*2];
			coordenadas[0] = 1000;
			coordenadas[1] = punto2[1];
			coordenadas[2] = 1000;
			coordenadas[3] = punto1[1];
			coordenadas[4] = punto1[0];
			coordenadas[5] = punto1[1];
			int j = puntos - 4;
			for(int i=3; i<puntos-1; i++)
			{
				coordenadas[i*2]   = coord1[vert2+j];
				coordenadas[i*2+1] = coord2[vert2+j];
				j--;
			}
			coordenadas[puntos*2-2] = punto2[0];
			coordenadas[puntos*2-1] = punto2[1];
			pzatemp  = new PieceVirtual(coordenadas);
		}
		return pzatemp;

	}
	
	
	



	
	private static int cercaniaHorOP(Sheet objeto, Piece piezaOut)
	{	
		int distancia = 0;
		int minima = 100000;
		Piece pzaIn;
		Piece pzaIzq;
		if(piezaOut.getXmin() == 0)
		{
			return 0;
		}
		

		PieceVirtual pzatemp = piezaIzq(piezaOut);
		List<Piece> pzasInside;
		List<Piece> pzasIzq = new LinkedList<Piece>();

		pzasInside = objeto.getPzasInside();


		if (pzasInside.isEmpty())
		{
			minima = piezaOut.getXmin();  
		}
		else {   

			for (int i = 0; i < pzasInside.size(); i++)
			{
				pzaIn = (Piece)pzasInside.get(i);
				if ( (interseccionPPV(pzaIn, pzatemp) || dentroPPV(pzaIn, pzatemp) )
						&& interseccionPP(pzaIn, piezaOut) == false
						&& dentroPP(pzaIn, piezaOut) == false )
				{
					pzasIzq.add(pzaIn);
				}
			}	



			if (pzasIzq.isEmpty())
			{
				minima = piezaOut.getXmin();  
			}
			else {

				for (int i = 0; i < pzasIzq.size(); i++)
				{
					pzaIzq = (Piece)pzasIzq.get(i);
					distancia = cercaniaHorPP(pzaIzq, piezaOut);
					if ( distancia < minima)
					{ 
						minima = distancia;
					}
				}
			}
		}

		minima = Math.min(minima, piezaOut.getXmin());
		return minima;
	}

	
	private static int cercaniaHorRightOP(Sheet objeto, Piece piezaOut)
	{
		int distancia = 0;
		int minima = 100000;
		Piece pzaIn;
		Piece pzaIzq;
		if(piezaOut.getXmax() == objeto.getXmax()){
			return 0;
		}







		PieceVirtual pzatemp = piezaRight(piezaOut);
		List<Piece> pzasInside;
		List<Piece> pzasIzq = new LinkedList<Piece>();

		pzasInside = objeto.getPzasInside();



		if (pzasInside.isEmpty())
		{
			minima = objeto.getXmax() - piezaOut.getXmax();

		}
		else {

			for (int i = 0; i < pzasInside.size(); i++)
			{
				pzaIn = (Piece)pzasInside.get(i);

				if ( (interseccionPPV(pzaIn, pzatemp) || dentroPPV(pzaIn, pzatemp) )
						&& interseccionPP(pzaIn, piezaOut) == false
						&& dentroPP(pzaIn, piezaOut) == false )
				{
					pzasIzq.add(pzaIn);
				}
			}



			if (pzasIzq.isEmpty())
			{
				minima = objeto.getXmax() - piezaOut.getXmax();

			}
			else {

				for (int i = 0; i < pzasIzq.size(); i++)
				{
					pzaIzq = (Piece)pzasIzq.get(i);
					distancia = cercaniaHorRightPP(pzaIzq, piezaOut);
					if ( distancia < minima)
					{
						minima = distancia;
					}
				}
			}
		}


		minima = Math.min(minima, objeto.getXmax() - piezaOut.getXmax());
		return minima;
	}





	
	private static int cercaniaVerOP(Sheet objeto, Piece piezaOut)
	{	
		int distancia = 0;
		int minima = 100000;
		Piece pzaIn;
		Piece pzaAb;
		if(piezaOut.getYmin() == 0)
		{
			return 0;
		}
		

		PieceVirtual pzatemp  = piezaAbajo(piezaOut);
		List<Piece> pzasInside;
		List<Piece> pzasAb = new LinkedList<Piece>();

		pzasInside = objeto.getPzasInside();


		if (pzasInside.isEmpty())
		{
			minima = piezaOut.getYmin();
		}
		else {

			for (int i = 0; i < pzasInside.size(); i++)
			{
				pzaIn = (Piece)pzasInside.get(i);
				if ( (interseccionPPV(pzaIn, pzatemp) || dentroPPV(pzaIn, pzatemp) )
						&& interseccionPP(pzaIn, piezaOut) == false
						&& dentroPP(pzaIn, piezaOut) == false )
				{
					pzasAb.add(pzaIn);
				}
			}


			if (pzasAb.isEmpty())
			{
				minima = piezaOut.getYmin();  
			}
			else {

				for (int i = 0; i < pzasAb.size(); i++)
				{
					pzaAb = (Piece)pzasAb.get(i);
					distancia = cercaniaVerPP(pzaAb, piezaOut);
					if ( distancia < minima)
					{ 
						minima = distancia;
					}
				}
			}
		}

		minima = Math.min(minima, piezaOut.getYmin());
		return minima;
	}

	
	private static int cercaniaVerUpOP(Sheet objeto, Piece piezaOut)
	{
		int distancia = 0;
		int minima = 100000;
		Piece pzaIn;
		Piece pzaAb;
		if(piezaOut.getYmin() == 0)
		{
			return 0;
		}


		PieceVirtual pzatemp  = piezaAbajo(piezaOut);
		List<Piece> pzasInside;
		List<Piece> pzasAb = new LinkedList<Piece>();

		pzasInside = objeto.getPzasInside();


		if (pzasInside.isEmpty())
		{
			minima = piezaOut.getYmin();
		}
		else {

			for (int i = 0; i < pzasInside.size(); i++)
			{
				pzaIn = (Piece)pzasInside.get(i);
				if ( (interseccionPPV(pzaIn, pzatemp) || dentroPPV(pzaIn, pzatemp) )
						&& interseccionPP(pzaIn, piezaOut) == false
						&& dentroPP(pzaIn, piezaOut) == false )
				{
					pzasAb.add(pzaIn);
				}
			}


			if (pzasAb.isEmpty())
			{
				minima = piezaOut.getYmin();
			}
			else {

				for (int i = 0; i < pzasAb.size(); i++)
				{
					pzaAb = (Piece)pzasAb.get(i);
					distancia = cercaniaVerPP(pzaAb, piezaOut);
					if ( distancia < minima)
					{
						minima = distancia;
					}
				}
			}
		}

		minima = Math.min(minima, piezaOut.getYmin());
		return minima;
	}




	
	private static int coordenadaBL(Piece pieza)
	{
		int yminimo = pieza.getYmin();
		int xminimo = pieza.getXmax();

		for (int i = 0; i < pieza.getvertices(); i++)
		{
			if( pieza.coordY[i] == yminimo )
			{
				if( pieza.coordX[i] < xminimo )
				{
					xminimo = pieza.coordX[i];
				}
			}
		}
		return xminimo;
	}




	
	private static int coordenadaTL(Piece pieza)
	{
		int ymaximo = pieza.getYmax();
		int xminimo = pieza.getXmax();

		for (int i = 0; i < pieza.getvertices(); i++)
		{
			if( pieza.coordY[i] == ymaximo )
			{
				if( pieza.coordX[i] < xminimo )
				{
					xminimo = pieza.coordX[i];
				}
			}
		}
		return xminimo;
	}

	
	private static int coordenadaBR(Piece pieza)
	{
		int yminimo = pieza.getYmin();
		int xmaximo = pieza.getXmin();




		for (int i = 0; i < pieza.getvertices(); i++)
		{
			if( pieza.coordX[i] == xmaximo )
			{
				if( pieza.coordX[i] > xmaximo )
				{
					xmaximo = pieza.coordX[i];
				}
			}
		}
		return xmaximo;
	}




	
	private static int coordenadaLB(Piece pieza)
	{
		int xminimo = pieza.getXmin();	 	
		int yminimo = pieza.getYmax();

		for (int i = 0; i < pieza.getvertices(); i++)
		{
			if( pieza.coordX[i] == xminimo )
			{
				if( pieza.coordY[i] < yminimo )
				{
					yminimo = pieza.coordY[i];
				}
			}
		}

		return yminimo;
	}




	
	private static int coordenadaRB(Piece pieza)
	{
		int xmaximo = pieza.getXmax();	 	
		int yminimo = pieza.getYmax();

		for (int i = 0; i < pieza.getvertices(); i++)
		{
			if( pieza.coordX[i] == xmaximo )
			{
				if( pieza.coordY[i] < yminimo )
				{
					yminimo = pieza.coordY[i];
				}
			}
		}

		return yminimo;
	}

	
	private static int coordenadaTR(Piece pieza)
	{
		int ymaximo = pieza.getYmax();

		int xmaximo = pieza.getXmin();


		for (int i = 0; i < pieza.getvertices(); i++)
		{
			if( pieza.coordY[i] == ymaximo )
			{
				if( pieza.coordX[i] > xmaximo )
				{
					xmaximo = pieza.coordX[i];
				}
			}
		}
		return xmaximo;
	}


	
	private static int cercaniaHorPP(Piece pieza1, Piece pieza2)
	{







		int minima = 100000;
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int dist = 0;
		int i=0;
		int j=0;


		for (i = 0; i < vertices1; i++)
		{  for (j = 0; j < vertices2-1; j++)
		{
			dist = -SegmentoPuntoH(pieza1.coordX[i], pieza1.coordY[i], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
			if ( dist < 0)
			{ 
				continue;
			}  		

			if ( dist < minima)
			{ 
				minima = dist;
			}
		}
		}


		for (i = 0; i < vertices1; i++)
		{
			dist = -SegmentoPuntoH(pieza1.coordX[i], pieza1.coordY[i], 
					pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1], 
					pieza2.coordX[0], pieza2.coordY[0]);
			if ( dist < 0)
			{ 
				continue;
			}  

			if ( dist < minima)
			{ 
				minima = dist;
			}
		}


		for (i = 0; i < vertices2; i++)
		{  for (j = 0; j < vertices1-1; j++)
		{
			dist = SegmentoPuntoH(pieza2.coordX[i], pieza2.coordY[i], 
					pieza1.coordX[j], pieza1.coordY[j], 
					pieza1.coordX[j+1], pieza1.coordY[j+1]);
			if ( dist < 0)
			{ 
				continue;
			}  

			if ( dist < minima)
			{ 
				minima = dist;
			}
		}
		}


		for (i = 0; i < vertices2; i++)
		{
			dist = SegmentoPuntoH(pieza2.coordX[i], pieza2.coordY[i], 
					pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0]);
			if ( dist < 0)
			{ 
				continue;
			}  

			if ( dist < minima)
			{ 
				minima = dist;
			}
		}

		return minima;
	}

	private static int cercaniaHorRightPP(Piece pieza1, Piece pieza2)
	{







		int minima = 100000;
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int dist = 0;
		int i=0;
		int j=0;


		for (i = 0; i < vertices1; i++)
		{  for (j = 0; j < vertices2-1; j++)
		{
			dist = SegmentoPuntoH(pieza1.coordX[i], pieza1.coordY[i],
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
			if ( dist < 0)
			{
				continue;
			}

			if ( dist < minima)
			{
				minima = dist;
			}
		}
		}


		for (i = 0; i < vertices1; i++)
		{
			dist = SegmentoPuntoH(pieza1.coordX[i], pieza1.coordY[i],
					pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
					pieza2.coordX[0], pieza2.coordY[0]);
			if ( dist < 0)
			{
				continue;
			}

			if ( dist < minima)
			{
				minima = dist;
			}
		}


		for (i = 0; i < vertices2; i++)
		{  for (j = 0; j < vertices1-1; j++)
		{
			dist = -SegmentoPuntoH(pieza2.coordX[i], pieza2.coordY[i],
					pieza1.coordX[j], pieza1.coordY[j],
					pieza1.coordX[j+1], pieza1.coordY[j+1]);
			if ( dist < 0)
			{
				continue;
			}

			if ( dist < minima)
			{
				minima = dist;
			}
		}
		}


		for (i = 0; i < vertices2; i++)
		{
			dist = -SegmentoPuntoH(pieza2.coordX[i], pieza2.coordY[i],
					pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1],
					pieza1.coordX[0], pieza1.coordY[0]);
			if ( dist < 0)
			{
				continue;
			}

			if ( dist < minima)
			{
				minima = dist;
			}
		}

		return minima;
	}


	private static int cercaniaVerPP(Piece pieza1, Piece pieza2)
	{	







		int minima = 100000;
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int dist = 0;
		int i=0;
		int j=0;


		for (i = 0; i < vertices1; i++)
		{  for (j = 0; j < vertices2-1; j++)
		 {
			dist = -SegmentoPuntoV(pieza1.coordX[i], pieza1.coordY[i], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);

			if ( dist < 0)
			{ 
				continue;
			}  
			if ( dist < minima)
			{ 
				minima = dist;
			}
		 }
		}      	


		for (i = 0; i < vertices1; i++)
		{
			dist = -SegmentoPuntoV(pieza1.coordX[i], pieza1.coordY[i], 
					pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1], 
					pieza2.coordX[0], pieza2.coordY[0]);
			if ( dist < 0)
			{ 
				continue;
			}  
			if ( dist < minima)
			{ 
				minima = dist;
			}
		}


		for (i = 0; i < vertices2; i++)
		{  for (j = 0; j < vertices1-1; j++)
		 {
			dist = SegmentoPuntoV(pieza2.coordX[i], pieza2.coordY[i], 
					pieza1.coordX[j], pieza1.coordY[j], 
					pieza1.coordX[j+1], pieza1.coordY[j+1]);
			if ( dist < 0)
			{ 
				continue;
			}  
			if ( dist < minima)
			{ 
				minima = dist;
			}
		 }
		}


		for (i = 0; i < vertices2; i++)
		{
			dist = SegmentoPuntoV(pieza2.coordX[i], pieza2.coordY[i], 
					pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0]);
			if ( dist < 0)
			{ 
				continue;
			}  
			if ( dist < minima)
			{ 
				minima = dist;
			}
		}

		return minima;
	}



	public static int SegmentoPuntoH(int X1, int Y1, int X2, int Y2, int X3, int Y3)
	{





		int distancia = 0;
		double dist = 0;
		int numgrande = 100000;

		if( (Y1 < Y2 && Y1 < Y3) ||
				(Y1 > Y2 && Y1 > Y3) )
		{
			return numgrande;  
		}

		if( (Y1 == Y2 && Y1 == Y3) &&
				(X1 > X2  && X1 > X3) )
		{
			distancia = Math.min(X1-X2, X1-X3); 
			return distancia; 
		}

		if( (Y1 == Y2 && Y1 == Y3) &&
				(X1 < X2  && X1 < X3) )
		{
			distancia = -Math.min(X2-X1, X3-X1); 
			return distancia; 
		}

		if(Y1 == Y2 && Y1 == Y3) 
		{
			distancia = 0; 
			return distancia; 
		}
		else 
		{
			dist = (double)(X1-X2) + ((double)(X2-X3)*(double)(Y2-Y1)/(double)(Y2-Y3));
			if (dist < 0)
			{
				distancia = (int)Math.ceil(dist);
			}
			else
			{
				distancia = (int)Math.floor(dist);
			}
		}

		return distancia;
	}



	public static int SegmentoPuntoV(int X1, int Y1, int X2, int Y2, int X3, int Y3)
	{





		int distancia = 0;
		double dist = 0;
		int numgrande = 100000;

		if( (X1 < X2 && X1 < X3) ||
				(X1 > X2 && X1 > X3) )
		{
			return numgrande;  
		}

		if( (X1 == X2 && X1 == X3) &&
				(Y1 > Y2  && Y1 > Y3) )
		{
			distancia = Math.min(Y1-Y2, Y1-Y3); 
			return distancia; 
		}

		if( (X1 == X2 && X1 == X3) &&
				(Y1 < Y2  && Y1 < Y3) )
		{
			distancia = -Math.min(Y2-Y1, Y3-Y1); 
			return distancia; 
		}

		if(X1 == X2 && X1 == X3) 
		{
			distancia = 0; 
			return distancia; 
		}
		else 
		{
			dist = (double)(Y1-Y2) + ((double)(Y2-Y3)*(double)(X2-X1)/(double)(X2-X3));
			if (dist < 0)
			{
				distancia = (int)Math.ceil(dist);
			}
			else
			{
				distancia = (int)Math.floor(dist);
			}
		}

		return distancia;
	}





	private static boolean posicionValida(Sheet objeto, Piece pieza)
	{

		if(pieza.getYmax() <= objeto.getYmax() && 
				pieza.getXmax() <= objeto.getXmax() && 
				pieza.getXmin()>=0 && pieza.getYmin()>=0 )
		{

			if( interseccionOP(objeto, pieza) == false)
			{

				if( dentroOP(objeto, pieza) == false )
				{  
					return true;
				}
			}
		}

		return false;
	}


	private static boolean interseccionOP(Sheet objeto, Piece piezaOut)
	{
		Piece pzaIn;
		boolean value;
		List<Piece> pzasInside;
		pzasInside = objeto.getPzasInside();


		if (pzasInside.isEmpty())
		{
			return false;
		}

		for (int i = 0; i < pzasInside.size(); i++)
		{  	
			pzaIn = (Piece)pzasInside.get(i);
			value = interseccionPP(piezaOut, pzaIn);
			if(value)
			{
				return true;
			}
		}

		return false;
	}



	

	private static boolean interseccionPP(Piece pieza1, Piece pieza2)
	{
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		boolean value;



		if ( (pieza1.getXmax() <= pieza2.getXmin())
				||(pieza2.getXmax() <= pieza1.getXmin())
				||(pieza1.getYmax() <= pieza2.getYmin())
				||(pieza2.getYmax() <= pieza1.getYmin()) )    
		{  
			return false;
		}      



		for (int i = 0; i < vertices1-1; i++)
		{  for (int j = 0; j < vertices2-1; j++)
			{
				value = interseccionSS(pieza1.coordX[i], pieza1.coordY[i],
						pieza1.coordX[i+1], pieza1.coordY[i+1],
						pieza2.coordX[j], pieza2.coordY[j],
						pieza2.coordX[j+1], pieza2.coordY[j+1]);
				if ( value )
				{
					return true;
				}
			}


			value = interseccionSS(pieza1.coordX[i], pieza1.coordY[i],
					pieza1.coordX[i+1], pieza1.coordY[i+1],
					pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
					pieza2.coordX[0], pieza2.coordY[0]);
			if ( value )
			 {
				return true;
			 }
		}




		for (int j = 0; j < vertices2-1; j++)
		{
			value = interseccionSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
			if ( value )
			{ 
				return true;  
			}  		
		}




		value = interseccionSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
				pieza1.coordX[0], pieza1.coordY[0], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		if ( value )
		{ 
			return true;  
		}  		

		return false;  
	}


	

	private static boolean interseccionPPV(Piece pieza1, PieceVirtual pieza2)
	{
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		boolean value;


		if ( (pieza1.getXmax() <= pieza2.getXmin())
				||(pieza2.getXmax() <= pieza1.getXmin())
				||(pieza1.getYmax() <= pieza2.getYmin())
				||(pieza2.getYmax() <= pieza1.getYmin()) )    
		{
			return false;
		}      


		for (int i = 0; i < vertices1-1; i++)
		{  for (int j = 0; j < vertices2-1; j++)
		{
			value = interseccionSS(pieza1.coordX[i], pieza1.coordY[i], 
					pieza1.coordX[i+1], pieza1.coordY[i+1], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
			if ( value )
			{ 
				return true;  
			}  		
		}

		value = interseccionSS(pieza1.coordX[i], pieza1.coordY[i], 
				pieza1.coordX[i+1], pieza1.coordY[i+1], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		if ( value )
		 { 
			return true;  
		 }  		
		}



		for (int j = 0; j < vertices2-1; j++)
		{
			value = interseccionSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
			if ( value )
			{ 
				return true;  
			}  		
		}



		value = interseccionSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
				pieza1.coordX[0], pieza1.coordY[0], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		if ( value )
		 { 
			return true;  
		 }  		

		return false;  
	}	
	
	

	





	private static boolean interseccionSS(int X1, int Y1, int X2, int Y2, int X3, int Y3, int X4, int Y4)
	{
		double m1, m2, x, y;


		if ( (Math.max(X1, X2) <= Math.min(X3, X4))
				||(Math.max(X3, X4) <= Math.min(X1, X2))
				||(Math.max(Y1, Y2) <= Math.min(Y3, Y4))
				||(Math.max(Y3, Y4) <= Math.min(Y1, Y2)) )    
		{
			return false;
		}


		if (X1 == X2)    
		{
			if (Y3 == Y4)
			{
				if( (X1 < Math.max(X3, X4) && X1 > Math.min(X3, X4))
						&&(Y3 < Math.max(Y1, Y2) && Y3 > Math.min(Y1, Y2)) )
				{
					return true;
				}
			}

			m2 = (double)(Y4-Y3)/ (double)(X4-X3);
			y = m2 * (double)(X1 - X3) + (double)(Y3);

			if( (y < Math.max(Y1, Y2) && y > Math.min(Y1, Y2)) 
					&& (y < Math.max(Y3, Y4) && y > Math.min(Y3, Y4)) )
			{
				return true;
			}
			else
			{
				return false;
			}
		}  


		if (X3 == X4)    
		{
			if (Y1 == Y2)
			{
				if( (X3 < Math.max(X1, X2) && X3 > Math.min(X1, X2))
						&&(Y1 < Math.max(Y3, Y4) && Y1 > Math.min(Y3, Y4)) )
				{
					return true;
				}
			}


			m1 = (double)(Y2-Y1)/ (double)(X2-X1);
			y = m1 * (double)(X3 - X1) + (double)(Y1);

			if( (y < Math.max(Y1, Y2) && y > Math.min(Y1, Y2)) 
					&& (y < Math.max(Y3, Y4) && y > Math.min(Y3, Y4)) )
			{
				return true;
			}
			else
			{
				return false;
			}
		}  


		m1 = (double)(Y2-Y1)/ (double)(X2-X1);
		m2 = (double)(Y4-Y3)/ (double)(X4-X3);

		if (m1 == m2)    
		{
			return false;
		}  

		x = (m1*(double)X1 - (double)Y1 - m2*(double)X3 + (double)Y3) / (m1-m2);
		x = redondeaSiCerca(x);



		if( (x < Math.max(X1, X2) && x > Math.min(X1, X2)) 
				&& (x < Math.max(X3, X4) && x > Math.min(X3, X4)) )
		{
			return true;
		}

		return false;  
	}





	public static boolean interseccionSP(int x1, int y1, int x2, int y2, Piece piezaOut)
	{	
		int vertices = piezaOut.getvertices();
		for(int i=0; i < vertices-1; i++)
		{
			if(  interseccionSS(x1, y1, x2, y2, piezaOut.coordX[i], piezaOut.coordY[i],
					            piezaOut.coordX[i+1], piezaOut.coordY[i+1])  )
			{
				return true;
			}
		}
		
		if(  interseccionSS(x1, y1, x2, y2, piezaOut.coordX[vertices-1], piezaOut.coordY[vertices-1],
	            piezaOut.coordX[0], piezaOut.coordY[0])  )
		{
			return true;
		}
		
		return false;
	}
	
	
	


	private static double redondeaSiCerca(double x)
	{
		double tolerancia = 0.00001;
		if( Math.abs(x - Math.ceil(x)) < tolerancia )
		{
			x = 	Math.ceil(x);
		} else if( Math.abs(x - Math.floor(x)) < tolerancia )
		{
			x = 	Math.floor(x);	
		}

		return x;
	}



	

	private static int adyacenciaOP(Sheet objeto, Piece piezaOut)
	{
		Piece pzaIn;
		int[] vertices = {0,0, objeto.getXmax(),0,      
				objeto.getXmax(),objeto.getYmax(), 0,objeto.getYmax()};
		PieceVirtual pzaOb = new PieceVirtual(vertices);
		List<Piece> pzasInside;
		int adyacencia = 0;

		pzasInside = objeto.getPzasInside();
		adyacencia += adyacenciaPPV(piezaOut, pzaOb);
		if (pzasInside.isEmpty())
		{
			return adyacencia;
		}

		for (int i = 0; i < pzasInside.size(); i++)
		{
			pzaIn = (Piece)pzasInside.get(i);
			adyacencia += adyacenciaPP(piezaOut, pzaIn);
		}

		return adyacencia;
	}




	

	private static int adyacenciaPP(Piece pieza1, Piece pieza2)
	{
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int adyacencia = 0;



		if ( (pieza1.getXmax() < pieza2.getXmin())
				||(pieza2.getXmax() < pieza1.getXmin())
				||(pieza1.getYmax() < pieza2.getYmin())
				||(pieza2.getYmax() < pieza1.getYmin()) )    
		{
			return 0;
		}     


		for (int i = 0; i < vertices1-1; i++)
		{  for (int j = 0; j < vertices2-1; j++)
		{
			adyacencia += adyacenciaSS(pieza1.coordX[i], pieza1.coordY[i], 
					pieza1.coordX[i+1], pieza1.coordY[i+1], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
		}

		adyacencia += adyacenciaSS(pieza1.coordX[i], pieza1.coordY[i], 
				pieza1.coordX[i+1], pieza1.coordY[i+1], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		}



		for (int j = 0; j < vertices2-1; j++)
		{
			adyacencia += adyacenciaSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
		}


		adyacencia += adyacenciaSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
				pieza1.coordX[0], pieza1.coordY[0], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		return adyacencia;  
	}



	

	private static int adyacenciaPPV(Piece pieza1, PieceVirtual pieza2)
	{
		int vertices1 = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int adyacencia = 0;



		if ( (pieza1.getXmax() < pieza2.getXmin())
				||(pieza2.getXmax() < pieza1.getXmin())
				||(pieza1.getYmax() < pieza2.getYmin())
				||(pieza2.getYmax() < pieza1.getYmin()) )    
		{
			return 0;
		}     


		for (int i = 0; i < vertices1-1; i++)
		{
			for (int j = 0; j < vertices2-1; j++)
			{
				adyacencia += adyacenciaSS(pieza1.coordX[i], pieza1.coordY[i],
						pieza1.coordX[i+1], pieza1.coordY[i+1],
						pieza2.coordX[j], pieza2.coordY[j],
						pieza2.coordX[j+1], pieza2.coordY[j+1]);
			}

			adyacencia += adyacenciaSS(pieza1.coordX[i], pieza1.coordY[i],
					pieza1.coordX[i+1], pieza1.coordY[i+1],
					pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
					pieza2.coordX[0], pieza2.coordY[0]);
		}



		for (int j = 0; j < vertices2-1; j++)
		{
			adyacencia += adyacenciaSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
					pieza1.coordX[0], pieza1.coordY[0], 
					pieza2.coordX[j], pieza2.coordY[j],
					pieza2.coordX[j+1], pieza2.coordY[j+1]);
		}



		adyacencia += adyacenciaSS(pieza1.coordX[vertices1-1], pieza1.coordY[vertices1-1], 
				pieza1.coordX[0], pieza1.coordY[0], 
				pieza2.coordX[vertices2-1], pieza2.coordY[vertices2-1],
				pieza2.coordX[0], pieza2.coordY[0]);
		return adyacencia;  
	}	
	
	

	



	private static int adyacenciaSS(int X1, int Y1, int X2, int Y2, int X3, int Y3, int X4, int Y4)
	{
		int adyacencia = 0;
		double m1, m2, b1, b2;


		if ( (max(X1, X2) < min(X3, X4))
				||(max(X3, X4) < min(X1, X2))
				||(max(Y1, Y2) < min(Y3, Y4))
				||(max(Y3, Y4) < min(Y1, Y2)) )    
		{
			return 0;
		}

		


		if (X1 == X2 && X3 == X4)    
		{

			if(   (Y1 <= Math.max(Y3,Y4)) && (Y1 >= Math.min(Y3,Y4)) 
					&&  (Y2 <= Math.max(Y3,Y4)) && (Y2 >= Math.min(Y3,Y4)) )
			{
				return Math.abs(Y2-Y1);
			}

			if(   (Y3 <= Math.max(Y1,Y2)) && (Y3 >= Math.min(Y1,Y2))
					&&  (Y4 <= Math.max(Y1,Y2)) && (Y4 >= Math.min(Y1,Y2)) )
			{
				return Math.abs(Y4-Y3);
			}

			if(  Math.max(Y1,Y2) > Math.max(Y3,Y4) )
			{
				adyacencia = Math.max(Y3,Y4) - Math.min(Y1,Y2);
				return adyacencia;
			}

			if(  Math.max(Y3,Y4) > Math.max(Y1,Y2) )
			{
				adyacencia = Math.max(Y1,Y2) - Math.min(Y3,Y4);
				return adyacencia;
			}
		}



		if (X1 == X2 || X3 == X4)
		{
			return 0;
		} 


		if (Y1 == Y2 && Y3 == Y4)    
		{



			if(   (X1 <= Math.max(X3,X4)) && (X1 >= Math.min(X3,X4)) 
					&&  (X2 <= Math.max(X3,X4)) && (X2 >= Math.min(X3,X4)) )
			{
				return Math.abs(X2-X1);
			}

			if(   (X3 <= Math.max(X1,X2)) && (X3 >= Math.min(X1,X2))
					&&  (X4 <= Math.max(X1,X2)) && (X4 >= Math.min(X1,X2)) )
			{
				return Math.abs(X4-X3);
			}

			if(  Math.max(X1,X2) > Math.max(X3,X4) )
			{
				adyacencia = Math.max(X3,X4) - Math.min(X1,X2);
				return adyacencia;
			}

			if(  Math.max(X3,X4) > Math.max(X1,X2) )
			{
				adyacencia = Math.max(X1,X2) - Math.min(X3,X4);
				return adyacencia;
			}
		}



		m1 = (double)(Y2-Y1)/ (double)(X2-X1);
		m2 = (double)(Y4-Y3)/ (double)(X4-X3);
		if (m1 != m2)    
		{
			return 0;
		} 

		b1 = (double)(Y1) - m1*(double)(X1);
		b2 = (double)(Y3) - m2*(double)(X3);
		if (b1 != b2)    
		{
			return 0;
		}



		if(   (Y1 <= Math.max(Y3,Y4)) && (Y1 >= Math.min(Y3,Y4)) 
				&&  (Y2 <= Math.max(Y3,Y4)) && (Y2 >= Math.min(Y3,Y4)) )
		{
			adyacencia = (int)distPuntoPunto(X1, Y1, X2, Y2);
			return adyacencia;
		}

		if(   (Y3 <= Math.max(Y1,Y2)) && (Y3 >= Math.min(Y1,Y2))
				&&  (Y4 <= Math.max(Y1,Y2)) && (Y4 >= Math.min(Y1,Y2)) )
		{
			adyacencia = (int)distPuntoPunto(X3, Y3, X4, Y4);
			return adyacencia;
		}

		if(  Math.max(Y1,Y2) > Math.max(Y3,Y4) )
		{
			if(m1 > 0)
			{
				adyacencia = (int)distPuntoPunto(max(X3,X4), max(Y3,Y4), min(X1,X2), min(Y1,Y2));
				return adyacencia;
			}
			adyacencia = (int)distPuntoPunto(min(X3,X4), max(Y3,Y4), max(X1,X2), min(Y1,Y2));
			return adyacencia;
		}

		if(  Math.max(Y3,Y4) > Math.max(Y1,Y2) )
		{
			if(m1 > 0)
			{
				adyacencia = (int)distPuntoPunto(max(X1,X2), max(Y1,Y2), min(X3,X4), min(Y3,Y4));
				return adyacencia;
			}
			adyacencia = (int)distPuntoPunto(min(X1,X2), max(Y1,Y2), max(X3,X4), min(Y3,Y4));
			return adyacencia;
		}
		return adyacencia;
	}

	






	public static int adyacenciaSP(int x1, int y1, int x2, int y2, Piece piezaOut)
	{	
		int vertices = piezaOut.getvertices();
		int distancia = 0;
		for(int i=0; i < vertices-1; i++)
		{
			distancia +=  adyacenciaSS(x1, y1, x2, y2, piezaOut.coordX[i], piezaOut.coordY[i],
					            piezaOut.coordX[i+1], piezaOut.coordY[i+1]);
		}
		
		distancia += adyacenciaSS(x1, y1, x2, y2, piezaOut.coordX[vertices-1], piezaOut.coordY[vertices-1],
	            piezaOut.coordX[0], piezaOut.coordY[0]);
	
		return distancia;
	}


	

	private static boolean dentroOP(Sheet objeto, Piece piezaOut)
	{
		Piece pzaIn;
		boolean value;
		List<Piece> pzasInside;




		pzasInside = objeto.getPzasInside();


		if (pzasInside.isEmpty())
		{
			return false;
		}

		for (int i = 0; i < pzasInside.size(); i++)
		{
			pzaIn = (Piece)pzasInside.get(i);
			value = dentroPP(pzaIn, piezaOut);
			if(value)
			{
				return true;
			}
		}

		return false;

	}


	

	private static boolean dentroPP(Piece pieza1, Piece pieza2)
	{
		boolean value;
		int vertices = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int alto, ancho;

		if(  pieza1.getXmax() <= pieza2.getXmin() ||
				pieza2.getXmax() <= pieza1.getXmin() ||
				pieza1.getYmax() <= pieza2.getYmin() ||
				pieza2.getYmax() <= pieza1.getYmin() )
		{
			return false;	
		} 


		alto = Math.max(pieza1.getXmax(), pieza2.getXmax())-
		       Math.min(pieza1.getXmin(), pieza2.getXmin());
		ancho = Math.max(pieza1.getYmax(), pieza2.getYmax())-
	            Math.min(pieza1.getYmin(), pieza2.getYmin());
		if(alto * ancho < pieza1.getTotalSize() + pieza2.getTotalSize() )
		{
			return true;
		}
		


		for (int j = 0; j < vertices; j++)
		{
			value = dentroPuntoPieza(pieza1.coordX[j], pieza1.coordY[j], pieza2);
			if(value)
			{
				return true;
			}
		}


		for (int j = 0; j < vertices-1; j++)
		{
			value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2, 
					(pieza1.coordY[j]+pieza1.coordY[j+1])/2, pieza2);
			if(value)
			{
				return true;
			}


			value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2 +2, 
					(pieza1.coordY[j]+pieza1.coordY[j+1])/2 +2, pieza1);
			if(value)
			{
				value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2 +2, 
						(pieza1.coordY[j]+pieza1.coordY[j+1])/2 +2, pieza2);
				if(value)
				{
					return true;
				}
			}
		}


        value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2, 
				(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2, pieza2);
		if(value)
		{
			return true;
		}
		value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2 +2, 
				(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2 +2, pieza1);
		if(value)
		{
			value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2 +2, 
					(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2 +2, pieza2);
			if(value)
			{
				return true;
			}
		}
		
		

		value = dentroPuntoPieza((pieza1.getXmax()+pieza1.getXmin())/2, 
				(pieza1.getYmax()+pieza1.getYmin())/2, pieza1);
		if(value)
		{
			value = dentroPuntoPieza((pieza1.getXmax()+pieza1.getXmin())/2, 
					(pieza1.getYmax()+pieza1.getYmin())/2, pieza2);
			if(value)
			{			
				return true;
			}
		}
		
		

		for (int j = 0; j < vertices2; j++)
		{
			value = dentroPuntoPieza(pieza2.coordX[j], pieza2.coordY[j], pieza1);
			if(value)
			{
				return true;
			}
		}


		for (int j = 0; j < vertices2-1; j++)
		{
			value = dentroPuntoPieza((pieza2.coordX[j]+pieza2.coordX[j+1])/2, 
					(pieza2.coordY[j]+pieza2.coordY[j+1])/2, pieza1);
			if(value)
			{
				return true;
			}


			value = dentroPuntoPieza((pieza2.coordX[j]+pieza2.coordX[j+1])/2 +2, 
					(pieza2.coordY[j]+pieza2.coordY[j+1])/2 +2, pieza2);
			if(value)
			{
				value = dentroPuntoPieza((pieza2.coordX[j]+pieza2.coordX[j+1])/2 +2, 
						(pieza2.coordY[j]+pieza2.coordY[j+1])/2 +2, pieza1);
				if(value)
				{
					return true;
				}
			}
		}


		value = dentroPuntoPieza((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2, 
				(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2, pieza1);
		if(value)
		{
			return true;
		}
		value = dentroPuntoPieza((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2 +2, 
				(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2 +2, pieza2);
		if(value)
		{
			value = dentroPuntoPieza((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2 +2, 
					(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2 +2, pieza1);
			if(value)
			{
				return true;
			}
		}
	
		

		value = dentroPuntoPieza((pieza2.getXmax()+pieza2.getXmin())/2, 
				(pieza2.getYmax()+pieza2.getYmin())/2, pieza2);
		if(value)
		{
			value = dentroPuntoPieza((pieza2.getXmax()+pieza2.getXmin())/2, 
					(pieza2.getYmax()+pieza2.getYmin())/2, pieza1);
			if(value)
			{			
				return true;
			}
		}
		return false;
	}

	
	
	private static boolean dentroPPV(Piece pieza1, PieceVirtual pieza2)
	{
		boolean value;
		int vertices = pieza1.getvertices();
		int vertices2 = pieza2.getvertices();
		int alto, ancho;

		if(  pieza1.getXmax() <= pieza2.getXmin() ||
				pieza2.getXmax() <= pieza1.getXmin() ||
				pieza1.getYmax() <= pieza2.getYmin() ||
				pieza2.getYmax() <= pieza1.getYmin() )
		{
			return false;	
		} 
		

		alto = Math.max(pieza1.getXmax(), pieza2.getXmax())-
		       Math.min(pieza1.getXmin(), pieza2.getXmin());
		ancho = Math.max(pieza1.getYmax(), pieza2.getYmax())-
	            Math.min(pieza1.getYmin(), pieza2.getYmin());
		if(alto * ancho < pieza1.getTotalSize() + pieza2.getTotalSize() )
		{
			return true;
		}



		for (int j = 0; j < vertices; j++)
		{
			value = dentroPuntoPiezaV(pieza1.coordX[j], pieza1.coordY[j], pieza2);
			if(value)
			{
				return true;
			}
		}


		for (int j = 0; j < vertices-1; j++)
		{
			value = dentroPuntoPiezaV((pieza1.coordX[j]+pieza1.coordX[j+1])/2, 
					(pieza1.coordY[j]+pieza1.coordY[j+1])/2, pieza2);
			if(value)
			{
				return true;
			}
			value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2 +2, 
					(pieza1.coordY[j]+pieza1.coordY[j+1])/2 +2, pieza1);
			if(value)
			{
				value = dentroPuntoPiezaV((pieza1.coordX[j]+pieza1.coordX[j+1])/2 +2, 
						(pieza1.coordY[j]+pieza1.coordY[j+1])/2 +2, pieza2);
				if(value)
				{
					return true;
				}
			}
		
		}


		value = dentroPuntoPiezaV((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2, 
				(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2, pieza2);
		if(value)
		{
			return true;
		}
		value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2 +2, 
				(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2 +2, pieza1);
		if(value)
		{
			value = dentroPuntoPiezaV((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2 +2, 
					(pieza1.coordY[vertices-1]+pieza1.coordY[0])/2 +2, pieza2);
			if(value)
			{
				return true;
			}
		}
		

		value = dentroPuntoPieza((pieza1.getXmax()+pieza1.getXmin())/2, 
				(pieza1.getYmax()+pieza1.getYmin())/2, pieza1);
		if(value)
		{
			value = dentroPuntoPiezaV((pieza1.getXmax()+pieza1.getXmin())/2, 
					(pieza1.getYmax()+pieza1.getYmin())/2, pieza2);
			if(value)
			{			
				return true;
			}
		}


		for (int j = 0; j < vertices2; j++)
		{
			value = dentroPuntoPieza(pieza2.coordX[j], pieza2.coordY[j], pieza1);
			if(value)
			{
				return true;
			}
		}


		for (int j = 0; j < vertices2-1; j++)
		{
			value = dentroPuntoPieza((pieza2.coordX[j]+pieza2.coordX[j+1])/2, 
					(pieza2.coordY[j]+pieza2.coordY[j+1])/2, pieza1);
			if(value)
			{
				return true;
			}
			value = dentroPuntoPiezaV((pieza2.coordX[j]+pieza2.coordX[j+1])/2 +2, 
					(pieza2.coordY[j]+pieza2.coordY[j+1])/2 +2, pieza2);
			if(value)
			{
				value = dentroPuntoPieza((pieza2.coordX[j]+pieza2.coordX[j+1])/2 +2, 
						(pieza2.coordY[j]+pieza2.coordY[j+1])/2 +2, pieza1);
				if(value)
				{
					return true;
				}
			}
		
		}


		value = dentroPuntoPieza((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2, 
				(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2, pieza1);
		if(value)
		{
			return true;
		}
		value = dentroPuntoPiezaV((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2 +2, 
				(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2 +2, pieza2);
		if(value)
		{
			value = dentroPuntoPieza((pieza2.coordX[vertices2-1]+pieza2.coordX[0])/2 +2, 
					(pieza2.coordY[vertices2-1]+pieza2.coordY[0])/2 +2, pieza1);
			if(value)
			{
				return true;
			}
		}
		
		

		value = dentroPuntoPiezaV((pieza2.getXmax()+pieza2.getXmin())/2, 
				(pieza2.getYmax()+pieza2.getYmin())/2, pieza2);
		if(value)
		{
			value = dentroPuntoPieza((pieza2.getXmax()+pieza2.getXmin())/2, 
					(pieza2.getYmax()+pieza2.getYmin())/2, pieza1);
			if(value)
			{			
				return true;
			}
		}

		return false;
	}
	

	

	public static boolean dentroPuntoPieza(int x1, int y1, Piece pieza)
	{
		int contador = 0;
		int numgrande = 100000; 
		int vertices = pieza.getvertices();
		int dFun1, dFun2;
		boolean value;
		if(x1 <= pieza.getXmin() || x1 >= pieza.getXmax() 
		   || y1 <= pieza.getYmin() || y1 >= pieza.getYmax())
		{
			return false;
		}
		
		

		for (int i = 0; i < vertices-1; i++)
		{
			value = dentroPuntoSegm(x1, y1,
					pieza.coordX[i], pieza.coordY[i],
					pieza.coordX[i+1], pieza.coordY[i+1]);
			if (value)
			{
				return false;
			}
		}
		value = dentroPuntoSegm(x1, y1,
				pieza.coordX[vertices-1], pieza.coordY[vertices-1],
				pieza.coordX[0], pieza.coordY[0]);
		if (value)
		{
			return false;
		}



		for (int i = 0; i < vertices-1; i++)
		{
			value = interseccionSS(x1, y1, numgrande, y1, 
					pieza.coordX[i], pieza.coordY[i],
					pieza.coordX[i+1], pieza.coordY[i+1]);
			if (value)
			{
				contador++;
			}
		}

		value = interseccionSS(x1, y1, numgrande, y1, 
				pieza.coordX[vertices-1], pieza.coordY[vertices-1],
				pieza.coordX[0], pieza.coordY[0]);
		if (value)
		{
			contador++;
		}

		


		for (int i = 0; i < vertices; i++)
		{

			value = ( dentroPuntoSegm(pieza.coordX[i], pieza.coordY[i],
					   x1, y1, numgrande, y1) 
					   && (x1 != pieza.coordX[i] || y1 != pieza.coordY[i]) );
		 
			if (value)
			{

				if(i == 0)
				{
					dFun1 = ArrayOperations.dFunction(pieza.coordX[vertices-1], pieza.coordY[vertices-1],
							x1, y1, numgrande, y1);
				}else{
					dFun1 = ArrayOperations.dFunction(pieza.coordX[i-1], pieza.coordY[i-1],
							x1, y1, numgrande, y1);
				}

				if(i == vertices - 1)
				{
					dFun2 = ArrayOperations.dFunction(pieza.coordX[0], pieza.coordY[0],
							x1, y1, numgrande, y1);
				}else{
					dFun2 = ArrayOperations.dFunction(pieza.coordX[i+1], pieza.coordY[i+1],
							x1, y1, numgrande, y1);
				}
				









				if( (dFun1 < 0 && dFun2 > 0) ||  (dFun1 > 0 && dFun2 < 0) )
				{
					contador ++;
				}
			}
		}
		
		

		if (contador % 2 == 0)
		{
			return false;
		}
		return true;  
	}


	
	

	private static boolean dentroPuntoPiezaV(int x1, int y1, PieceVirtual pieza)
	{
		int contador = 0;
		int numgrande = 100000; 
		int vertices = pieza.getvertices();
		int dFun1, dFun2;
		boolean value;

		if(x1 <= pieza.getXmin() || x1 >= pieza.getXmax() 
			   || y1 <= pieza.getYmin() || y1 >= pieza.getYmax())
		{
			return false;
		}
		

		for (int i = 0; i < vertices-1; i++)
		{
			value = dentroPuntoSegm(x1, y1,
					pieza.coordX[i], pieza.coordY[i],
					pieza.coordX[i+1], pieza.coordY[i+1]);
			if (value)
			{
				return false;
			}
		}
		value = dentroPuntoSegm(x1, y1,
				pieza.coordX[vertices-1], pieza.coordY[vertices-1],
				pieza.coordX[0], pieza.coordY[0]);
		if (value)
		{
			return false;
		}



		for (int i = 0; i < vertices-1; i++)
		{
			value = interseccionSS(x1, y1, numgrande, y1, 
					pieza.coordX[i], pieza.coordY[i],
					pieza.coordX[i+1], pieza.coordY[i+1]);
			if (value)
			{
				contador++;
			}
		}


		value = interseccionSS(x1, y1, numgrande, y1, 
				pieza.coordX[vertices-1], pieza.coordY[vertices-1],
				pieza.coordX[0], pieza.coordY[0]);

		if (value)
		{
			contador++;
		}

		


		for (int i = 0; i < vertices; i++)
		{

			value = ( dentroPuntoSegm(pieza.coordX[i], pieza.coordY[i],
					   x1, y1, numgrande, y1) 
					   && (x1 != pieza.coordX[i] || y1 != pieza.coordY[i]) );
		 
			if (value)
			{

				if(i == 0)
				{
					dFun1 = ArrayOperations.dFunction(pieza.coordX[vertices-1], pieza.coordY[vertices-1],
							x1, y1, numgrande, y1);
				}else{
					dFun1 = ArrayOperations.dFunction(pieza.coordX[i-1], pieza.coordY[i-1],
							x1, y1, numgrande, y1);
				}

				if(i == vertices - 1)
				{
					dFun2 = ArrayOperations.dFunction(pieza.coordX[0], pieza.coordY[0],
							x1, y1, numgrande, y1);
				}else{
					dFun2 = ArrayOperations.dFunction(pieza.coordX[i+1], pieza.coordY[i+1],
							x1, y1, numgrande, y1);
				}
				









				if( (dFun1 < 0 && dFun2 > 0) ||  (dFun1 > 0 && dFun2 < 0) )
				{
					contador ++;
				}
			}
		}	
		
			

		if (contador % 2 == 0)
		{
			return false;
		}
		return true;  
	}



	
	public static boolean tangenteSegmPieza(int X1, int Y1, int X2, int Y2, Piece pza)
	{
		int vertices = pza.getvertices();
		int Xpza, Ypza;
		for(int i = 0; i < vertices; i++)
		{
			Xpza = pza.coordX[i];
			Ypza = pza.coordY[i];
			if( distPuntoPunto(X2, Y2, Xpza, Ypza) +
				    distPuntoPunto(X1, Y1, Xpza, Ypza)==
					distPuntoPunto(X1, Y1, X2, Y2)  &&
					(X2 != Xpza && Y2 != Ypza) &&
					(X1 != Xpza && Y1 != Ypza)   )
			{
				return true;
			}
		}
			
		return false;    
	}
	
	
	
	

	private static boolean dentroPuntoSegm(int X1, int Y1, int X2, int Y2, int X3, int Y3)
	{
		if( distPuntoPunto(X1, Y1, X2, Y2)+
				distPuntoPunto(X1, Y1, X3, Y3)==
					distPuntoPunto(X2, Y2, X3, Y3) )
		{
			return true;
		}
		return false;    
	}


	

	private static double distPuntoPunto(int X1, int Y1, int X2, int Y2)
	{
		return sqrt(pow(X2-X1, 2)+pow(Y2-Y1, 2));
	}





	private static List<Double> rotacionesAProbar4()
	{
		List<Double> listaAngulos = new LinkedList<Double>();
		listaAngulos.add( (double)0 );
		listaAngulos.add((double)90);
		listaAngulos.add((double)180);
		listaAngulos.add((double)270);
		return listaAngulos;
	}


}


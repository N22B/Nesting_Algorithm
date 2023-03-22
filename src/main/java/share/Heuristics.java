package share;

import java.util.List;


// Selection heuristics.  

public class Heuristics
{
	static Sheet nextObject;
	static Piece pza;
	static int objectsMaximum = 20;	//���ʹ�õĵװ�����
	static int piecesMaximum = 200;	//һ�������Է��õ��������
	static boolean getOut = false;  //ʧ�ܱ��
	private int[][] pieceUnfit  = new int [objectsMaximum][piecesMaximum];  									// ��¼��Щ���ʺϷ�
	private int[][][] pieceUnfit2  = new int [objectsMaximum][piecesMaximum][piecesMaximum];					// ��¼��Щ������ϵĲ��ʺ�
	private int[][][][] pieceUnfit3  = new int [objectsMaximum][piecesMaximum][piecesMaximum][piecesMaximum];	// ��¼��Щ������ϵĲ��ʺ�


	/**
	 * filler
	 */
	public void Filler(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)   
	{
		boolean acomodopieza = false;
		listapiezas = OrderPieces(listapiezas, 1);

		for(int i = 0; i<listapiezas.size(); i++)  //����
		{
			if (acomodopieza)
			{
				break; 
			}
			pza = (Piece)listapiezas.get(i);

			//for (int j = 0; j < listaObjetos.size(); j++)   // For hyper-heuristics
			for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++) 
			{
				nextObject = (Sheet)listaObjetos.get(j);
				if (pza.getTotalSize() <= nextObject.getFreeArea())
				{
					pza.desRotar();
					HeuristicsPlacement acomodo = new HeuristicsPlacement();
					acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
					if (acomodopieza)
					{
						nextObject.addPieza(pza);
						listapiezas.remove(pza);
						break;   
					}	    
				}
			}
		}

		if (acomodopieza == false)
		{
			First_Fit_Decreasing(listapiezas, listaObjetos, xObjeto, yObjeto, H_acomodo); 
		}

		listapiezas = AcomodoOriginalPiezas(listapiezas);
	}


	
	public static void First_Fit(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false; //itemcanpack
		boolean encontroObjeto = false; //bincanpack
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		pza = (Piece)listapiezas.get(0);
		//Search for an object to place the piece
		int n = listaObjetos.size();
		for (int j = 0; j < n; j++) 
		{
			nextObject = (Sheet)listaObjetos.get(j);
			if (pza.getTotalSize() <= nextObject.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
				if (acomodopieza)
				{			            	
					nextObject.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;
				}
			}
		}


		if (!encontroObjeto)
		{
			if(listapiezas.size()>0)
				nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
			if (acomodopieza) 
			{
				nextObject.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	} 



	public void First_Fit_Decreasing(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		pza = SearchGreatest(listapiezas);
		for (int j = 0; j < listaObjetos.size(); j++) 
		{
			nextObject = (Sheet)listaObjetos.get(j);
			if (pza.getTotalSize() <= nextObject.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
				if (acomodopieza)
				{
					nextObject.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;
				}
			}
		}


		if (!encontroObjeto)
		{
			nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
			if (acomodopieza)
			{
				nextObject.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	} 



	public static void First_Fit_Increasing(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		
		pza = SearchSmallest(listapiezas);
		for (int j = 0; j < listaObjetos.size(); j++)
		{
			nextObject = (Sheet)listaObjetos.get(j);
			if (pza.getTotalSize() <= nextObject.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
				if (acomodopieza)
				{
					nextObject.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;
				}
			}
		}


		if (!encontroObjeto)
		{
			nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
			if (acomodopieza)  
			{
				nextObject.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	} 


	// DJD
	public void Djang_and_Finch(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo, double CapInicial, int type)
	{ 	
		int ancho, anchoPza0;
		int numPiezas = listapiezas.size();
		boolean iguales = true;
		Piece pza;
		Sheet obj;
		
		pza = (Piece)listapiezas.get(0);
		anchoPza0 = pza.getxsize();
		
		for(int i=1; i<numPiezas; i++)
		{
			pza = (Piece)listapiezas.get(i);
	   		ancho = pza.getxsize();
	   		if(ancho != anchoPza0)
	   		{
	   			iguales = false;
	   			break;
	   		}
	
		}
		if(iguales)  
			for(int i=0; i<listaObjetos.size(); i++)
			{
				obj = (Sheet)listaObjetos.get(i);
				List<Piece> listapzas = obj.getPzasInside();
				numPiezas = listapzas.size();
				for(int j=0; j<numPiezas; j++)
				{
					pza = (Piece)listapzas.get(j);
			   		ancho = pza.getxsize();
			   		if(ancho != anchoPza0)
			   		{
			   			iguales = false;
			   			break;
			   		}
				}
				if(iguales==false)
				{
					break;
				}
			}

		if(iguales)
		{
			Djang_and_Finch_1D(listapiezas, listaObjetos, xObjeto, yObjeto, H_acomodo, CapInicial, type);
		}else
		{
			if(H_acomodo == "MABR"){
				Djang_and_Finch_2D(listapiezas, listaObjetos, xObjeto, yObjeto, H_acomodo, CapInicial, type);
			}else{
				Djang_and_Finch_2D(listapiezas, listaObjetos, xObjeto, yObjeto, H_acomodo, CapInicial, type);
			}
		}
	}

	
	private void Djang_and_Finch_2D(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo, double CapInicial, int type)
	{ 	
      
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		boolean acomodopieza = false;
		int increment = ((Sheet)listaObjetos.get(0)).gettotalsize() / 20;	// ÿ�����ӵ�waste��1/20
		int w = 0; 															// waste
		listapiezas = OrderPieces(listapiezas, 1);					// ���������
		boolean terminar = false;											// ����ʲôʱ��һ���µ�bin
		getOut = false;

		/**
		 * line 3-4, take the newst bin and fill the bin until 1/3
		 * �õ����µ�һ��bin������1/3
		 */
		//for (int j = 0; j < listaObjetos.size(); j++)   // Hyper-heuristics
		// ������1/3
		for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++)
		{
			nextObject = (Sheet)listaObjetos.get(j);
			// capinicial = 1/3
			if(nextObject.getUsedArea() < nextObject.gettotalsize()*CapInicial)
			{
				for (int i=0; i<listapiezas.size(); i++)   //����
				{
					pza = (Piece)listapiezas.get(i);
					if (pza.getTotalSize() <= nextObject.getFreeArea() )
					{
						pza.desRotar();
						acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
						if (!acomodopieza)
						{
							pieceUnfit[j][i] = 1;   // ��¼���ʺϷŵ�
						}
						if (acomodopieza)
						{
							nextObject.addPieza(pza);
							listapiezas.remove(pza);
							listapiezas = AcomodoOriginalPiezas(listapiezas);  // adjust to the original order
							return;
						}
					}
				}
			}
		}


		/**
		 * ������Ϸ���
		 */
		//for (int j = 0; j < listaObjetos.size(); j++) // hyper-heuristics.
		for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++) 
		{
			nextObject = (Sheet)listaObjetos.get(j);
			w = 0;
			terminar = false;

			if( verificador(listapiezas, nextObject.getFreeArea()) )
			{
				continue;
			}

			do
			{
				// ����һ��
				unapieza(listapiezas, nextObject, H_acomodo, w); 
				if(getOut)
				{
					listapiezas = AcomodoOriginalPiezas(listapiezas);
					return;
				}
				// ��������
				if(listapiezas.size()>1)
				{
					dospiezas(listapiezas, nextObject, H_acomodo, w); 
					if(getOut)
					{
						listapiezas = AcomodoOriginalPiezas(listapiezas);
						return;
					}
				}
				// ��������
				if(listapiezas.size()>2)
				{
					trespiezas(listapiezas, nextObject, H_acomodo, w);
					if(getOut)
					{
						listapiezas = AcomodoOriginalPiezas(listapiezas);
						return;
					}
				}

				// end
				if(w > nextObject.getFreeArea() )
					{terminar = true;}
				w+= increment;  //��w����1ʱ�����Գ��Խ��䳬���������򡣼�����������Ϊ10999������Ϊ1000�������ڳ���w=10000���ٳ���w=11000���Ա����Ƿ������С��999������������Ͽ����ʺϡ�

			}while(!terminar);
		}


		nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto); //���µĵװ�
		pza = SearchGreatest(listapiezas); 							//�ҵ�����һ������ŵ���һ���װ���
		pza.desRotar(); 											//ȡ����ת
		acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
		if (acomodopieza)
		{
			nextObject.addPieza(pza);
			listapiezas.remove(pza);
		}
		// ��������
		listapiezas = AcomodoOriginalPiezas(listapiezas);
	}

	/**
	 * ����Ŷ���DJD
	 */
	private void Djang_and_Finch_2D_Random(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo, double CapInicial, int type)
	{

		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		boolean acomodopieza = false;
		int increment = ((Sheet)listaObjetos.get(0)).gettotalsize() / 20;
		int w = 0; //allowed waste
		listapiezas = OrderPieces(listapiezas, 1);  //descending order
		//�����ķŵ���󣬱���TS011C8����������ҵ��Ǹ������½ǵ��ȷţ��õ��ý���Ŀ����Ծͺܴ�Ҳ����˵������㷨��������ʼ����ķ���
		Piece remove = listapiezas.remove(0);
		listapiezas.add(remove);
		boolean terminar = false;  // decides when to open a new bin.
		getOut = false;

		/**
		 * line 3-4, take the newst bin and fill the bin until 1/3
		 * �õ����µ�һ��bin������1/3
		 */

		//for (int j = 0; j < listaObjetos.size(); j++)   // for Hyper-heuristics
		// ���￪ʼע��
		for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++)
		{
			nextObject = (Sheet)listaObjetos.get(j);
			// capinicial = 1/3
			if(nextObject.getUsedArea() < nextObject.gettotalsize()*CapInicial)
			{
				for (int i=0; i<listapiezas.size(); i++)   //decreasing order of size
				{
					pza = (Piece)listapiezas.get(i);
					if (pza.getTotalSize() <= nextObject.getFreeArea() )
					{
						pza.desRotar();
						acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
						if (!acomodopieza)
						{
							pieceUnfit[j][i] = 1;
						}
						if (acomodopieza)
						{
							nextObject.addPieza(pza);
							listapiezas.remove(pza);
							listapiezas = AcomodoOriginalPiezas(listapiezas);  // adjust to the original order
							return;
						}
					}
				}
			}
		}


		//for (int j = 0; j < listaObjetos.size(); j++) // For hyper-heuristics.
		for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++)
		{
			nextObject = (Sheet)listaObjetos.get(j);
			w = 0;
			terminar = false;

			if( verificador(listapiezas, nextObject.getFreeArea()) )
			{
				continue;
			}

			do
			{
				// try one piece
				unapieza(listapiezas, nextObject, H_acomodo, w);
				if(getOut)
				{
					listapiezas = AcomodoOriginalPiezas(listapiezas);
					return;
				}
				// try two piece
				if(listapiezas.size()>1)
				{
					dospiezas(listapiezas, nextObject, H_acomodo, w);
					if(getOut)
					{
						listapiezas = AcomodoOriginalPiezas(listapiezas);
						return;
					}
				}
				// try three piece
				if(listapiezas.size()>2)
				{
					trespiezas(listapiezas, nextObject, H_acomodo, w);
					if(getOut)
					{
						listapiezas = AcomodoOriginalPiezas(listapiezas);
						return;
					}
				}

				// end
				if(w > nextObject.getFreeArea() )
				{terminar = true;}
				w+= increment;

			}while(!terminar);
		}


		nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto); //���µĵװ�
		pza = SearchGreatest(listapiezas); 							//�ҵ�����һ������ŵ���һ���װ���
		pza.desRotar(); 											//ȡ����ת
		acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
		if (acomodopieza)
		{
			nextObject.addPieza(pza);
			listapiezas.remove(pza);
		}
		// ������
		listapiezas = AcomodoOriginalPiezas(listapiezas);
	}


	public static void Djang_and_Finch_1D(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo, double CapInicial, int type)
	{ 	
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		boolean acomodopieza = false;
		int increment = ((Sheet)listaObjetos.get(0)).gettotalsize() / 20;   
		int w = 0; //allowed waste
		listapiezas = OrderPieces(listapiezas, 1);  //decreasing order of size
		boolean terminar = false;  // decides when to open a new object
		getOut = false;
		

		//for (int j = 0; j < listaObjetos.size(); j++)   // For hyper-heuristics
        for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++)
		{
			nextObject = (Sheet)listaObjetos.get(j);
			if(nextObject.getUsedArea() < nextObject.gettotalsize()*CapInicial)
			{
				for (int i=0; i<listapiezas.size(); i++)   //decreasing order of size
				{
					pza = (Piece)listapiezas.get(i);
					if (pza.getTotalSize() <= nextObject.getFreeArea() )
					{
						pza.desRotar();
						acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
						if (acomodopieza)
						{
							nextObject.addPieza(pza);
							listapiezas.remove(pza);
							listapiezas = AcomodoOriginalPiezas(listapiezas);
							return;
						}
					}
				}
			}
		}



		//for (int j = 0; j < listaObjetos.size(); j++) // For Hyper-heuristics
		for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++) 
		{
			nextObject = (Sheet)listaObjetos.get(j);
			w = 0;
			terminar = false;

			if( verificador(listapiezas, nextObject.getFreeArea()) )
			{
				continue;  //�����free�������Ѿ�û�пռ���Է����κ����������Ҫ�ƶ�����һ��bin��
			}

			do
			{
				unapieza_1D(listapiezas, nextObject, H_acomodo, w);  
				if(getOut)
				{
					listapiezas = AcomodoOriginalPiezas(listapiezas);
					return;
				}
				if(listapiezas.size()>1)
				{
					dospiezas_1D(listapiezas, nextObject, H_acomodo, w);  
					if(getOut)
					{
						listapiezas = AcomodoOriginalPiezas(listapiezas);
						return;
					}
				}
				if(listapiezas.size()>2)
				{
					trespiezas_1D(listapiezas, nextObject, H_acomodo, w);
					if(getOut)
					{
						listapiezas = AcomodoOriginalPiezas(listapiezas);
						return;
					}
				}

				if(w > nextObject.getFreeArea() )
				{terminar = true;}
				w+= increment;

			}while(!terminar);
		}


		nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
		pza = SearchGreatest(listapiezas);
		pza.desRotar();
		acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
		if (acomodopieza)
		{
			nextObject.addPieza(pza);
			listapiezas.remove(pza);
		}
		// For hyper-heuristics (combining heuristics), it is important to leave the pieces in their original order.
		listapiezas = AcomodoOriginalPiezas(listapiezas);
	}


	//Receives an ordered list by piece size (decreasing order)
	private static boolean verificador(List<Piece> listapiezas1, int freearea)
	{
		Piece pza1;
		for(int i=listapiezas1.size()-1; i>=0; i--)
		{
			pza1 = (Piece)listapiezas1.get(i);
			if(pza1.getTotalSize() <= freearea)
			{
				return false;   
			}		
		}
		return true;
	}



	private void unapieza(List<Piece> listapiezas1, Sheet nextObject1, String H_acomodo1, int w)    
	{
		HeuristicsPlacement acomodoP = new HeuristicsPlacement();
		Piece pza1;
		boolean acomodo = false;
		int arealibre;
		int numObj;  
		arealibre = nextObject1.getFreeArea();
		numObj = nextObject1.getNumObjeto(); 

		
		for(int i=0; i<listapiezas1.size(); i++)
		{
			pza1 = (Piece)listapiezas1.get(i);
			if( (arealibre-pza1.getTotalSize()) > w )
			{
				break;  // ����������������Ѿ�û�пռ���Է����κ����������Ҫ�ƶ�����һ������
			}
			if( pza1.getTotalSize() > arealibre 
					|| (pieceUnfit[numObj][i] == 1 ) )  
			{
				continue;
			}

																								   

			pza1.desRotar();
			acomodo = acomodoP.HAcomodo(nextObject1, pza1, H_acomodo1);
			if (!acomodo)
			{
				pieceUnfit[numObj][i] = 1;
			}
			if (acomodo)
			{
				nextObject1.addPieza(pza1);
				listapiezas1.remove(pza1);
				getOut = true;
				return;
			}
		}
		return;
	}


	

	private static void unapieza_1D(List<Piece> listapiezas1, Sheet nextObject1, String H_acomodo1, int w)    
	{
		HeuristicsPlacement acomodoP = new HeuristicsPlacement();
		Piece pza1;
		boolean acomodo = false;
		int arealibre;
		arealibre = nextObject1.getFreeArea();
		
		for(int i=0; i<listapiezas1.size(); i++)
		{
			pza1 = (Piece)listapiezas1.get(i);
			if( (arealibre-pza1.getTotalSize()) > w )
			{
				break;
			}

			pza1.desRotar();
			acomodo = acomodoP.HAcomodo(nextObject1, pza1, H_acomodo1);
			if (acomodo)
			{
				nextObject1.addPieza(pza1);
				listapiezas1.remove(pza1);
				getOut = true;
				return;
			}
		}
		return;
	}


	/**
	 *  try place two pieces
	 */
	private void dospiezas(List<Piece> listapiezas1, Sheet nextObject1, String H_acomodo1, int w1)
	{
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		Piece pza1, pza2;
		boolean acomodo1 = false, acomodo2 = false;
		int area0, area1;  //��������������
		int areaU;		   //g�����С�������minimum piece
		int arealibre;
		int numObj;  
		pza1 = (Piece)listapiezas1.get(listapiezas1.size()-1);  
		areaU = pza1.getTotalSize();
		arealibre = nextObject1.getFreeArea();
		numObj = nextObject1.getNumObjeto();  

		// RECEIVES PIECES IN DESCENDING ORDER OF SIZE.
		pza1 = (Piece)listapiezas1.get(0);
		pza2 = (Piece)listapiezas1.get(1);
		area0 = pza1.getTotalSize();
		area1 = pza2.getTotalSize();
		// ��������������, the first biggest two
		if( (arealibre-area0-area1) > w1)
		{
			return;
		}

		for(int i=0; i<listapiezas1.size(); i++)
		{
			acomodo1 = false;
			acomodo2 = false;
			pza1 = (Piece)listapiezas1.get(i);
			// �ڶ���, line 2
			if(arealibre - pza1.getTotalSize()-area0 > w1)
			{
				break;
			}
			// �����У�line 4
			if(pza1.getTotalSize()+areaU > arealibre  
					 || (pieceUnfit[numObj][i] == 1 ) )  
			{
				continue;
			}

			pza1.desRotar();
			acomodo1 = acomodo.HAcomodo(nextObject1, pza1, H_acomodo1);
			if (acomodo1)
			{
				nextObject1.addPreliminarPieza(pza1);



				// ��10�У� Line 10
				for(int j=0; j<listapiezas1.size(); j++)
				{
					pza2 = (Piece)listapiezas1.get(j);

					if ( (arealibre-pza1.getTotalSize()-pza2.getTotalSize()) >  w1)
					{
						break;
					}

					if ( (pza1.getTotalSize() + pza2.getTotalSize()) > arealibre 
							 || i == j  || pieceUnfit[numObj][j] == 1
							 || pieceUnfit2[numObj][i][j] == 1 )   
					{
						continue;
					}

					pza2.desRotar();
					acomodo2 = acomodo.HAcomodo(nextObject1, pza2, H_acomodo1);
					if (acomodo2)
					{
						nextObject1.removePreliminarPieza(pza1);
						nextObject1.addPieza(pza1);
						nextObject1.addPieza(pza2);
						listapiezas1.remove(pza1);
						listapiezas1.remove(pza2);
						getOut = true;
						return;
					}else{ 
						pieceUnfit2[numObj][i][j] = 1;  //pieces i & j cannot be placed in the object.
					}
				}
				// �����൱��piece2û�ܷŽ�ȥ��Ҳ�ð�piece1ɾ�����о�Ҳ���Էŵ�����
				nextObject1.removePreliminarPieza(pza1);

			} else{
				// ��8�У�line 8
				pieceUnfit[numObj][i] = 1;
			}
		}

		return;
	}

	
	private static void dospiezas_1D(List<Piece> listapiezas1, Sheet nextObject1, String H_acomodo1, int w1)
	{
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		Piece pza1, pza2;
		boolean acomodo1 = false, acomodo2 = false;
		int area0, area1;
		int areaU;
		int arealibre;
		pza1 = (Piece)listapiezas1.get(listapiezas1.size()-1);  
		areaU = pza1.getTotalSize();
		arealibre = nextObject1.getFreeArea();
		

		// RECEIVES PIECES IN DESCENDING ORDER OF SIZE.
		pza1 = (Piece)listapiezas1.get(0);
		pza2 = (Piece)listapiezas1.get(1);
		area0 = pza1.getTotalSize();
		area1 = pza2.getTotalSize();
		if( (arealibre-area0-area1) > w1)
		{
			return;
		}

		for(int i=0; i<listapiezas1.size(); i++)
		{
			acomodo1 = false;
			acomodo2 = false;
			pza1 = (Piece)listapiezas1.get(i);

			if(arealibre - pza1.getTotalSize()-area0 > w1)
			{
				break;
			}

			if(pza1.getTotalSize()+areaU > arealibre )  
			{
				continue;
			}

			pza1.desRotar();
			acomodo1 = acomodo.HAcomodo(nextObject1, pza1, H_acomodo1);
			if (acomodo1)
			{
				nextObject1.addPreliminarPieza(pza1);



				for(int j=0; j<listapiezas1.size(); j++)
				{
					pza2 = (Piece)listapiezas1.get(j);

					if ( (arealibre-pza1.getTotalSize()-pza2.getTotalSize()) >  w1)
					{
						break;
					}
					
					if ( (pza1.getTotalSize() + pza2.getTotalSize()) > arealibre || i==j)  
					{
						continue;
					}

					pza2.desRotar();
					acomodo2 = acomodo.HAcomodo(nextObject1, pza2, H_acomodo1);
					if (acomodo2)
					{
						nextObject1.removePreliminarPieza(pza1);
						nextObject1.addPieza(pza1);
						nextObject1.addPieza(pza2);
						listapiezas1.remove(pza1);
						listapiezas1.remove(pza2);
						getOut = true;
						return;
					}
				}

				nextObject1.removePreliminarPieza(pza1);

			}
		}

		return;
	}


	private void trespiezas(List<Piece> listapiezas1, Sheet nextObject1, String H_acomodo1, int w1)
	{
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		Piece pza1, pza2, pza3;
		boolean acomodo1 = false, acomodo2 = false, acomodo3 = false;
		int area0, area1, area2;
		int areaU1, areaU2;
		int arealibre;
		int numObj;  
		pza1 = (Piece)listapiezas1.get(listapiezas1.size()-1);  
		pza2 = (Piece)listapiezas1.get(listapiezas1.size()-2);  
		areaU1 = pza1.getTotalSize();
		areaU2 = pza2.getTotalSize();
		arealibre = nextObject1.getFreeArea();
		numObj = nextObject1.getNumObjeto(); 


		// RECEIVES PIECES IN DESCENDING ORDER OF SIZE.
		pza1 = (Piece)listapiezas1.get(0);
		pza2 = (Piece)listapiezas1.get(1);
		pza3 = (Piece)listapiezas1.get(2);
		area0 = pza1.getTotalSize();
		area1 = pza2.getTotalSize();
		area2 = pza3.getTotalSize();
		// line2: ��������Ҳ����, the biggest three cannot
		if( (arealibre-area0-area1-area2) > w1)
		{
			return;
		}

		for(int i=0; i<listapiezas1.size(); i++)
		{
			acomodo1 = false;
			acomodo2 = false;
			acomodo3 = false;

			pza1 = (Piece)listapiezas1.get(i);
			if(arealibre-pza1.getTotalSize()-area0-area1 > w1)
			{
				break;
			}
			// line 4: ʣ��ռ�Ų���
			if(pza1.getTotalSize() +areaU1 + areaU2> arealibre 
					 || pieceUnfit[numObj][i] == 1 )   
			{
				continue;
			}

			pza1.desRotar();
			acomodo1 = acomodo.HAcomodo(nextObject1, pza1, H_acomodo1);
			if (acomodo1)
			{
				nextObject1.addPreliminarPieza(pza1);
				for(int j=0; j<listapiezas1.size(); j++)
				{
					pza2 = (Piece)listapiezas1.get(j);

					if(arealibre-pza1.getTotalSize()-pza2.getTotalSize()-area0 > w1)
					{
						break;
					}

					if ( (pza1.getTotalSize() + pza2.getTotalSize()+areaU1) > arealibre  
							 || i == j  || pieceUnfit[numObj][j] == 1
							 || pieceUnfit2[numObj][i][j] == 1 )         
					{     
						continue;
					}

					pza2.desRotar();
					acomodo2 = acomodo.HAcomodo(nextObject1, pza2, H_acomodo1);
					if (acomodo2)
					{
						nextObject1.addPreliminarPieza(pza2);  

						for(int k =0; k<listapiezas1.size(); k++)
						{
							pza3 = (Piece)listapiezas1.get(k);

							if ( (arealibre-pza1.getTotalSize()-pza2.getTotalSize()-pza3.getTotalSize()) > w1)
							{
								break;
							}

							if ( (pza1.getTotalSize()+pza2.getTotalSize()+pza3.getTotalSize()) > arealibre 
									 || i == k || j == k   
									 || pieceUnfit[numObj][k] == 1  			
									 || (pieceUnfit2[numObj][i][k] == 1)     
								     || (pieceUnfit2[numObj][j][k] == 1)		
								     || (pieceUnfit3[numObj][i][j][k] == 1 ) )
							{  
								continue;
							}

							pza3.desRotar();
							acomodo3 = acomodo.HAcomodo(nextObject1, pza3, H_acomodo1);
							if (acomodo3)
							{
								nextObject1.removePreliminarPieza(pza1);
								nextObject1.removePreliminarPieza(pza2);
								nextObject1.addPieza(pza1);
								nextObject1.addPieza(pza2);
								nextObject1.addPieza(pza3);
								listapiezas1.remove(pza1);
								listapiezas1.remove(pza2);
								listapiezas1.remove(pza3);
								getOut = true;
								return;
							}else{
								pieceUnfit3[numObj][i][j][k] = 1;							
							}

						}

						nextObject1.removePreliminarPieza(pza2);
					}else{ 
						pieceUnfit2[numObj][i][j] = 1;  //pieces i & j cannot be placed in the object.
					}

				}

				nextObject1.removePreliminarPieza(pza1);
			}else{ 
				pieceUnfit[numObj][i] = 1;
			}
		}

		return;
	}


	
	private static void trespiezas_1D(List<Piece> listapiezas1, Sheet nextObject1, String H_acomodo1, int w1)
	{
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		Piece pza1, pza2, pza3;
		boolean acomodo1 = false, acomodo2 = false, acomodo3 = false;
		int area0, area1, area2;
		int areaU1, areaU2;
		int arealibre;
		pza1 = (Piece)listapiezas1.get(listapiezas1.size()-1);  
		pza2 = (Piece)listapiezas1.get(listapiezas1.size()-2);  
		areaU1 = pza1.getTotalSize();
		areaU2 = pza2.getTotalSize();
		arealibre = nextObject1.getFreeArea();


		pza1 = (Piece)listapiezas1.get(0);
		pza2 = (Piece)listapiezas1.get(1);
		pza3 = (Piece)listapiezas1.get(2);
		area0 = pza1.getTotalSize();
		area1 = pza2.getTotalSize();
		area2 = pza3.getTotalSize();
		if( (arealibre-area0-area1-area2) > w1)
		{
			return;
		}

		for(int i=0; i<listapiezas1.size(); i++)
		{
			acomodo1 = false;
			acomodo2 = false;
			acomodo3 = false;

			pza1 = (Piece)listapiezas1.get(i);
			if(arealibre-pza1.getTotalSize()-area0-area1 > w1)
			{
				break;
			}
			if(pza1.getTotalSize() +areaU1 + areaU2> arealibre )
			{
				continue;
			}

			pza1.desRotar();
			acomodo1 = acomodo.HAcomodo(nextObject1, pza1, H_acomodo1);
			if (acomodo1)
			{
				nextObject1.addPreliminarPieza(pza1);
				for(int j=0; j<listapiezas1.size(); j++)
				{
					pza2 = (Piece)listapiezas1.get(j);

					if(arealibre-pza1.getTotalSize()-pza2.getTotalSize()-area0 > w1)
					{
						break;
					}

					if ( (pza1.getTotalSize() + pza2.getTotalSize()+areaU1) > arealibre || i == j)
					{      
						continue;
					}

					pza2.desRotar();
					acomodo2 = acomodo.HAcomodo(nextObject1, pza2, H_acomodo1);
					if (acomodo2)
					{
						nextObject1.addPreliminarPieza(pza2);  

						for(int k =0; k<listapiezas1.size(); k++)
						{
							pza3 = (Piece)listapiezas1.get(k);

							if ( (arealibre-pza1.getTotalSize()-pza2.getTotalSize()-pza3.getTotalSize()) > w1)
							{
								break;
							}

							if ( (pza1.getTotalSize()+pza2.getTotalSize()+pza3.getTotalSize()) > arealibre 
									 || i == k || j == k   ) //Same warning from method "dospiezas"
							{   
								continue;
							}

							pza3.desRotar();
							acomodo3 = acomodo.HAcomodo(nextObject1, pza3, H_acomodo1);
							if (acomodo3)
							{
								nextObject1.removePreliminarPieza(pza1);
								nextObject1.removePreliminarPieza(pza2);
								nextObject1.addPieza(pza1);
								nextObject1.addPieza(pza2);
								nextObject1.addPieza(pza3);
								listapiezas1.remove(pza1);
								listapiezas1.remove(pza2);
								listapiezas1.remove(pza3);
								getOut = true;
								return;
							}

						}

						nextObject1.removePreliminarPieza(pza2);
					}

				}

				nextObject1.removePreliminarPieza(pza1);
			}
		}

		return;
	}


	


	public static void Next_Fit(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		pza = (Piece)listapiezas.get(0);
		int j = listaObjetos.size()-1;
		nextObject = (Sheet)listaObjetos.get(j);


		if (pza.getTotalSize() <= nextObject.getFreeArea())
		{
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
			if (acomodopieza)
			{
				nextObject.addPieza(pza);
				listapiezas.remove(pza);
				encontroObjeto = true;
			}
		}

		if (!encontroObjeto)
		{

			nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
			if (acomodopieza)
			{
				nextObject.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	}




	public static void Next_Fit_Decreasing(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		pza = SearchGreatest(listapiezas);
		int j = listaObjetos.size()-1;
		nextObject = (Sheet)listaObjetos.get(j);


		if (pza.getTotalSize() <= nextObject.getFreeArea())
		{
			pza.desRotar(); 
			acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
			if (acomodopieza)
			{
				nextObject.addPieza(pza);
				listapiezas.remove(pza);
				encontroObjeto = true;
			}
		}

		if (!encontroObjeto)
		{

			nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
			if (acomodopieza)
			{
				nextObject.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	}



	public static void Best_Fit(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		int temp;
		Sheet objy, objym;
		int[] ordenObjetos = new int[listaObjetos.size()];
		pza = (Piece)listapiezas.get(0);

		for(int i = 0; i<listaObjetos.size(); i++)
		{
			ordenObjetos[i] = i;
		}
		for(int i =0; i<listaObjetos.size(); i++)
		{
			for(int j = 0; j<listaObjetos.size()-1; j++)
			{
				objy = (Sheet)listaObjetos.get(ordenObjetos[j]);
				objym = (Sheet)listaObjetos.get(ordenObjetos[j+1]);
				if(objy.getFreeArea() > objym.getFreeArea()) 
				{
					temp = ordenObjetos[j];
					ordenObjetos[j] = ordenObjetos[j+1];
					ordenObjetos[j+1] = temp;
				}
			}
		}


		for(int i=0; i<listaObjetos.size(); i++)
		{
			nextObject = (Sheet)listaObjetos.get(ordenObjetos[i]);
			if (pza.getTotalSize() <= nextObject.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
				if(acomodopieza)
				{
					nextObject.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;	 			
				}
			}
		}
		if (!encontroObjeto)
		{
			nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);	
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
			if (acomodopieza)
			{
				nextObject.addPieza(pza);
				listapiezas.remove(pza);
			}
		}

	}




	public void Best_Fit_Decreasing(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		int temp;
		Sheet objy, objym;
		int[] ordenObjetos = new int[listaObjetos.size()];
		pza = SearchGreatest(listapiezas);

		for(int i = 0; i<listaObjetos.size(); i++)
		{
			ordenObjetos[i] = i;
		}
		//ordena objetos por ��rea libre
		for(int i =0; i<listaObjetos.size(); i++)
		{
			for(int j = 0; j<listaObjetos.size()-1; j++)
			{
				objy = (Sheet)listaObjetos.get(ordenObjetos[j]);
				objym = (Sheet)listaObjetos.get(ordenObjetos[j+1]);
				if(objy.getFreeArea() > objym.getFreeArea())  
				{
					temp = ordenObjetos[j];
					ordenObjetos[j] = ordenObjetos[j+1];
					ordenObjetos[j+1] = temp;
				}
			}
		}

		for(int i=0; i<listaObjetos.size(); i++)
		{
			nextObject = (Sheet)listaObjetos.get(ordenObjetos[i]);
			if (pza.getTotalSize() <= nextObject.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
				if(acomodopieza)
				{
					nextObject.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;	 			
				}
			}
		}

		if (!encontroObjeto)
		{
			nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);	
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
			if (acomodopieza)
			{
				nextObject.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	}



	public static void Worst_Fit(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)
	{
		boolean acomodopieza = false;
		boolean encontroObjeto = false;
		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		int temp;
		Sheet objy, objym;
		int[] ordenObjetos = new int[listaObjetos.size()];
		pza = (Piece)listapiezas.get(0);

		for(int i = 0; i<listaObjetos.size(); i++)
		{
			ordenObjetos[i] = i;
		}
		//ordena objetos por ��rea libre
		for(int i =0; i<listaObjetos.size(); i++)
		{
			for(int j = 0; j<listaObjetos.size()-1; j++)
			{
				objy = (Sheet)listaObjetos.get(ordenObjetos[j]);
				objym = (Sheet)listaObjetos.get(ordenObjetos[j+1]);
				if(objy.getFreeArea() < objym.getFreeArea())   
				{
					temp = ordenObjetos[j];
					ordenObjetos[j] = ordenObjetos[j+1];
					ordenObjetos[j+1] = temp;
				}
			}
		}


		for(int i=0; i<listaObjetos.size(); i++)
		{
			nextObject = (Sheet)listaObjetos.get(ordenObjetos[i]);
			if (pza.getTotalSize() <= nextObject.getFreeArea())
			{
				pza.desRotar();
				acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
				if(acomodopieza)
				{
					nextObject.addPieza(pza);
					listapiezas.remove(pza);
					encontroObjeto = true;
					break;	 			
				}
			}
		}

		if (!encontroObjeto)
		{
			nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto);	
			pza.desRotar();
			acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
			if (acomodopieza)
			{
				nextObject.addPieza(pza);
				listapiezas.remove(pza);
			}
		}
	}



	private static Sheet abreNuevoObjeto(List<Sheet> listaObjetos, int xObjeto, int yObjeto)
	{
		int num = listaObjetos.size();
		Sheet nuevoObjeto = new Sheet(xObjeto, yObjeto, num);
		listaObjetos.add(nuevoObjeto);
		return nuevoObjeto;
	}


	// 1: ����,  ����: ����.
	private static List<Piece> OrderPieces(List<Piece> ListaPiezas, int Orderd)
	{
		Piece temporal;
		List<Piece> ListOrdered = ListaPiezas;
		for (int x = 0; x < ListOrdered.size(); x++)
		{
			for(int y=0; y < ListOrdered.size()-1; y++)
			{
				if (Orderd == 1)
				{
					if (((Piece)(ListOrdered.get(y))).getTotalSize()<((Piece)(ListOrdered.get(y+1))).getTotalSize())
					{
						temporal=(Piece)(ListOrdered.get(y));
						ListOrdered.set(y, (Piece)(ListOrdered.get(y+1)));
						ListOrdered.set(y+1, temporal);	
					}
				}
				else
				{         
					if (((Piece)(ListOrdered.get(y))).getTotalSize()>((Piece)(ListOrdered.get(y+1))).getTotalSize())
					{
						temporal=(Piece)(ListOrdered.get(y));
						ListOrdered.set(y, (Piece)(ListOrdered.get(y+1)));
						ListOrdered.set(y+1, temporal);	
					}
				}
			}
		}
		return ListOrdered;
	}


	private static List<Piece> AcomodoOriginalPiezas(List<Piece> ListaPiezas)
	{
		Piece temporal;
		List<Piece> ListaOrdenOriginal = ListaPiezas;
		for (int x = 0; x < ListaOrdenOriginal.size(); x++) 
		{
			for(int y=0; y < ListaOrdenOriginal.size()-1; y++)
			{
				if (((Piece)(ListaOrdenOriginal.get(y))).getnumber()>((Piece)(ListaOrdenOriginal.get(y+1))).getnumber())
				{
					temporal=(Piece)(ListaOrdenOriginal.get(y));
					ListaOrdenOriginal.set(y, (Piece)(ListaOrdenOriginal.get(y+1)));
					ListaOrdenOriginal.set(y+1, temporal);
				}
			}//for
		}//for
		return ListaOrdenOriginal;
	}

	private static Piece SearchGreatest(List<Piece> ListaPiezas)
	{	   
		Piece greatest = (Piece)ListaPiezas.get(0);
		for(int y=0; y < ListaPiezas.size(); y++)     
		{
			if (((Piece)(ListaPiezas.get(y))).getTotalSize()>greatest.getTotalSize())
			{
				greatest = (Piece)(ListaPiezas.get(y));
			}
		}
		return greatest;
	}

	private static Piece SearchSmallest(List<Piece> ListaPiezas)
	{
		Piece smallest = (Piece)ListaPiezas.get(0);
		for(int y=0; y < ListaPiezas.size(); y++)		
		{
			if (((Piece)(ListaPiezas.get(y))).getTotalSize()<(smallest).getTotalSize())
			{
				smallest = (Piece)(ListaPiezas.get(y));
			}
		}
		return smallest;
	}

}


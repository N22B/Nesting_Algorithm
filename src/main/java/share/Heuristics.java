package share;

import java.util.HashSet;
import java.util.List;

public class Heuristics
{
	static Sheet nextObject;
	static Piece pza;
	static int objectsMaximum = 20;	//最多使用的底板数量
	static int piecesMaximum = 200;	//一次最多可以放置的零件数量
	static boolean getOut = false;  //失败标记
	private int[][] pieceUnfit  = new int [objectsMaximum][piecesMaximum];  									// 记录哪些不适合放
	private int[][][] pieceUnfit2  = new int [objectsMaximum][piecesMaximum][piecesMaximum];					// 记录哪些两个组合的不适合
	private int[][][][] pieceUnfit3  = new int [objectsMaximum][piecesMaximum][piecesMaximum][piecesMaximum];	// 记录哪些三个组合的不适合


	/**
	 * filler选择策略
	 */
	public void Filler(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo)   
	{
		boolean acomodopieza = false;
		listapiezas = OrderPieces(listapiezas, 1);

		for(int i = 0; i<listapiezas.size(); i++)  //降序
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

		listapiezas = AdjustOriginalPieces(listapiezas);
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


	// DJD
	private void Djang_and_Finch_2D(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo, double CapInicial, int type)
	{ 	
      
		HeuristicsPlacement placement = new HeuristicsPlacement();
		boolean canPlace = false;
		int increment = ((Sheet)listaObjetos.get(0)).gettotalsize() / 20;	// 每次增加的waste，1/20
		int w = 0; 															// waste
		listapiezas = OrderPieces(listapiezas, 1);					// 按面积降序
		boolean terminar = false;											// 决定什么时候开一个新的bin
		getOut = false;

		/**
		 * line 3-4, take the newst bin and fill the bin until 1/3
		 * 拿到最新的一个bin，填满1/3
		 */
//		for (int j = 0; j < listaObjetos.size(); j++)   // Hyper-heuristics
		// 先填满1/3
		for (int j = listaObjetos.size()-1; j < listaObjetos.size(); j++)
		{
			nextObject = (Sheet)listaObjetos.get(j);
			// capinicial = 1/3
			if(nextObject.getUsedArea() < nextObject.gettotalsize()*CapInicial)
			{
				for (int i=0; i<listapiezas.size(); i++)   //降序
				{
					pza = (Piece)listapiezas.get(i);
					if (pza.getTotalSize() <= nextObject.getFreeArea() )
					{
						pza.desRotar();
						canPlace = placement.HAcomodo(nextObject, pza, H_acomodo);
						if (!canPlace)
						{
							pieceUnfit[j][i] = 1;   // 记录不适合放的
						}
						if (canPlace)
						{
							nextObject.addPieza(pza);
							listapiezas.remove(pza);
							listapiezas = AdjustOriginalPieces(listapiezas);  // adjust to the original order
							return;
						}
					}
				}
			}
		}


		/**
		 * 尝试组合放置
		 */
//		for (int j = 0; j < listaObjetos.size(); j++) // hyper-heuristics.
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
				// 尝试一个
				unapieza(listapiezas, nextObject, H_acomodo, w); 
				if(getOut)
				{
					listapiezas = AdjustOriginalPieces(listapiezas);
					return;
				}
				// 尝试两个
				if(listapiezas.size()>1)
				{
					dospiezas(listapiezas, nextObject, H_acomodo, w); 
					if(getOut)
					{
						listapiezas = AdjustOriginalPieces(listapiezas);
						return;
					}
				}
				// 尝试三个
				if(listapiezas.size()>2)
				{
					trespiezas(listapiezas, nextObject, H_acomodo, w);
					if(getOut)
					{
						listapiezas = AdjustOriginalPieces(listapiezas);
						return;
					}
				}

				// end
				if(w > nextObject.getFreeArea() )
					{terminar = true;}
				w+= increment;  //当w大于1时，可以尝试将其超出自由区域。假设自由区域为10999，增量为1000，建议在尝试w=10000后，再尝试w=11000，以便检查是否有面积小于999的零件或零件组合可以适合。

			}while(!terminar);
		}


		nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto); //开新的底板
		pza = SearchGreatest(listapiezas); 							//找到最大的一块零件放到下一个底板中
		pza.desRotar(); 											//取消旋转
		canPlace = placement.HAcomodo(nextObject, pza, H_acomodo);
		if (canPlace)
		{
			nextObject.addPieza(pza);
			listapiezas.remove(pza);
		}
		// 重新排序
		listapiezas = AdjustOriginalPieces(listapiezas);
	}

	/**
	 * 随机扰动的DJD
	 */
	private void Djang_and_Finch_2D_Random(List<Piece> listapiezas, List<Sheet> listaObjetos, int xObjeto, int yObjeto, String H_acomodo, double CapInicial, int type)
	{

		HeuristicsPlacement acomodo = new HeuristicsPlacement();
		boolean acomodopieza = false;
		int increment = ((Sheet)listaObjetos.get(0)).gettotalsize() / 20;
		int w = 0; //allowed waste
		listapiezas = OrderPieces(listapiezas, 1);  //descending order
		//将最大的放到最后，比如TS011C8，如果我能找到那个最右下角的先放，得到好结果的可能性就很大，也就是说，这个算法很依赖初始零件的放置
		Piece remove = listapiezas.remove(0);
		listapiezas.add(remove);
		boolean terminar = false;  // decides when to open a new bin.
		getOut = false;

		/**
		 * line 3-4, take the newst bin and fill the bin until 1/3
		 * 拿到最新的一个bin，填满1/3
		 */

		//for (int j = 0; j < listaObjetos.size(); j++)   // for Hyper-heuristics
		// 这里开始注释
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
							listapiezas = AdjustOriginalPieces(listapiezas);  // adjust to the original order
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
					listapiezas = AdjustOriginalPieces(listapiezas);
					return;
				}
				// try two piece
				if(listapiezas.size()>1)
				{
					dospiezas(listapiezas, nextObject, H_acomodo, w);
					if(getOut)
					{
						listapiezas = AdjustOriginalPieces(listapiezas);
						return;
					}
				}
				// try three piece
				if(listapiezas.size()>2)
				{
					trespiezas(listapiezas, nextObject, H_acomodo, w);
					if(getOut)
					{
						listapiezas = AdjustOriginalPieces(listapiezas);
						return;
					}
				}

				// end
				if(w > nextObject.getFreeArea() )
				{terminar = true;}
				w+= increment;

			}while(!terminar);
		}


		nextObject=abreNuevoObjeto(listaObjetos, xObjeto, yObjeto); //开新的底板
		pza = SearchGreatest(listapiezas); 							//找到最大的一块零件放到下一个底板中
		pza.desRotar(); 											//取消旋转
		acomodopieza = acomodo.HAcomodo(nextObject, pza, H_acomodo);
		if (acomodopieza)
		{
			nextObject.addPieza(pza);
			listapiezas.remove(pza);
		}
		// 重排序
		listapiezas = AdjustOriginalPieces(listapiezas);
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
							listapiezas = AdjustOriginalPieces(listapiezas);
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
				continue;  //如果在free区域中已经没有空间可以放置任何零件，则需要移动到下一个bin。
			}

			do
			{
				unapieza_1D(listapiezas, nextObject, H_acomodo, w);  
				if(getOut)
				{
					listapiezas = AdjustOriginalPieces(listapiezas);
					return;
				}
				if(listapiezas.size()>1)
				{
					dospiezas_1D(listapiezas, nextObject, H_acomodo, w);  
					if(getOut)
					{
						listapiezas = AdjustOriginalPieces(listapiezas);
						return;
					}
				}
				if(listapiezas.size()>2)
				{
					trespiezas_1D(listapiezas, nextObject, H_acomodo, w);
					if(getOut)
					{
						listapiezas = AdjustOriginalPieces(listapiezas);
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
		listapiezas = AdjustOriginalPieces(listapiezas);
	}


	//判断是否合适
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
				break;  // 如果在自由区域中已经没有空间可以放置任何零件，则需要移动到下一个对象。
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
		int area0, area1;  //面积最大的两个零件
		int areaU;		   //g面积最小的零件，minimum piece
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
		// 最大的两个先试下, the first biggest two
		if( (arealibre-area0-area1) > w1)
		{
			return;
		}

		for(int i=0; i<listapiezas1.size(); i++)
		{
			acomodo1 = false;
			acomodo2 = false;
			pza1 = (Piece)listapiezas1.get(i);
			// 第二行, line 2
			if(arealibre - pza1.getTotalSize()-area0 > w1)
			{
				break;
			}
			// 第四行，line 4
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



				// 第10行， Line 10
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
				// 这里相当于piece2没能放进去，也得把piece1删掉，感觉也可以放到上面
				nextObject1.removePreliminarPieza(pza1);

			} else{
				// 第8行，line 8
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
		// line2: 最大的三个也不行, the biggest three cannot
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
			// line 4: 剩余空间放不下
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
		//ordena objetos por área libre
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
		//ordena objetos por área libre
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


	// 1: 降序,  其他: 升序.
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


	/**
	 * 对零件重新排序
	 */
	private static List<Piece> AdjustOriginalPieces(List<Piece> ListaPiezas)
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


package share;

import jxl.Cell;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;


public class Instance
{
	private List<Sheet> listaObjetos = new LinkedList<Sheet>(); 
    List<Piece> listapiezas = new LinkedList<Piece>();      	//����б�
    List<Piece> listapiezasFijas = new LinkedList<Piece>(); 	//����
    private int xObjeto, yObjeto; 							//Object size
    private int numpiezas;									//���������ѭ����ȡʵ����
    private int noPzasAcomodar; 							//���������ûɶ��
    private int Totalpiezas; 								//������������ûɶ��
 	public ResultVisual[] nuevo; 							//���ӻ����


 	// �ط�����
 	public RePlacement rePlacement = new RePlacement();
 	
	public Instance(int indi)   
	{
		 nuevo = new ResultVisual[indi];
	}

	/**
	 * ��ȡtxt����ʵ��
 	 */
	public void obtainProblem(File file)
	{
		RWfiles rw = new RWfiles();
		int[][] matriz = null;
		try{  
			matriz = rw.obtainMatriz(file, 37);  //���ļ���װ��������
		}
		catch (Exception e){
			System.err.println("��ȡ�ļ����� : "+file);
		}
		
	    Totalpiezas = 0;  
	    numpiezas = matriz[0][0]; 
        
        //Pone las piezas en el arreglo de piezas no acomodadas
	   	for(int m=0; m<numpiezas; m++)
	   	{
	   		int numLados = matriz[0][m+2];
   		 	int[] vertices = new int[numLados*2];
   		 	for(int i=0;i<numLados*2;i+=2){
   		 		vertices[i] = matriz[i+1][m+2];
   		 		vertices[i+1] = matriz[i+2][m+2];
   		 	}
   		 	Piece piece = new Piece(vertices);
		    piece.setnumber(m); 
 			this.Totalpiezas+=piece.getTotalSize();
   			this.listapiezas.add(piece);
   			this.listapiezasFijas.add(piece);
      	}

   	    if(listaObjetos.size()>0)
   	    	listaObjetos.clear(); 
   	    listaObjetos.add(new Sheet(matriz[0][1], matriz[1][1], 0));
   	    xObjeto = matriz[0][1];
   	    yObjeto = matriz[1][1];   
   	    noPzasAcomodar = (int)(listapiezas.size());    
   	    System.out.println("Pieces to place: " + noPzasAcomodar+ " into objects of size "+xObjeto+" x "+yObjeto);  
	}

	public void obtainProblemExcel(File file){
		try {
			Workbook workbook = Workbook.getWorkbook(file);
			jxl.Sheet sheet = workbook.getSheet(0);
			numpiezas = sheet.getRows() - 1; //������������һ��Ϊ����
			int k = 0;
			for(int i = 0; i < numpiezas; i = i+2){ //��i��
				int len = sheet.getRow(i).length;//��ȡ��i�е�cell

				// ����
				List<Integer> list = new ArrayList<>(); //���涥��
				int numofVertices = 0;
				for(int j = 0; j < len; j++){
					if(sheet.getCell(j,i).getContents().isEmpty()){
						break;
					}
					numofVertices++;
					int x = Integer.valueOf(sheet.getCell(j,i).getContents());
					int y = Integer.valueOf(sheet.getCell(j,i+1).getContents());
					list.add(x);
					list.add(y);
				}
				if(list.size() > 0){
					Integer[] vertices = list.toArray(new Integer[numofVertices * 2]);

					Piece piece = new Piece(vertices);
					piece.setnumber(k++);
					this.Totalpiezas+=piece.getTotalSize();
					this.listapiezas.add(piece);
				}
			}
			// ��ȡ�װ峤��
			xObjeto = Integer.valueOf(sheet.getCell(0,numpiezas).getContents());
			yObjeto = Integer.valueOf(sheet.getCell(1,numpiezas).getContents());

		} catch (IOException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param action
	 * @param indi
	 * @param graphVisual
	 * @return
	 */
	public double execute (int action, int indi, boolean graphVisual){
		/**
		 * ����ӽ���������ܣ��м�Ҫ���ؽ�Ͻ�����Խ�������ｨ���Ϊ���̶ģ��Ľ����ƻ����½�ϣ����������ã�����Ϊ1�����ƻ�
		 * ÿһ��ban���ϲ�Ч�����ģ���������������ϲ���Ҳ�������м�ĺϲ����̣�Ҳ����˵�����ɱ������ÿһ�κϲ��Ĺ���
		 * �ø�set�Ϳ���ά���ˣ�set��Ԫ���������˵ĺϲ����id�����Ҽ���һЩ�ָ������������ø�����
		 */
		// �����б����ֵ
		//Set<String> tabuSet = new HashSet<>(3); 		//����ϲ����̵Ľ��ɱ�����Ϊ3
		Queue<String> tabuQueue = new LinkedList<>();	//�ö�����������ɱ�����Ϊ5
		double initThreshold = 2; 						//�ϲ����и��÷֣�������ʼ�ɽ�����ֵ���ܺϲ����ĳɴ���replacement�ĳ�Ա������
		int tabuSize = 3; 								//�����б���

		// ԭʼ������б�Ҫ����
		List<Piece> originPieceList = new ArrayList<>();
		for (Piece piece : listapiezas) { 				//������ԭʼ������б�
			originPieceList.add(piece);
		}
		// ���ְ�͹����Σ��ȵ�����һ��Ч�������ˣ���֪Ϊɶ
		rePlacement.findConvexAndNonconvex(originPieceList, rePlacement.convexList, rePlacement.nonConvexList);


		// ����������ʼ
		List<Double> resultList = new ArrayList<>(); 	//�м����б�
		double bestResult = 0; 							//��ѽ��
		rePlacement.threshold = 2; 						//�����ֵ
		rePlacement.smallThreshold = 1; 				//�����ֵ�������˾Ͳ����ٽ���
		rePlacement.tabuQueue = tabuQueue;
		for(int it = 0; it < 4; it++){
			// 0. ԭʼ�����ֵ
			rePlacement.setOriginPieceList(originPieceList);
			// 1. ����fitness�Ľ�����ֵ
			rePlacement.threshold -= it*0.1; //��ֵ�𽥼�С��ֱ����Ϳɽ�����ֵn��
			if(rePlacement.threshold < rePlacement.smallThreshold){
				// ���ͺ��Ǳ����ڵ���ֵ����������
				//rePlacement.threshold = rePlacement.smallThreshold;
				rePlacement.threshold = initThreshold;
			}
			// 2. ��ϣ����ﻹҪ���ؽ�ϵĽ��
			rePlacement.processUion.clear(); //���һ�ºϲ����
			rePlacement.xObject = xObjeto;
			rePlacement.yObject = yObjeto;
			listapiezas = rePlacement.combineNonConvex();


			// 3.ȡ����ϵĽ������fitness�Ӵ�С����
			List<Map<String, Double>> processUnion = rePlacement.processUion;
			Collections.sort(processUnion, new Comparator<Map<String, Double>>() {
				@Override
				public int compare(Map<String, Double> o1, Map<String, Double> o2) {
					Double f1 = 0d;
					Double f2 = 0d;
					for (Map.Entry<String, Double> entry : o1.entrySet()) {
						f1 = entry.getValue();
					}
					for (Map.Entry<String, Double> entry : o2.entrySet()) {
						f2 = entry.getValue();
					}
					if(f1 - f2 < 0){
						return -1;
					}else{
						return 1;
					}
				}
			});


			// 4.���½��ɱ����ֺ�͸�������Ϊ���һֱ�ظ�����queue��һֱ������
			if(it % tabuSize == 0){
				tabuQueue.poll(); //����������õ�
			}
			// ����ȷ���ϲ����ǿյģ�todo:����ǿյľ�ֱ������?
			if(!processUnion.isEmpty()){
				// ���fitness��С���Ǹ���������ɱ�
				String fit = (String) processUnion.get(0).keySet().stream().toArray()[0];
				// ������ɱ�ĳ���С��3���Ҳ��������������Ԫ�أ��ż���
				if(queueContains(tabuQueue, fit)){ //������ڣ������ѡһ������
					// ���ѡһ���ϲ������еĽ������
					int size = processUnion.size();
					int randomNum = (int) (Math.random() * size);
					fit = (String) processUnion.get(randomNum).keySet().stream().toArray()[0];
					while (tabuQueue.contains(fit)){ //������ظ��ˣ�������
						randomNum = (int) (Math.random() * size);
						fit = (String) processUnion.get(randomNum).keySet().stream().toArray()[0];
					}
					tabuQueue.offer(fit);
				}else{
					tabuQueue.offer(fit); //�����ڲż���
				}
			}
			

			// 5.��ʼ����
			// ׼��������͵װ�
			if(listaObjetos.size()>0)
				listaObjetos.clear(); 
			listaObjetos.add(new Sheet(xObjeto, yObjeto, 0));

			double currentResult; //��ǰ���
			ControlHeuristics control = new ControlHeuristics();
			do
			{
				control.executeHeuristic(listapiezas, listaObjetos, 0);
			}while(listapiezas.size()>0);


			// 6.�����������û������ĵװ�
			for(int i=0; i<listaObjetos.size(); i++)
			{
				Sheet objk = (Sheet)listaObjetos.get(i);
				List<Piece> Lista2 = objk.getPzasInside();
				if(Lista2.size()==0)
					listaObjetos.remove(i);
			}
			// ������
			currentResult=control.calcularAptitud(listaObjetos);
			int ax=(int) (currentResult*1000.0);
			currentResult=(double) ax/1000.0;
			resultList.add(currentResult);
			// ������ѽ��
			if(currentResult > bestResult){
				bestResult = currentResult;
			}


			// 7.��ԭ�������ӵģ�
			for(int i = 0; i < listaObjetos.size(); i++){
				// ��ÿ���װ��ϵ����
				Sheet sheet = listaObjetos.get(i);
				List<Piece> pieceList = sheet.getPzasInside();

				boolean flag = true;
				while(flag){
					boolean happen = false; //������ɾ��
					for(int j = 0; j < pieceList.size(); j++){
						Piece piece = pieceList.get(j);
						if(piece.child.size() > 0) {
							double rotate = piece.getRotada();
							piece.rotateCori(rotate); //��תԭ���꣬�ټ����ƶ��˶���
							int shifx = piece.coordX[0] - piece.getCoriX()[0]; //x������ƶ�����
							int shify = piece.coordY[0] - piece.getCoriY()[0]; //y������ƶ�����
							movereStore(piece, rotate, shifx, shify, pieceList); //�����ӽڵ㶼���뵱ǰ�װ������б�
							pieceList.remove(piece); //ɾ��������ڵ�
							happen = true;
							break; //ֻ�л��к��ӽڵ������ͻ�һֱ�ظ�
						}
					}
					if(happen){ //��ɾ�������ü���ѭ��
						flag = true;
					}else{
						flag = false;
					}
				}
			}


			// 8.���ӻ����
			if (graphVisual){
				Vector<Sheet> listita = new Vector<Sheet>();
				for(int i=0; i<listaObjetos.size(); i++)
				{
					listita.add((Sheet)(listaObjetos.get(i)));
				}
				nuevo[indi] = new ResultVisual(listita);
				nuevo[indi].setSize(700, 650);
				nuevo[indi].setVisible(true);
			}

		}

		// ����һ�鲻��ϵ�
		bestResult = getBestResult(indi, graphVisual, originPieceList, resultList, bestResult);

		System.out.println("process��"+resultList);
		System.out.println("best result��"+bestResult);

		/**
		 * 1.��ϣ�ԭʼ��
		 */
//		if(listaObjetos.size()>0)
//			listaObjetos.clear(); //Limpiar el contenedor
//		listaObjetos.add(new Sheet(xObjeto, yObjeto, 0));
//		rePlacement.setOriginPieceList(originPieceList);
//		listapiezas = rePlacement.combineNonConvex();
//		double aptitud; //action?0??FF-BL??3??DJD-MA
//		ControlHeuristics control = new ControlHeuristics();
//		do
//		{
//			control.ejecutaHeuristica(listapiezas, listaObjetos, 0);
//		}while(listapiezas.size()>0);


		// ����Ӧ�����ӳ���ȥ

		/**
		 * 0.��ʼ��repalcement
		 */
//		rePlacement.setObjectList(listaObjetos);
//		rePlacement.setOriginPieceList(listapiezas);

		/**
		 * 2.��replacement
		 */
//		rePlacement.fillSingleHole(listaObjetos);
//		rePlacement.fillSingleHole(listaObjetos);


		/**
		 * 3.���Ի���һ���ٻ���1or2or3��
		 */
//		rePlacement.changeOne();
//		rePlacement.changeOne();

		/**
		 * 4.���Ի��������ٻ���1or2or3��
		 */
//		rePlacement.changeTwo();

		/**
		 * 3.���Դ��������,
		 * actionΪ0����ʾ�����ϵ�����
		 * actionΪ1����ʾ�����ϵ�����
		 */
//		reExecute(listapiezas, listaObjetos, 1);


		/**
		 * ���
		 */
//		rePlacement.fillSingleHole(listaObjetos);

		/**
		 * �ٽ���
		 */
//		rePlacement.changeOne();

		/**
		 * ��������
		 */
//		reExecute(listapiezas, listaObjetos, 1);
//		rePlacement.fillSingleHole(listaObjetos);
//		rePlacement.fillSingleHole(listaObjetos);
//
//		reExecute(listapiezas, listaObjetos, 1);


		/**
		 * ������1
		 */
		int iter = 1;
		for(int i = 0; i < iter; i++){ //�������в�����ֻ��������ʲ�Ϊ1�ĵװ�
			// 1.��һ���������������ܴӸ������ʻ����������ʣ������Ҫ�ǵ���������������

			// 2.����������������ֻ�����������ߵĵװ壬ֻ��������

			// 3.����������������ֻ�����������ߵĵװ壬ֻ��������

			// 4.������������н�����һ����������һ��or����or����or�ĸ�������ֻ��������
			// ����ɹ����������������������֮��ĵװ����������ĸ����򣩣���������򱣴�
			// ע������ԭ����������ߵĵװ���ܲ���������ˣ���Ϊ�����Դ������õ����ʵ���䵽����λ�ã�����Ӧ��Ҳ���ԣ�ֻ���������ܻ��п�λ
			// ����������ԼӸ��жϣ����ԭ����������ߵĵװ������ʱ���ˣ���ô���Ǹ�������һЩ���

			// 5.������������н���������������������or����or�ĸ�������ֻ������������������ܵ�5��6�����п����и��õĽ���ģ���TS018C8����ߵװ壩
			// ����ɹ��������������֮��ĵװ����������ĸ����򣩣���������򱣴�

			// 6.�����������������ܲ�������������ߵĽ⣬����Ϊ��������Ȼ��������������ŵý�ȥ�����ܣ�����Ų���ȥ��������

		}

		/**
		 * ������2
		 */
		int iter2 = 1;
		for(int i = 0; i < iter2; i++){ //�������в�����ֻ��������ʲ�Ϊ1�ĵװ�
			// 1.��һ���������������ܴӸ������ʻ����������ʣ������Ҫ�ǵ���������������
			// ��䵽��������Ϊֹ

			// 2.����������������ֻ�����������ߵĵװ壬ֻ��������

			// 3.����������������ֻ�����������ߵĵװ壬ֻ��������

			// 4.������������н�����һ����������һ��or����or����or�ĸ�������ֻ��������
			// һֱ������֪����������ߵĵװ�������ʲ������
			// ����з����ɹ����������������������֮��ĵװ����������ĸ����򣩣���������򱣴�
			// ע������ԭ����������ߵĵװ���ܲ���������ˣ���Ϊ�����Դ������õ����ʵ���䵽����λ�ã�����Ӧ��Ҳ���ԣ�ֻ���������ܻ��п�λ
			// ����������ԼӸ��жϣ����ԭ����������ߵĵװ������ʱ���ˣ���ô���Ǹ�������һЩ���
			// ��������������Ϊֹ


			// 6.���԰���������ʵ�������������ʵķţ��Ų�����������

			// 6.�����������������ܲ�������������ߵĽ⣬����Ϊ��������Ȼ��������������ŵý�ȥ�����ܣ�����Ų���ȥ��������

		}

		// ����жϣ����û�иĽ���ʹ��ԭ���Ľ�


		// �����ԭʼ�ģ�ֱ�ӽ⿪����,START
		// delete the bin that cannot pack any item ?????item?bin
		// bestResult = getBestResult(indi, graphVisual, originPieceList, resultList, bestResult);
		// �����ԭʼ�ģ�ֱ�ӽ⿪���У�END

		return bestResult;
	}


	/**
	 * @param action ���а汾
	 * @param indi
	 * @param graphVisual
	 * @return
	 */
	public double executeParallel(int action, int indi, boolean graphVisual){
		// �����̳߳�
		int nThreads = 4;
		CountDownLatch countDownLatch = new CountDownLatch(nThreads);
		ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
		Double[] results = new Double[nThreads];
		// ��ֵ�ķ�����б�
		List<List<Piece>> parallelPieceList = new ArrayList<>();
		for(int j = 0; j < nThreads; j++){
			// ��ֵ����б�
			List<Piece> pieceList = new LinkedList<>();
			for (Piece piece : listapiezas) {
				try {
					pieceList.add((Piece) piece.clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			parallelPieceList.add(pieceList);
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// ��ʼ���м���
		for(int i = 0; i < nThreads; i++){
			int id = i;
			executorService.submit(()->{
				System.out.println("current Thread:" + Thread.currentThread().getName() + " begin");
				// ��¼ÿ���߳����еõ������ֵ��
				results[id] = singleThreadTask(indi, graphVisual, parallelPieceList.get(id));
				countDownLatch.countDown();
				System.out.println("current Thread:" + Thread.currentThread().getName() + " countdown");
			});
		}
		// �ȴ������������
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// ����ÿ���̴߳����������ֵ
//		return Arrays.stream(results).max((d1, d2)->{
//			return d1.compareTo(d2);
//		}).get();
		executorService.shutdown();
		return Arrays.stream(results).max(Double::compareTo).get();
	}

	/**
	 * ÿ���̵߳�����
	 */
	private double singleThreadTask(int indi, boolean graphVisual, List<Piece> originPieceList) {
		// �����б����ֵ
		Queue<String> tabuQueue = new LinkedList<>();	//�ö�����������ɱ�����Ϊ5
		double initThreshold = 2; 						//�ϲ����и��÷֣�������ʼ�ɽ�����ֵ���ܺϲ����ĳɴ���replacement�ĳ�Ա������
		int tabuSize = 5; 								//�����б���

		// ���ְ�͹����Σ��ȵ�����һ��Ч�������ˣ���֪Ϊɶ
		RePlacement rePlacement = new RePlacement();
		rePlacement.findConvexAndNonconvex(originPieceList, rePlacement.convexList, rePlacement.nonConvexList);


		// ����������ʼ
		List<Double> resultList = new ArrayList<>(); 	//�м����б�
		double bestResult = 0; 							//��ѽ��
		rePlacement.threshold = 2; 						//�����ֵ
		rePlacement.smallThreshold = 0.4; 				//�����ֵ�������˾Ͳ����ٽ���
		rePlacement.tabuQueue = tabuQueue;
		int iteration = 5; 								//���д���
		bestResult = tabuSearch(iteration, indi, graphVisual, tabuQueue, initThreshold, tabuSize, originPieceList, resultList, bestResult, rePlacement);

		// ����һ�鲻��ϵ�
		//bestResult = getBestResult(indi, graphVisual, originPieceList, resultList, bestResult);

		System.out.println("current Thread: " + Thread.currentThread().getName() + ".  process:" + resultList + ".  best result:" + bestResult);

		return bestResult;
	}

	private double tabuSearch(int iteration, int indi, boolean graphVisual, Queue<String> tabuQueue, double initThreshold, int tabuSize, List<Piece> originPieceList, List<Double> resultList, double bestResult, RePlacement rePlacement) {
		List<Piece> pieceListParallel = new LinkedList<>();
		for (int it = 0; it < iteration; it++) {
			// 0. ԭʼ�����ֵ
			rePlacement.setOriginPieceList(originPieceList);
			// 1. ����fitness�Ľ�����ֵ
			rePlacement.threshold -= it * 0.1; //��ֵ�𽥼�С��ֱ����Ϳɽ�����ֵn��
			if (rePlacement.threshold < rePlacement.smallThreshold) {
				// ���ͺ��Ǳ����ڵ���ֵ����������
				//rePlacement.threshold = rePlacement.smallThreshold;
				rePlacement.threshold = initThreshold;
			}
			// 2. ��ϣ����ﻹҪ���ؽ�ϵĽ��
			rePlacement.processUion.clear(); //���һ�ºϲ����
			rePlacement.xObject = xObjeto;
			rePlacement.yObject = yObjeto;
			pieceListParallel = rePlacement.combineNonConvex();


			// 3.ȡ����ϵĽ������fitness�Ӵ�С����
			List<Map<String, Double>> processUnion = rePlacement.processUion;
			Collections.sort(processUnion, new Comparator<Map<String, Double>>() {
				@Override
				public int compare(Map<String, Double> o1, Map<String, Double> o2) {
					Double f1 = 0d;
					Double f2 = 0d;
					for (Map.Entry<String, Double> entry : o1.entrySet()) {
						f1 = entry.getValue();
					}
					for (Map.Entry<String, Double> entry : o2.entrySet()) {
						f2 = entry.getValue();
					}
					if (f1 - f2 < 0) {
						return -1;
					} else {
						return 1;
					}
				}
			});


			// 4.���½��ɱ����ֺ�͸�������Ϊ���һֱ�ظ�����queue��һֱ������
			if (it % tabuSize == 0) {
				tabuQueue.poll(); //����������õ�
			}
			// ����ȷ���ϲ����ǿյģ�todo:����ǿյľ�ֱ������?
			if (!processUnion.isEmpty()) {
				// ���fitness��С���Ǹ���������ɱ�
				String fit = (String) processUnion.get(0).keySet().stream().toArray()[0];
				// ������ɱ�ĳ���С��3���Ҳ��������������Ԫ�أ��ż���
				if (queueContains(tabuQueue, fit)) { //������ڣ������ѡһ������
					// ���ѡһ���ϲ������еĽ������
					int size = processUnion.size();
					int randomNum = (int) (Math.random() * size);
					fit = (String) processUnion.get(randomNum).keySet().stream().toArray()[0];
					while (tabuQueue.contains(fit)) { //������ظ��ˣ�������
						randomNum = (int) (Math.random() * size);
						fit = (String) processUnion.get(randomNum).keySet().stream().toArray()[0];
					}
					tabuQueue.offer(fit);
				} else {
					tabuQueue.offer(fit); //�����ڲż���
				}
			}


			// 5.��ʼ����
			// ׼��������͵װ�
			List<Sheet> listaObjetos = new LinkedList<>();
			if (listaObjetos.size() > 0)
				listaObjetos.clear(); //Limpiar el contenedor
			listaObjetos.add(new Sheet(xObjeto, yObjeto, 0));

			double currentResult; //��ǰ���
			ControlHeuristics control = new ControlHeuristics();
			do {
				control.executeHeuristic(pieceListParallel, listaObjetos, 0);
			} while (pieceListParallel.size() > 0);


			// 6.�����������û������ĵװ�
			for (int i = 0; i < listaObjetos.size(); i++) {
				Sheet objk = (Sheet) listaObjetos.get(i);
				List<Piece> Lista2 = objk.getPzasInside();
				if (Lista2.size() == 0)
					listaObjetos.remove(i);
			}
			// ������
			currentResult = control.calcularAptitud(listaObjetos);
			int ax = (int) (currentResult * 1000.0);
			currentResult = (double) ax / 1000.0;
			resultList.add(currentResult);
			// ������ѽ��
			if (currentResult > bestResult) {
				bestResult = currentResult;
			}


			// 7.��ԭ�������ӵģ�
			for (int i = 0; i < listaObjetos.size(); i++) {
				// ��ÿ���װ��ϵ����
				Sheet sheet = listaObjetos.get(i);
				List<Piece> pieceList = sheet.getPzasInside();

				boolean flag = true;
				while (flag) {
					boolean happen = false; //������ɾ��
					for (int j = 0; j < pieceList.size(); j++) {
						Piece piece = pieceList.get(j);
						if (piece.child.size() > 0) {
							double rotate = piece.getRotada();
							piece.rotateCori(rotate); //��תԭ���꣬�ټ����ƶ��˶���
							int shifx = piece.coordX[0] - piece.getCoriX()[0]; //x������ƶ�����
							int shify = piece.coordY[0] - piece.getCoriY()[0]; //y������ƶ�����
							movereStore(piece, rotate, shifx, shify, pieceList); //�����ӽڵ㶼���뵱ǰ�װ������б�
							pieceList.remove(piece); //ɾ��������ڵ�
							happen = true;
							break; //ֻ�л��к��ӽڵ������ͻ�һֱ�ظ�
						}
					}
					if (happen) { //��ɾ�������ü���ѭ��
						flag = true;
					} else {
						flag = false;
					}
				}
			}


			// 8.���ӻ����
			//Get graph of the results:
			ResultVisual[] nuevo = new ResultVisual[4];
			if (graphVisual) {
				Vector<Sheet> listita = new Vector<Sheet>();
				for (int i = 0; i < listaObjetos.size(); i++) {
					listita.add((Sheet) (listaObjetos.get(i)));
				}
				nuevo[indi] = new ResultVisual(listita);
				nuevo[indi].setSize(700, 650);
				nuevo[indi].setVisible(true);
			}

		}
		return bestResult;
	}

	private double getBestResult(int indi, boolean graphVisual, List<Piece> originPieceList, List<Double> resultList, double bestResult) {
		if(listaObjetos.size()>0)
			listaObjetos.clear(); //Limpiar el contenedor
		listaObjetos.add(new Sheet(xObjeto, yObjeto, 0));
		// ���Ҫ����һ�ν�ϵ�
		//rePlacement.setOriginPieceList(originPieceList);
		//listapiezas = rePlacement.combineNonConvex();
		// �������ֵ��listapiezas����
		for (Piece piece : originPieceList) {
			listapiezas.add(piece);
		}

		double aptitud; //action?0??FF-BL??3??DJD-MA
		ControlHeuristics control = new ControlHeuristics();
		do
		{
			control.executeHeuristic(listapiezas, listaObjetos, 0);
		}while(listapiezas.size()>0);

		for(int i=0; i<listaObjetos.size(); i++)
		{
			Sheet objk = (Sheet)listaObjetos.get(i);
			List<Piece> Lista2 = objk.getPzasInside();
			if(Lista2.size()==0)
				listaObjetos.remove(i);
		}

		// ������
		aptitud=control.calcularAptitud(listaObjetos);
		int ax=(int) (aptitud*1000.0);
		aptitud=(double) ax/1000.0;
		resultList.add(aptitud); //��ӵ�����б�
		// ������ѽ��
		if(aptitud > bestResult){
			bestResult = aptitud;
		}

		//Get graph of the results:
		if (graphVisual){
		 Vector<Sheet> listita = new Vector<Sheet>();
		 for(int i=0; i<listaObjetos.size(); i++)
		 {
			listita.add((Sheet)(listaObjetos.get(i)));
		 }
		 nuevo[indi] = new ResultVisual(listita);
		 nuevo[indi].setSize(700, 650);
       	 nuevo[indi].setVisible(true);
       	}
		return bestResult;
	}

	/**
	 * ��piece�ĺ��ӽ�㶼����piecelist����ɾ��ÿ�����ӽ��ĺ��ӽ��
	 */
	private void movereStore(Piece piece, double rotate, int shifx, int shify, List<Piece> pieceList) {
		if(piece.child.size() > 0){
			movereStore(piece.child.get(0), rotate, shifx, shify, pieceList);
			movereStore(piece.child.get(1), rotate, shifx, shify, pieceList);
			//piece.child.clear(); //���ӽ�����󣬾�ɾ��
		}

		if(piece.child.size() == 0){
			// ����ת
			piece.rotate(rotate);
			// x,y����ֱ��ƶ�
			for(int i = 0; i < piece.coordX.length; i++){
				piece.coordX[i] += shifx;
				piece.coordY[i] += shify;
			}
			pieceList.add(piece);
		}

	}


	/**
	 * ֻ���������ʲ����ĵװ�����������
	 */
	private void reExecute(List<Piece> listapiezas, List<Sheet> listaObjetos, int action) {
		listapiezas.clear();
		// ��������Լ��Ѿ�װ���ĵװ�
		List<Sheet> fullObjectList = new ArrayList<>();
		for(int i = 0; i < listaObjetos.size(); i++){
			// ��û��װ��������£���������
			if( i == listaObjetos.size()-1 ){
				fullObjectList.add(listaObjetos.get(i));
				continue;
			}
			// װ��������
			if(listaObjetos.get(i).getFreeArea() == 0){
				fullObjectList.add(listaObjetos.get(i));
				continue;
			}
			List<Piece> pzasInside = listaObjetos.get(i).getPzasInside();
			for(int j = 0; j < pzasInside.size(); j++){
				listapiezas.add(pzasInside.get(j));
			}
		}
		// ��յװ�
		listaObjetos.clear();
		listaObjetos.add(new Sheet(1000, 1000, 0));
		// �����Ű�
		ControlHeuristics control = new ControlHeuristics();
		do
		{
			control.executeHeuristic(listapiezas, listaObjetos, action);
		}while(listapiezas.size()>0);

		// �����ĵװ����¼���
		for(int i = 0; i < fullObjectList.size(); i++){
			listaObjetos.add(fullObjectList.get(i));
		}
	}


	public int numeroObjetos()
	{
		return listaObjetos.size();
	}

	// �ƶ�������������½�
	public void moveAllPieceToBottomLeft(List<Piece> listapiezas) {
		for(int i = 0; i < listapiezas.size(); i++){
			Piece piece = listapiezas.get(i);
			piece.moveToXY(0,0, 2);
			if(piece.child.size() != 0){
				for(int j = 0; j < piece.child.size(); j++){
					piece.child.get(j).moveToXY(0,0,2);
				}
			}
		}
	}

	// �鿴�������Ƿ���Ԫ��
	public boolean queueContains(Queue<String> queue, String str){
		for (String s : queue) {
			if(s.equals(str)){
				return true;
			}
		}
		return false;
	}
	
	
}
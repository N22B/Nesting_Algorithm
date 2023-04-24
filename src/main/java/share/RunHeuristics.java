package share;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RunHeuristics {
	
	public static void main(String[] args) throws Exception{

		// TU002C5C5.txt
		String instances = "listOfInstances";  //待求解的样例列表
        
		// 当前样例路径:
        File dirSingle = new File("C:/Users/zzb/Desktop/NestingData/");

        // 所有样例的路径:
        File dirAllInstance = new File("C:/Users/zzb/Desktop/NestingData/");

        // 所有结果存储:
        File dirSolution = new File("C:/Users/zzb/Desktop/results/"+ instances+"/");

        // 当前结果存储:
        File filePath = new File("C:/Users/zzb/Desktop/NestingData/results/");

        // 参数
        int numHeuristics = 1;			// 使用的启发式算法个数，如MALBR, MALR, MABR, ...
        boolean repetition = true;		// 覆盖之前的结果
        boolean graphVisual = true;		// 可视化图结果

		// 结果存储路径
        if(!filePath.exists())
        	filePath.mkdir(); 			//如果文件夹不存在，新建一个

		// 如果结果路径不存在，新建文件夹
        if(!dirSolution.exists())
        	dirSolution.mkdir();
        
        File archieveProblems = new File(dirSingle,instances+".txt");
        
        System.out.println("Solving instances: "+instances);
        RunHeuristics.run(dirAllInstance, dirSolution, filePath, archieveProblems,
        		instances, numHeuristics, repetition, graphVisual);
        System.out.println("Finish");
        System.out.println();
	}
	
	public RunHeuristics(){}

	public static void run(File dirAllInstance, File dirSolution, File filePath, File archieveProblems,
			String instances, int numHeuristicas, boolean repeticion, boolean graphVisual) throws Exception{
		
		RWfiles rw = new RWfiles();
		List<String> problems = new ArrayList<String>();
		File instancesSolution0 = new File(dirSolution,"solution_"+instances);
		File instancesSolution = new File(dirSolution,"solution_"+instances+".txt");
		
		if(!instancesSolution.exists() || instancesSolution.length() ==0 || repeticion){
			try{
				// 读取listOfInstances，problems为[fuLB.txt, fuMB.txt, ...]
				problems = rw.loadProblems(archieveProblems);
				
			}catch (Exception e){
				System.err.println("读取listOfInstances错误："+archieveProblems);
				System.exit(0);
			}
			
			int numproblems = problems.size();   
			double[][] aptitudes= new double[numproblems][numHeuristicas];  //每个样例的F值
			int[][] numObjects= new int[numproblems][numHeuristicas];       //每个样例所用的底板数量
			int[][] executionTime = new int[numproblems][numHeuristicas];   //每个样例的执行时间

			int indice = 0;
			Iterator<String> iter = problems.iterator();
			while (iter.hasNext()){
				Instance p = new Instance(numproblems*numHeuristicas);	//为每个样例及对应的方法生成一个instance求解
				String problem = iter.next();
				File instancesOut = new File(filePath,"salida_"+problem + ".txt"); //保存结果
				if(!instancesOut.exists() || instancesOut.length()==0 || repeticion==true){
					PrintWriter printerWriter = new PrintWriter(instancesOut);
					// 应用各种启发式求解
					for(int i = 0; i < numHeuristicas; i++){
						System.out.println("Solving "+ problem +" with heuristic "+ i);

						// 读取文件，分为txt和xlxs
						if(problem.endsWith("txt")){
							p.obtainProblem(new File(dirAllInstance,problem));
						}else{ //excel文件
							p.obtainProblemExcel(new File(dirAllInstance, problem));
						}

						// 开始计算，起始时间
						long start = System.currentTimeMillis();
//						aptitudes[indice][i]=p.execute(i, indice, graphVisual); //串行版本（改进djd）
						aptitudes[indice][i]=p.ejecutaAccion(i, indice, graphVisual); //串行版本（原始djd）
						//aptitudes[indice][i]=p.executeParallel(i, indice, graphVisual); //并行版本
						numObjects[indice][i]=p.numeroObjetos();
						long stop = System.currentTimeMillis();
						// 结束计算，终止时间

						executionTime[indice][i]=(int)(stop-start);
						printerWriter.println(i+","+aptitudes[indice][i]+","+numObjects[indice][i]);
					}
					printerWriter.close();

				}
				else{
					try{
						BufferedReader reader = new BufferedReader(new FileReader(instancesOut));
						String line = null;
						String[] lineBreak;
						for(int i=0;i<numHeuristicas;i++){
							line = reader.readLine();
							lineBreak = line.split(",");
							aptitudes[indice][i]=Double.valueOf(lineBreak[1]);
							numObjects[indice][i]=Integer.valueOf(lineBreak[2]);
						}
						reader.close();
					}catch (Exception e){
						System.err.println("错误 : "+instancesOut);
					}
				}
				indice++;
			} 
			//结束
			
			int[] indiceMejores=rw.buscarMejor(aptitudes, numObjects, indice, numHeuristicas);
			try{
				rw.instancesSolution(instancesSolution0, instancesSolution, problems, aptitudes, numObjects, executionTime, indiceMejores, indice);
			}catch (Exception e){
				System.err.println("错误");
				System.exit(0);
			}
			
			
		}
	}


}

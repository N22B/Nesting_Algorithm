package share;

import java.io.BufferedReader;
import java.io.DataOutputStream; 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

// 读写文件
public class RWfiles {

	public RWfiles(){
	}

	public ArrayList<String> loadProblems(File archivo)throws FileNotFoundException,IOException{
		ArrayList<String> archivos = new ArrayList<String>();
		BufferedReader lector = new BufferedReader(new FileReader(archivo));
		String line = null;
		while((line = lector.readLine())!=null){
			archivos.add(line);
		}
		lector.close();
		return archivos;
	}

	public int[][] obtainMatriz(File archivo, int maxvalue) throws FileNotFoundException,IOException{
		BufferedReader lector = new BufferedReader(new FileReader(archivo));
		String line = lector.readLine();
		int ysize = Integer.valueOf(line.trim());  //Gets the matrix size
		int[][] matriz = new int[maxvalue][ysize+2];
		ArrayOperations.zeros(matriz,maxvalue,ysize+2);
		matriz[0][0]=ysize;  
		int renglon = 1;
		String[] lineBreak;
		while((line = lector.readLine())!=null){
			lineBreak = line.split("\\s");     
			for(int columna=1;columna<lineBreak.length;columna++){
				matriz[columna-1][renglon] = Integer.valueOf(lineBreak[columna].trim());
			}
			renglon++;
		}
		return matriz;
	}



	public void instancesSolution(File archivo0, File archivo1, List<String> Problem, double[][] apt, int[][] obj, int[][] exTimes, int[] indices, int indice) throws FileNotFoundException,IOException{
		PrintWriter writer = new PrintWriter(archivo1);
		for(int i=0; i<indice; i++){
			int inx=indices[i];
			writer.println(Problem.get(i)+","+apt[i][inx]+","+obj[i][inx]+","+inx);
		}
		writer.close();
	
		FileOutputStream fptr;
		DataOutputStream f;
		String ap, ob, time;   
		fptr = new FileOutputStream(archivo0+"_aptitudes.txt");
		f = new DataOutputStream(fptr);
		for(int i=0; i<indice; i++)
		{
			f.writeBytes(Problem.get(i));
			for(int j=0; j<apt[0].length; j++)
			{  
				ap = String.valueOf(apt[i][j]);
				f.writeByte(44);   //comma
				f.writeBytes(ap);
			}
			f.writeByte(13);   //enter
		}
		fptr.close();
		fptr = new FileOutputStream(archivo0+"_objetos.txt");
		f = new DataOutputStream(fptr);
		for(int i=0; i<indice; i++)
		{
			f.writeBytes(Problem.get(i));
			for(int j=0; j<obj[0].length; j++)
			{
				ob = String.valueOf(obj[i][j]);
				f.writeByte(44);   //comma
				f.writeBytes(ob);
			}
			f.writeByte(13);   //enter
		}
		fptr.close();
		fptr = new FileOutputStream(archivo0+"_time.txt");
		f = new DataOutputStream(fptr);
		for(int i=0; i<indice; i++)
		{
			f.writeBytes(Problem.get(i));
			for(int j=0; j<exTimes[0].length; j++)
			{
				time = String.valueOf(exTimes[i][j]);
				f.writeByte(44);   //comma
				f.writeBytes(time);
			}
			f.writeByte(13);   //enter
		}
		fptr.close();
	}

	/*  ind1:  Number of instances
	 *  ind2:  Number of heuristics that solves each instance */
	public int[] buscarMejor(double[][] apt, int[][] obj, int ind1, int ind2)
	{
		int[] mejor_ind=new int[ind1];    //best heuristic for each instance
		double[]  mejor_apt=new double[ind1];  //best fitness for each instance
		for(int i=0; i< ind1; i++)
		{
			mejor_ind[i]=0;
			mejor_apt[i]=apt[i][0];
		}
		for(int i=0; i<ind1; i++)
		{
			for(int j=0; j<ind2; j++)
			{
				if(apt[i][j]>mejor_apt[i])
				{
					mejor_ind[i]=j;
					mejor_apt[i]=apt[i][j];
				}
			}
		}
		return mejor_ind;
	}

	
	public void imprimirResultados(PrintWriter archivoSalida,String problema, int objetos, int tiempo, int[] secuencia) throws Exception{
		archivoSalida.print(problema+","+objetos+","+tiempo+",");
		for(int i=0;i<secuencia.length;i++){
			if(i!=secuencia.length-1)
				archivoSalida.print(secuencia[i]+",");
			else
				archivoSalida.println(secuencia[i]);
		}
	}


}
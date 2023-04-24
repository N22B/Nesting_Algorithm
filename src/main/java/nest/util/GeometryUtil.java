package nest.util;

//import com.qunhe.util.nest.data.Bound;
//import com.qunhe.util.nest.data.NestPath;
//import com.qunhe.util.nest.data.Segment;
//import com.qunhe.util.nest.data.SegmentRelation;
import de.lighti.clipper.Clipper;
import de.lighti.clipper.DefaultClipper;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;
import nest.data.*;
import nest.data.Vector;
import share.Piece;

import java.util.*;

//import static com.qunhe.util.nest.util.PlacementworkerDJD.scaleUp2ClipperCoordinates;
//import static com.qunhe.util.nest.util.PlacementworkerDJD.toNestCoordinates;
import static java.lang.Math.*;
import static share.RePlacement.scaleUp2ClipperCoordinates;
import static share.RePlacement.toNestCoordinates;


/**
 *      y
 *      ↑
 *      |
 *      |
 *      |__________ x 项目的坐标系
 *
 */

public class GeometryUtil {
    private static double TOL = Math.pow(10,-2);

    // 在容忍度范围0.01内两者的值基本相等
    public static boolean almostEqual(double a, double b ){
        return Math.abs(a-b)<TOL;
    }

    public static boolean almostEqual(double a , double b  , double tolerance){
        return Math.abs(a-b)<tolerance;
    }

    // 判断是否凸多边形
    public static boolean isConvex(NestPath path){
        int n = path.size();
        boolean flag = false;
        List<Segment> originSegments = path.getSegments();
        List<Segment> segments = new ArrayList<>();
        segments.add(0,null);
        for(int i = 0; i < originSegments.size(); i++){
            segments.add(originSegments.get(i));
        }
        segments.add(path.get(0));
        segments.add(path.get(1));
        for(int i = 2; i <= n+1; i++){
            if(Xji(segments.get(i-1), segments.get(i), segments.get(i+1)) > 0){
                flag = true;
            }
        }
        return !flag;
    }

    // 计算叉积
    public static double Xji(Segment a, Segment b, Segment c)
    {   //ba X bc
        double x1,y1,x2,y2;
        x1=a.x-b.x;
        y1=a.y-b.y;
        x2=c.x-b.x;
        y2=c.y-b.y;
        return x1*y2-x2*y1;
    }

    /**
     * 判断两个零件是否相交
     * @return
     */
    public static boolean interseccionPP(Piece pieza1, Piece pieza2)
    {
        int vertices1 = pieza1.getvertices();
        int vertices2 = pieza2.getvertices();
        boolean value;

        // 1-4????????
        if ( (pieza1.getXmax() <= pieza2.getXmin())
                ||(pieza2.getXmax() <= pieza1.getXmin())
                ||(pieza1.getYmax() <= pieza2.getYmin())
                ||(pieza2.getYmax() <= pieza1.getYmin()) )
        {
            return false;
        }

        // ???n-1??????
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

    public static boolean interseccionSS(int X1, int Y1, int X2, int Y2, int X3, int Y3, int X4, int Y4)
    {
        double m1, m2, x, y;

        // primero se descartan el caso de segmentos que no tienen posibilidad de cruzarse.
        if ( (Math.max(X1, X2) <= Math.min(X3, X4))
                ||(Math.max(X3, X4) <= Math.min(X1, X2))
                ||(Math.max(Y1, Y2) <= Math.min(Y3, Y4))
                ||(Math.max(Y3, Y4) <= Math.min(Y1, Y2)) )
        {
            return false;      //aquí caen los casos de 2 segm verticales: O son paralelos o pertenecen a la misma recta.  Pueden traslaparse o no.
        }

        // solo el primer segmento es vertical
        if (X1 == X2)
        {
            if (Y3 == Y4)     // 2o segmento horizontal.
            {
                if( (X1 < Math.max(X3, X4) && X1 > Math.min(X3, X4))
                        &&(Y3 < Math.max(Y1, Y2) && Y3 > Math.min(Y1, Y2)) )
                {
                    return true;
                }
            }

            m2 = (double)(Y4-Y3)/ (double)(X4-X3);   //pendiente del segmento 2
            y = m2 * (double)(X1 - X3) + (double)(Y3);   // de la recta2: y=m(x-x3)+y3, encontrar la
            // coordenada y que tiene la intersección de los segmentos.
            if( (y < Math.max(Y1, Y2) && y > Math.min(Y1, Y2))
                    && (y < Math.max(Y3, Y4) && y > Math.min(Y3, Y4)) )  //prueba si la coordenada y pertenece a los segmentos (entonces la coordenada x también)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        // solo el 2o segmento es vertical
        if (X3 == X4)
        {
            if (Y1 == Y2)     // 1er segmento horizontal.
            {
                if( (X3 < Math.max(X1, X2) && X3 > Math.min(X1, X2))
                        &&(Y1 < Math.max(Y3, Y4) && Y1 > Math.min(Y3, Y4)) )
                {
                    return true;
                }
            }


            m1 = (double)(Y2-Y1)/ (double)(X2-X1);   //pendiente del segmento 1
            y = m1 * (double)(X3 - X1) + (double)(Y1);   // de la recta2: y=m1(x-x1)+y1, encontrar la
            // coordenada y que tiene la intersección de los segmentos.
            if( (y < Math.max(Y1, Y2) && y > Math.min(Y1, Y2))
                    && (y < Math.max(Y3, Y4) && y > Math.min(Y3, Y4)) )  //prueba si la coordenada y pertenece a los segmentos (entonces la coordenada x también)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        // Ninguna recta es vertical.
        m1 = (double)(Y2-Y1)/ (double)(X2-X1);   //pendiente del segmento 1
        m2 = (double)(Y4-Y3)/ (double)(X4-X3);   //pendiente del segmento 2

        if (m1 == m2)
        {
            return false;   //Segmentos paralelos o q pertenecen a la misma recta.  Pueden traslaparse o no.
        }

        x = (m1*(double)X1 - (double)Y1 - m2*(double)X3 + (double)Y3) / (m1-m2);   //coordenada X del punto de intersección de las dos rectas.
        x = redondeaSiCerca(x);  //el cálculo de las pendientes puede hacer que el punto de intersección quede
        //distorsionado por una factor aprox. de 10E-11, aparentando no estar en el extremo del segmento.

        //Prueba si el punto de intersección está en los segmentos.
        if( (x < Math.max(X1, X2) && x > Math.min(X1, X2))
                && (x < Math.max(X3, X4) && x > Math.min(X3, X4)) )  //prueba si la coordenada X pertenece a los segmentos (entonces la coordenada Y también)
        {
            return true;
        }

        return false;
    }

    public static double redondeaSiCerca(double x)
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


        // 判断零件1是否在零件2内
    public static boolean insidePP(NestPath pieza1, NestPath pieza2){
        Paths polygonOne = new Paths();
        polygonOne.add(scaleUp2ClipperCoordinates(pieza1));

        Paths polygonTwo = new Paths();
        polygonTwo.add(scaleUp2ClipperCoordinates(pieza2));


        Paths remain = new Paths();
        DefaultClipper clipper3 = new DefaultClipper(2); //强简单多边形
        clipper3.addPaths(polygonTwo, Clipper.PolyType.CLIP, true); //裁切two
        clipper3.addPaths(polygonOne, Clipper.PolyType.SUBJECT, true);
        clipper3.execute(Clipper.ClipType.INTERSECTION, remain, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);

        if(remain.size() == 0){ //没有相交结果
            return false;
        }

        List<NestPath> holeNFP = new ArrayList<>();
        for(int j = 0 ; j<remain.size() ; j++){
            // back to normal scale
            holeNFP.add( toNestCoordinates(remain.get(j)));
        }


        if(almostEqual(abs(polygonArea(holeNFP.get(0))), abs(polygonArea(pieza1)))){
            return true;
        }

        return false;

    }

    // 判断零件1是否在零件2内，互相的
    public static boolean insidePP2(NestPath pieza1, NestPath pieza2)
    {

        boolean value;
        int vertices = pieza1.size();
        int vertices2 = pieza2.size();
        double alto;
        double ancho;


        //part1 包围两个零件的矩形不相交，两个零件是否相交已经在外面判断过了
        if(  pieza1.getMaxX() <= pieza2.getMinX() ||
                pieza2.getMaxX() <= pieza1.getMinX() ||
                pieza1.getMaxY() <= pieza2.getMinY() ||
                pieza2.getMaxY() <= pieza1.getMinY() )
        {  //Los rectángulos que las circunscriben no se intersectan.
            return false;
        }

        //part2 计算两个零件的包络矩形，若包络矩形的面积小于两个零件的面积和，证明一个零件在另一个当中
        alto = Math.max(pieza1.getMaxX(), pieza2.getMaxX())-
                Math.min(pieza1.getMinX(), pieza2.getMinX());
        ancho = Math.max(pieza1.getMaxY(), pieza2.getMaxY())-
                Math.min(pieza1.getMinY(), pieza2.getMinY());
        if(alto * ancho < polygonArea(pieza1)  + polygonArea(pieza2))
        {  					//si el rectángulo que encierra las 2 pzas es menor
            return true;    //que la suma del área de las piezas, entonces hay empalme.
        }

        //part4 先判断item1的顶点是否有在item2中的
        for (int j = 0; j < vertices; j++)
        {
//            value = dentroPuntoPieza(pieza1.coordX[j], pieza1.coordY[j], pieza2);
            value = pointInPolygon(pieza1.get(j).x, pieza1.get(j).y, pieza2);
            if(value)
            {
                return true;
            }
        }

        //part4 判断item1边的中点是否有在item2中的，前n-1条边
        for (int j = 0; j < vertices-1; j++)
        {
//            value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2,
//                    (pieza1.coordY[j]+pieza1.coordY[j+1])/2, pieza2);
            value = pointInPolygon((pieza1.get(j).x + pieza1.get(j+1).x)/2,
                    (pieza1.get(j).y + pieza1.get(j+1).y)/2, pieza2);
            if(value)
            {
                return true;
            }
            // Un punto cercano a su punto medio.  Si ese punto cercano está dentro de
            // las 2 figuras
//            value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2 +2,
//                    (pieza1.coordY[j]+pieza1.coordY[j+1])/2 +2, pieza1);
            value = pointInPolygon((pieza1.get(j).x+pieza1.get(j+1).x)/2 +2,
                    (pieza1.get(j).y+pieza1.get(j+1).y)/2 +2, pieza1);
            if(value)
            {
//                value = dentroPuntoPieza((pieza1.coordX[j]+pieza1.coordX[j+1])/2 +2,
//                        (pieza1.coordY[j]+pieza1.coordY[j+1])/2 +2, pieza2);
                value = pointInPolygon((pieza1.get(j).x + pieza1.get(j+1).x)/2 +2,
                        (pieza1.get(j).y+pieza1.get(j+1).y)/2 + 2, pieza2);
                if(value)
                {
                    return true;
                }
            }
        }

        //part4 判断item1边的中点是否有在item2中的，最后一条条边
//        value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2,
//                (pieza1.coordY[vertices-1]+pieza1.coordY[0])/2, pieza2);
        value = pointInPolygon((pieza1.get(vertices-1).x + pieza1.get(0).x)/2,
                (pieza1.get(vertices-1).y + pieza1.get(0).y)/2, pieza2);
        if(value)
        {
            return true;
        }
        // 判断在item1某条边中点附近的点
//        value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2 +2,
//                (pieza1.coordY[vertices-1]+pieza1.coordY[0])/2 +2, pieza1);
        value = pointInPolygon((pieza1.get(vertices-1).x + pieza1.get(0).x)/2 +2,
                (pieza1.get(vertices-1).y + pieza1.get(0).y)/2 +2, pieza1);
        if(value)
        {
//            value = dentroPuntoPieza((pieza1.coordX[vertices-1]+pieza1.coordX[0])/2 +2,
//                    (pieza1.coordY[vertices-1]+pieza1.coordY[0])/2 +2, pieza2);
            value = pointInPolygon((pieza1.get(vertices-1).x + pieza1.get(0).x)/2 +2,
                    (pieza1.get(vertices-1).y + pieza1.get(0).y)/2 +2, pieza2);
            if(value)
            {
                return true;
            }
        }


        //part3 判断item1的中点是否在item2中
//        value = dentroPuntoPieza((pieza1.getXmax()+pieza1.getXmin())/2,
//                (pieza1.getYmax()+pieza1.getYmin())/2, pieza1);
        value = pointInPolygon((pieza1.getMaxX()+pieza1.getMinX())/2,
                (pieza1.getMaxY() + pieza1.getMinY())/2, pieza1);
        if(value)
        {
//            value = dentroPuntoPieza((pieza1.getXmax()+pieza1.getXmin())/2,
//                    (pieza1.getYmax()+pieza1.getYmin())/2, pieza2);
            value = pointInPolygon((pieza1.getMaxX() + pieza1.getMinX())/2,
                    (pieza1.getMaxY() + pieza1.getMinY())/2, pieza2);
            if(value)
            {
                return true;
            }
        }
        return false;
    }


    // 判断点是否在零件内
    public static boolean dentroPuntoPieza(double x1, double y1, NestPath pieza){
        return true;
    }


    // 找到binNFP在combineNFP边或顶点上的点
    public static List<NestPath> intersectPoints(List<NestPath> combineNfp, List<NestPath> binNfp) {
        List<NestPath> pointList = new ArrayList<>();

        NestPath pieza = combineNfp.get(0);     //
        int vertices = pieza.size();            //combineNFP的顶点数

        NestPath pieza2 = binNfp.get(0);
        int vertices2 = pieza2.size();

        /**
         * 判断点是否在线段上，包括端点
         */
        for(int j = 0; j < vertices2; j++){
            double x = pieza2.get(j).x;
            double y = pieza2.get(j).y;
            // 前n-1条边
            boolean value;
            for (int i = 0; i < vertices-1; i++)
            {
                value = dentroPuntoSegm(x, y,
                        pieza.get(i).x, pieza.get(i).y,
                        pieza.get(i+1).x, pieza.get(i+1).y);
                if (value)
                {
                    NestPath temp = new NestPath();
                    temp.add(pieza2.get(j));
                    pointList.add(temp);
                }
            }
            value = dentroPuntoSegm(x, y,
                    pieza.get(vertices-1).x, pieza.get(vertices-1).y,
                    pieza.get(0).x, pieza.get(0).y);
            if (value)
            {
                NestPath temp = new NestPath();
                temp.add(pieza2.get(j));
                pointList.add(temp);
            }
        }
        return pointList;
    }

    // 计算点（x1，y1）是否在线段(x2, y2)-(x3,y3)上
    private static boolean dentroPuntoSegm(double X1, double Y1, double X2, double Y2, double X3, double Y3)
    {
        if( distPuntoPunto(X1, Y1, X2, Y2)+
                distPuntoPunto(X1, Y1, X3, Y3)==
                distPuntoPunto(X2, Y2, X3, Y3) )
        {
            return true;
        }
        return false;
    }



    /**
     * 计算最大邻接长度
     */
    public static double adjancencyOP(NestPath binPolygon, NestPath path, Vector shifvector, List<NestPath> placed, List<Vector> placements){
        double adjancency = 0;

        //先计算和bin的邻接长度
        NestPath newPath = new NestPath();
        for(int n = 0 ; n < path.size(); n++){ //加入每个已经放置的板件的顶点坐标
            newPath.add(new Segment(  path.get(n).x + shifvector.x,
                    path.get(n).y + shifvector.y));
        }
        adjancency += adjancencyPP(binPolygon, newPath);

        // 再判断bin里有无
        if(placed.isEmpty()){
            return adjancency;
        }

        for(int m = 0; m < placed.size(); m++){
            NestPath placedPath = new NestPath();
            for(int n = 0 ; n < placed.get(m).size();n++){ //加入每个已经放置的板件的顶点坐标
                placedPath.add(new Segment(  placed.get(m).get(n).x + placements.get(m).x  ,
                        placed.get(m).get(n).y +placements.get(m).y));
            }
            adjancency += adjancencyPP(placed.get(m), newPath);
        }

        return adjancency;
    }

    /**
     * 计算bin和item的邻接长度
     */
    public static double adjancencyPP(NestPath pieza1, NestPath pieza2){
        int vertices1 = pieza1.size();
        int vertices2 = pieza2.size();
        int adyacencia = 0;

        if ( (pieza1.getMaxX() < pieza2.getMinX())
                ||(pieza2.getMaxX() < pieza1.getMinX())
                ||(pieza1.getMaxY() < pieza2.getMinY())
                ||(pieza2.getMaxY() < pieza1.getMinY()) )
        {
            return 0;
        }

        for (int i = 0; i < vertices1-1; i++)
        {  for (int j = 0; j < vertices2-1; j++)
        {
            adyacencia += adyacenciaSS(pieza1.get(i).x, pieza1.get(i).y,
                    pieza1.get(i+1).x, pieza1.get(i+1).y,
                    pieza2.get(j).x, pieza2.get(j).y,
                    pieza2.get(j+1).x, pieza2.get(j+1).y);
        }
            //vs. último lado de pieza2
            adyacencia += adyacenciaSS(pieza1.get(i).x, pieza1.get(i).y,
                    pieza1.get(i+1).x, pieza1.get(i+1).y,
                    pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y,
                    pieza2.get(0).x, pieza2.get(0).y);
        }


        //último lado de pieza1 vs todos los lados de pieza2 (excepto el último).
        for (int j = 0; j < vertices2-1; j++)
        {
            adyacencia += adyacenciaSS(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y,
                    pieza1.get(0).x, pieza1.get(0).y,
                    pieza2.get(j).x, pieza2.get(j).y,
                    pieza2.get(j+1).x, pieza2.get(j+1).y);
        }

        //último lado de pieza1 vs. último lado de pieza2
        adyacencia += adyacenciaSS(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y,
                pieza1.get(0).x, pieza1.get(0).y,
                pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y,
                pieza2.get(0).x, pieza2.get(0).y);
        return adyacencia;

    }

    /**
     * 线段(x1,y1)-(x2,y2)和(x3,y3)-(x4,y4)的相邻长度
     */
    public static double adyacenciaSS(double X1, double Y1, double X2, double Y2, double X3, double Y3, double X4, double Y4)
    {
        double adyacencia = 0;
        double m1, m2, b1, b2;

        // primero se descartan el caso de segmentos que no tienen posibilidad de ser adyacentes.
        if ( (max(X1, X2) < min(X3, X4))
                ||(max(X3, X4) < min(X1, X2))
                ||(max(Y1, Y2) < min(Y3, Y4))
                ||(max(Y3, Y4) < min(Y1, Y2)) )
        {
            return 0;
        }


        // Dos segmentos verticales (que no se descartaron arriba).
        // Están sobre la misma vertical, hay que ver cuánto se traslapan.
        if (X1 == X2 && X3 == X4)
        {
            // el segm 1 está contenido en el segm 2.
            if(   (Y1 <= Math.max(Y3,Y4)) && (Y1 >= Math.min(Y3,Y4))
                    &&  (Y2 <= Math.max(Y3,Y4)) && (Y2 >= Math.min(Y3,Y4)) )
            {
                return Math.abs(Y2-Y1);
            }
            // el segm 2 está contenido en el segm 1.
            if(   (Y3 <= Math.max(Y1,Y2)) && (Y3 >= Math.min(Y1,Y2))
                    &&  (Y4 <= Math.max(Y1,Y2)) && (Y4 >= Math.min(Y1,Y2)) )
            {
                return Math.abs(Y4-Y3);
            }
            // el segm 1 empieza más arriba que el segm 2.
            if(  Math.max(Y1,Y2) > Math.max(Y3,Y4) )
            {
                adyacencia = Math.max(Y3,Y4) - Math.min(Y1,Y2);
                return adyacencia;
            }
            // el segm 2 empieza más arriba que el segm 1.
            if(  Math.max(Y3,Y4) > Math.max(Y1,Y2) )
            {
                adyacencia = Math.max(Y1,Y2) - Math.min(Y3,Y4);
                return adyacencia;
            }
        }


        if (X1 == X2 || X3 == X4)    // ser adyacentes aunque se crucen).
        {
            return 0;
        }

        // dos segmentos horizontales
        if (Y1 == Y2 && Y3 == Y4)
        {
            // Están sobre la misma horizontal, hay que ver cuánto se traslapan.

            // el segm 1 está contenido en el segm 2.
            if(   (X1 <= Math.max(X3,X4)) && (X1 >= Math.min(X3,X4))
                    &&  (X2 <= Math.max(X3,X4)) && (X2 >= Math.min(X3,X4)) )
            {
                return Math.abs(X2-X1);
            }
            // el segm 2 está contenido en el segm 1.
            if(   (X3 <= Math.max(X1,X2)) && (X3 >= Math.min(X1,X2))
                    &&  (X4 <= Math.max(X1,X2)) && (X4 >= Math.min(X1,X2)) )
            {
                return Math.abs(X4-X3);
            }
            // el segm 1 empieza más a la der que el segm 2.
            if(  Math.max(X1,X2) > Math.max(X3,X4) )
            {
                adyacencia = Math.max(X3,X4) - Math.min(X1,X2);
                return adyacencia;
            }
            // el segm 2 empieza más a la der que el segm 1.
            if(  Math.max(X3,X4) > Math.max(X1,X2) )
            {
                adyacencia = Math.max(X1,X2) - Math.min(X3,X4);
                return adyacencia;
            }
        }


        // Ninguna recta es vertical ni horizontal.
        m1 = (double)(Y2-Y1)/ (double)(X2-X1);   //pendiente del segmento 1
        m2 = (double)(Y4-Y3)/ (double)(X4-X3);   //pendiente del segmento 2
        if (m1 != m2)
        {
            return 0;   //Segmentos sin posibilidad de ser adyacentes.
        }

        b1 = (double)(Y1) - m1*(double)(X1);     //ordenada al origen del segmento 1
        b2 = (double)(Y3) - m2*(double)(X3);     //ordenada al origen del segmento 2
        if (b1 != b2)
        {
            return 0;   //Segmentos paralelos que no pertenecen a la misma recta.
        }

        //Casos de rectas inclinadas donde sí hay traslape.
        // el segm 1 está contenido en el segm 2.
        if(   (Y1 <= Math.max(Y3,Y4)) && (Y1 >= Math.min(Y3,Y4))
                &&  (Y2 <= Math.max(Y3,Y4)) && (Y2 >= Math.min(Y3,Y4)) )
        {
            adyacencia = distPuntoPunto(X1, Y1, X2, Y2);
//            adyacencia = (int)distPuntoPunto(X1, Y1, X2, Y2);
            return adyacencia;
        }
        // el segm 2 está contenido en el segm 1.
        if(   (Y3 <= Math.max(Y1,Y2)) && (Y3 >= Math.min(Y1,Y2))
                &&  (Y4 <= Math.max(Y1,Y2)) && (Y4 >= Math.min(Y1,Y2)) )
        {
//            adyacencia = (int)distPuntoPunto(X3, Y3, X4, Y4);
            adyacencia = distPuntoPunto(X3, Y3, X4, Y4);
            return adyacencia;
        }
        // el segm 1 empieza más arriba que el segm 2.
        if(  Math.max(Y1,Y2) > Math.max(Y3,Y4) )
        {
            if(m1 > 0)
            {
//                adyacencia = (int)distPuntoPunto(max(X3,X4), max(Y3,Y4), min(X1,X2), min(Y1,Y2));
                adyacencia = distPuntoPunto(max(X3,X4), max(Y3,Y4), min(X1,X2), min(Y1,Y2));
                return adyacencia;
            }
//            adyacencia = (int)distPuntoPunto(min(X3,X4), max(Y3,Y4), max(X1,X2), min(Y1,Y2));
            adyacencia = distPuntoPunto(min(X3,X4), max(Y3,Y4), max(X1,X2), min(Y1,Y2));
            return adyacencia;
        }
        // el segm 2 empieza más arriba que el segm 1.
        if(  Math.max(Y3,Y4) > Math.max(Y1,Y2) )
        {
            if(m1 > 0)
            {
//                adyacencia = (int)distPuntoPunto(max(X1,X2), max(Y1,Y2), min(X3,X4), min(Y3,Y4));
                adyacencia = distPuntoPunto(max(X1,X2), max(Y1,Y2), min(X3,X4), min(Y3,Y4));
                return adyacencia;
            }
//            adyacencia = (int)distPuntoPunto(min(X1,X2), max(Y1,Y2), max(X3,X4), min(Y3,Y4));
            adyacencia = distPuntoPunto(min(X1,X2), max(Y1,Y2), max(X3,X4), min(Y3,Y4));
            return adyacencia;
        }
        return adyacencia;
    }


    /**
     * 点到点的距离
     */
    private static double distPuntoPunto(double X1, double Y1, double X2, double Y2)
    {
        return sqrt(pow(X2-X1, 2)+pow(Y2-Y1, 2));
    }

    /**
     * 计算两个零件各个边的重叠比例之和
     */
    public static double adjancencyAllEdgeRatioPP(NestPath pieza1, NestPath pieza2){
        int vertices1 = pieza1.size();
        int vertices2 = pieza2.size();
        double ratio = 0;
        double len1; // 第i条边的长度
        double len2; // 第j条边的长度
        double edgeOverlap; //两条边的重叠长度

        if ( (pieza1.getMaxX() < pieza2.getMinX())
                ||(pieza2.getMaxX() < pieza1.getMinX())
                ||(pieza1.getMaxY() < pieza2.getMinY())
                ||(pieza2.getMaxY() < pieza1.getMinY()) )
        {
            return 0;
        }

        // item1的第i条边
        for (int i = 0; i < vertices1-1; i++)
        {
            len1 = lenofLineSegment(pieza1.get(i).x, pieza1.get(i).y, pieza1.get(i+1).x, pieza1.get(i+1).y);
            // item2的第j条边
            for (int j = 0; j < vertices2-1; j++)
                {
                    len2 = lenofLineSegment(pieza2.get(j).x, pieza2.get(j).y, pieza2.get(j+1).x, pieza2.get(j+1).y);
                    edgeOverlap = adyacenciaSS(pieza1.get(i).x, pieza1.get(i).y,
                            pieza1.get(i+1).x, pieza1.get(i+1).y,
                            pieza2.get(j).x, pieza2.get(j).y,
                            pieza2.get(j+1).x, pieza2.get(j+1).y);
                    ratio += Math.pow(edgeOverlap / (len1 > len2 ? len1:len2),2); //重叠部分除以较大边的长度
                }
                //item2的最后一条边的长度
                len2 = lenofLineSegment(pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y, pieza2.get(0).x, pieza2.get(0).y);
                edgeOverlap = adyacenciaSS(pieza1.get(i).x, pieza1.get(i).y,
                        pieza1.get(i+1).x, pieza1.get(i+1).y,
                        pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y,
                        pieza2.get(0).x, pieza2.get(0).y);
                ratio += Math.pow(edgeOverlap / (len1 > len2 ? len1:len2),2); //重叠部分除以较大边的长度
        }


        // item1的最后一条边和item2的所有边
        for (int j = 0; j < vertices2-1; j++)
        {
            len1 = lenofLineSegment(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y, pieza1.get(0).x, pieza1.get(0).y);
            len2 = lenofLineSegment(pieza2.get(j).x, pieza2.get(j).y, pieza2.get(j+1).x, pieza2.get(j+1).y);
            edgeOverlap = adyacenciaSS(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y,
                    pieza1.get(0).x, pieza1.get(0).y,
                    pieza2.get(j).x, pieza2.get(j).y,
                    pieza2.get(j+1).x, pieza2.get(j+1).y);
            ratio += Math.pow(edgeOverlap / (len1 > len2 ? len1:len2),2); //重叠部分除以较大边的长度
        }

        // item1的最后一条边和item2的最后一条边
        len1 = lenofLineSegment(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y, pieza1.get(0).x, pieza1.get(0).y);
        len2 = lenofLineSegment(pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y, pieza2.get(0).x, pieza2.get(0).y);
        edgeOverlap = adyacenciaSS(pieza1.get(vertices1-1).x, pieza1.get(vertices1-1).y,
                pieza1.get(0).x, pieza1.get(0).y,
                pieza2.get(vertices2-1).x, pieza2.get(vertices2-1).y,
                pieza2.get(0).x, pieza2.get(0).y);
        ratio += Math.pow(edgeOverlap / (len1 > len2 ? len1:len2),2); //重叠部分除以较大边的长度
        return ratio;
    }


    /**
     * 计算线段长度
     */
    public static double lenofLineSegment(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2),2));
    }


    /**
     * 计算多边形面积
     * @param polygon
     * @return
     */
    public static double polygonArea(NestPath polygon){
        double area = 0;
        for(int i = 0  , j = polygon.size()-1; i < polygon.size() ; j = i++){
            Segment si = polygon.getSegments().get(i);
            Segment sj = polygon.getSegments().get(j);
            area += ( sj.getX() +si.getX()) * (sj.getY() - si.getY());
        }
        return Math.abs(0.5*area);
    }

    /**
     * 判断点P是否在边AB上
     * @param A
     * @param B
     * @param p
     * @return
     */
    public static boolean onSegment(Segment A, Segment B , Segment p ){
        // vertical line
        if(almostEqual(A.x, B.x) && almostEqual(p.x, A.x)){
            if(!almostEqual(p.y, B.y) && !almostEqual(p.y, A.y) && p.y < Math.max(B.y, A.y) && p.y > Math.min(B.y, A.y)){
                return true;
            }
            else{
                return false;
            }
        }

        // horizontal line
        if(almostEqual(A.y, B.y) && almostEqual(p.y, A.y)){
            if(!almostEqual(p.x, B.x) && !almostEqual(p.x, A.x) && p.x < Math.max(B.x, A.x) && p.x > Math.min(B.x, A.x)){
                return true;
            }
            else{
                return false;
            }
        }

        //range check
        if((p.x < A.x && p.x < B.x) || (p.x > A.x && p.x > B.x) || (p.y < A.y && p.y < B.y) || (p.y > A.y && p.y > B.y)){
            return false;
        }


        // exclude end points
        if((almostEqual(p.x, A.x) && almostEqual(p.y, A.y)) || (almostEqual(p.x, B.x) && almostEqual(p.y, B.y))){
            return false;
        }

        double cross = (p.y - A.y) * (B.x - A.x) - (p.x - A.x) * (B.y - A.y);

        if(Math.abs(cross) > TOL){
            return false;
        }

        double dot = (p.x - A.x) * (B.x - A.x) + (p.y - A.y)*(B.y - A.y);



        if(dot < 0 || almostEqual(dot, 0)){
            return false;
        }

        double len2 = (B.x - A.x)*(B.x - A.x) + (B.y - A.y)*(B.y - A.y);



        if(dot > len2 || almostEqual(dot, len2)){
            return false;
        }

        return true;

    }

    /**
     * 判断点P是否在多边形polygon上
     * @param point
     * @param polygon
     * @return
     */
    public static Boolean pointInPolygon(Segment point ,NestPath polygon){
        boolean inside = false;
        double offsetx = polygon.offsetX;
        double offsety = polygon.offsetY;

        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j=i++) {
            double xi = polygon.get(i).x + offsetx;
            double yi = polygon.get(i).y + offsety;
            double xj = polygon.get(j).x + offsetx;
            double yj = polygon.get(j).y + offsety;

            if(almostEqual(xi, point.x) && almostEqual(yi, point.y)){
//                return null; // no result
                return false; // no result
            }

            if(onSegment( new Segment(xi,yi),new Segment(xj,yj) , point)){
//                return null ; // exactly on the segment
                return false ; // exactly on the segment
            }

            if(almostEqual(xi, xj) && almostEqual(yi, yj)){ // ignore very small lines
                continue;
            }

            boolean intersect = ((yi > point.y) != (yj > point.y)) && (point.x < (xj - xi) * (point.y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }

        return inside;
    }

    /**
     * 判断点P是否在多边形polygon上，重载
     * @param x,y
     * @param polygon
     * @return
     */
    public static Boolean pointInPolygon(double x, double y,NestPath polygon){
        boolean inside = false;
        double offsetx = polygon.offsetX;
        double offsety = polygon.offsetY;

        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j=i++) {
            double xi = polygon.get(i).x + offsetx;
            double yi = polygon.get(i).y + offsety;
            double xj = polygon.get(j).x + offsetx;
            double yj = polygon.get(j).y + offsety;

            if(almostEqual(xi, x) && almostEqual(yi, y)){
//                return null; // no result
                return false; // no result
            }

            if(onSegment( new Segment(xi,yi),new Segment(xj,yj) , new Segment(x,y))){
//                return null ; // exactly on the segment
                return false ; // exactly on the segment
            }

            if(almostEqual(xi, xj) && almostEqual(yi, yj)){ // ignore very small lines
                continue;
            }

            boolean intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }

        return inside;
    }

    /**
     * 获取多边形的矩形包络边界
     * @param polygon
     * @return
     */
    public static Bound getPolygonBounds(NestPath polygon){

        double xmin = polygon.getSegments().get(0).getX();
        double xmax = polygon.getSegments().get(0).getX();
        double ymin = polygon.getSegments().get(0).getY();
        double ymax = polygon.getSegments().get(0).getY();

        for(int i = 1 ; i <polygon.getSegments().size(); i ++){
            double x = polygon.getSegments().get(i).getX();
            double y = polygon.getSegments().get(i).getY();
            if(x > xmax ){
                xmax = x;
            }
            else if(x < xmin){
                xmin = x;
            }

            if(y > ymax ){
                ymax =y;
            }
            else if(y< ymin ){
                ymin = y;
            }
        }
        return new Bound(xmin,ymin,xmax-xmin , ymax-ymin);
    }

    /**
     * 将多边形旋转一定角度后，返回旋转后多边形的边界
     * @param polygon
     * @param angle
     * @return
     */
    public static Bound rotatePolygon (NestPath polygon ,int angle){
        if(angle == 0 ){
            return getPolygonBounds(polygon);
        }
        double Fangle = angle * Math.PI / 180;
        NestPath rotated = new NestPath();
        for(int i=0; i<polygon.size(); i++){
            double x = polygon.get(i).x;
            double y = polygon.get(i).y;
            double x1 = x*Math.cos(Fangle)-y*Math.sin(Fangle);
            double y1 = x*Math.sin(Fangle)+y*Math.cos(Fangle);
            rotated.add(x1,y1);
        }
        Bound bounds = getPolygonBounds(rotated);
        return bounds;
    }

    /**
     * 将多边形旋转一定角度后，返回该旋转后的多边形
     * @param polygon
     * @param degrees
     * @return
     */
    public static NestPath rotatePolygon2Polygon(NestPath polygon , int degrees ){
        NestPath rotated = new NestPath();
        double angle = degrees * Math.PI / 180;
        for(int i = 0 ; i< polygon.size() ; i++){
            double x = polygon.get(i).x;
            double y = polygon.get(i).y;
            double x1 = x*Math.cos(angle)-y*Math.sin(angle);
            double y1 = x*Math.sin(angle)+y*Math.cos(angle);
            rotated.add(new Segment(x1 , y1));
        }
        rotated.bid = polygon.bid;
        rotated.setId(polygon.getId());
        rotated.setSource(polygon.getSource());
        rotated.setArea(polygon.getArea());
        if(polygon.getChildren().size() > 0 ){
            for(int j = 0 ; j<polygon.getChildren().size() ; j ++){
                rotated.getChildren().add( rotatePolygon2Polygon(polygon.getChildren().get(j) , degrees));
            }
        }
        return rotated;
    }

    /**
     * 判断是否是矩形
     * @param poly
     * @param tolerance
     * @return
     */
    public static boolean isRectangle(NestPath poly , double tolerance){
        Bound bb = getPolygonBounds(poly);

        for(int i = 0 ; i< poly.size();i++){
            if( !almostEqual(poly.get(i).x , bb.getXmin(),tolerance) && ! almostEqual(poly.get(i).x , bb.getXmin() + bb.getWidth(), tolerance)){
                return false;
            }
            if( ! almostEqual(poly.get(i).y , bb.getYmin() ,tolerance) && ! almostEqual(poly.get(i).y , bb.getYmin() + bb.getHeight() ,tolerance)){
                return false;
            }
        }
        return true;
    }

    /**
     * 构建NFP
     * given a static polygon A and a movable polygon B, compute a no fit polygon by orbiting B about A
     * if the inside flag is set, B is orbited inside of A rather than outside
     * if the searchEdges flag is set, all edges of A are explored for NFPs - multiple
     * @param A
     * @param B
     * @param inside
     * @param searchEdges
     * @return
     */
    public static List<NestPath> noFitPolygon(final NestPath A ,final  NestPath B , boolean inside , boolean searchEdges){
        A.setOffsetX(0);
        A.setOffsetY(0);

        double minA = A.get(0).y;
        int minAIndex = 0;
        double currentAX = A.get(0).x;
        double maxB = B.get(0).y;
        int maxBIndex = 0;

        for(int i = 1 ; i< A.size(); i ++){
            A.get(i).marked = false;
            if(almostEqual(A.get(i).y , minA ) && A.get(i).x < currentAX ){
                minA = A.get(i).y;
                minAIndex = i;
                currentAX = A.get(i).x;
            }
            else if(A.get(i).y < minA ){
                minA = A.get(i).y;
                minAIndex = i;
                currentAX = A.get(i).x;
            }
        }
        for(int i  =1 ; i<B.size() ; i ++){
            B.get(i).marked = false;
            if(B.get(i).y >maxB ){
                maxB = B.get(i).y;
                maxBIndex = i;
            }
        }
        Segment startPoint = null ;
        if(!inside){
            startPoint = new Segment(A.get(minAIndex).x - B.get(maxBIndex).x ,
                                     A.get(minAIndex).y - B.get(maxBIndex).y);

        }
        else{
            //TODO heuristic for inside
            startPoint = searchStartPoint(A,B, true , null);

        }

        List<NestPath> NFPlist = new ArrayList<NestPath>();

        while(startPoint != null ){
            Segment prevvector = null;
            B.setOffsetX(startPoint.x);
            B.setOffsetY(startPoint.y);


            List<SegmentRelation> touching;
            NestPath NFP = new NestPath();
            NFP.add(new Segment(B.get(0).x + B.getOffsetX(),
                                B.get(0).y + B.getOffsetY()));

            double referenceX = B.get(0).x + B.getOffsetX();
            double referenceY = B.get(0).y + B.getOffsetY();
            double startX = referenceX;
            double startY = referenceY;
            int counter = 0 ;

            // sanity check  , prevent infinite loop
            while( counter < 10 *( A.size() + B.size())){
                touching = new ArrayList<SegmentRelation>();


                for(int i = 0 ; i <A.size();i++){
                    int nexti = (i == A.size()-1) ? 0 : i +1;
                    for(int j = 0 ; j < B.size() ; j++){
                        int nextj = (j == B.size()-1 ) ? 0: j+1;
                        if(almostEqual(A.get(i).x, B.get(j).x+B.offsetX) && almostEqual(A.get(i).y, B.get(j).y+B.offsetY)){
                            touching.add(new SegmentRelation(0,i,j));
                        }
                        else if(onSegment(A.get(i),A.get(nexti),new Segment(B.get(j).x+B.offsetX, B.get(j).y + B.offsetY))){
                            touching.add(new SegmentRelation(1, nexti,j));
                        }
                        else if(onSegment( new Segment(B.get(j).x +B.offsetX , B.get(j).y +B.offsetY),
                                           new Segment(B.get(nextj).x+B.offsetX , B.get(nextj).y + B.offsetY),
                                            A.get(i))){
                            touching.add( new SegmentRelation(2 , i , nextj));
                        }
                    }
                }


                NestPath vectors = new NestPath();
                for(int i = 0; i < touching.size() ; i++){
                    Segment vertexA = A.get(touching.get(i).A);
                    vertexA.marked = true;

                    int prevAIndex = touching.get(i).A -1;
                    int nextAIndex = touching.get(i).A +1;

                    prevAIndex = (prevAIndex < 0) ? A.size()-1 : prevAIndex; // loop
                    nextAIndex = (nextAIndex >= A.size()) ? 0 : nextAIndex; // loop

                    Segment prevA = A.get(prevAIndex);
                    Segment nextA = A.get(nextAIndex);

                    Segment vertexB = B.get(touching.get(i).B);

                    int prevBIndex = touching.get(i).B -1;
                    int nextBIndex = touching.get(i).B +1;

                    prevBIndex = (prevBIndex < 0) ? B.size()-1 : prevBIndex; // loop
                    nextBIndex = (nextBIndex >= B.size()) ? 0 : nextBIndex; // loop

                    Segment prevB = B.get(prevBIndex);
                    Segment nextB = B.get(nextBIndex);

                    if(touching.get(i).type == 0 ){
                        Segment vA1 = new Segment(prevA.x - vertexA.x , prevA.y - vertexA.y);
                        vA1.start = vertexA ; vA1.end = prevA;

                        Segment vA2 = new Segment(nextA.x - vertexA.x , nextA.y - vertexA.y);
                        vA2.start = vertexA; vA2.end = nextA;

                        Segment vB1 = new Segment(vertexB.x - prevB.x , vertexB.y - prevB.y );
                        vB1.start = prevB; vB1.end = vertexB;

                        Segment vB2 = new Segment(vertexB.x - nextB.x , vertexB.y - nextB.y);
                        vB2.start = nextB ; vB2.end = vertexB;

                        vectors.add(vA1);
                        vectors.add(vA2);
                        vectors.add(vB1);
                        vectors.add(vB2);
                    }
                    else if (touching.get(i).type ==1 ){

                        Segment tmp = new Segment( vertexA.x - (vertexB.x +B.offsetX) ,
                                                    vertexA.y - (vertexB.y +B.offsetY));

                        tmp.start = prevA;
                        tmp.end = vertexA;

                        Segment tmp2 = new Segment(prevA.x-(vertexB.x+B.offsetX) ,prevA.y-(vertexB.y+B.offsetY) );
                        tmp2.start = vertexA ; tmp2.end = prevA;
                        vectors.add(tmp);
                        vectors.add(tmp2);

                    }
                    else if (touching.get(i).type == 2 ){
                        Segment tmp1 = new Segment( vertexA.x - (vertexB.x + B.offsetX) ,
                                                    vertexA.y - (vertexB.y + B.offsetY));
                        tmp1.start = prevB;
                        tmp1.end = vertexB;
                        Segment tmp2 = new Segment(vertexA.x - (prevB.x +B.offsetX),
                                                   vertexA.y - (prevB.y + B.offsetY));
                        tmp2.start = vertexB;
                        tmp2.end = prevB;

                        vectors.add(tmp1); vectors.add(tmp2);
                    }
                }

                Segment translate = null;

                Double maxd = 0.0;
                for(int i = 0 ; i <vectors.size() ; i ++){
                    if(almostEqual(vectors.get(i).x , 0 ) && almostEqual(vectors.get(i).y , 0 ) ){
                        continue;
                    }

                    if(prevvector != null  &&  vectors.get(i).y * prevvector.y + vectors.get(i).x * prevvector.x < 0 ){

                        double vectorlength = Math.sqrt(vectors.get(i).x*vectors.get(i).x+vectors.get(i).y*vectors.get(i).y);
                        Segment unitv = new Segment(vectors.get(i).x/vectorlength , vectors.get(i).y/vectorlength);


                        double prevlength = Math.sqrt(prevvector.x*prevvector.x+prevvector.y*prevvector.y);
                        Segment prevunit = new Segment(prevvector.x / prevlength , prevvector.y / prevlength);


                        // we need to scale down to unit vectors to normalize vector length. Could also just do a tan here
                        if(Math.abs(unitv.y * prevunit.x - unitv.x * prevunit.y) < 0.0001){

                            continue;
                        }
                    }
                    //todo polygonSlideDistance
                    Double d = polygonSlideDistance(A,B, vectors.get(i) , true);

                    double vecd2 = vectors.get(i).x*vectors.get(i).x + vectors.get(i).y*vectors.get(i).y;

                    if(d == null || d*d > vecd2){
                        double vecd = Math.sqrt(vectors.get(i).x*vectors.get(i).x + vectors.get(i).y*vectors.get(i).y);
                        d = vecd;
                    }

                    if(d != null && d > maxd){
                        maxd = d;
                        translate = vectors.get(i);
                    }

                }

                if(translate == null || almostEqual(maxd, 0)){
                    // didn't close the loop, something went wrong here
                    if(translate == null ){

                    }
                    if( almostEqual(maxd ,0 )){
                    }
                    NFP = null;
                    break;
                }

                translate.start.marked = true;
                translate.end.marked = true;

                prevvector = translate;


                // trim
                double vlength2 = translate.x*translate.x + translate.y*translate.y;
                if(maxd*maxd < vlength2 && !almostEqual(maxd*maxd, vlength2)){
                    double scale = Math.sqrt((maxd*maxd)/vlength2);
                    translate.x *= scale;
                    translate.y *= scale;
                }

                referenceX += translate.x;
                referenceY += translate.y;


                if(almostEqual(referenceX, startX) && almostEqual(referenceY, startY)){
                    // we've made a full loop
                    break;
                }

                // if A and B start on a touching horizontal line, the end point may not be the start point
                boolean looped = false;
                if(NFP.size() > 0){
                    for(int i=0; i<NFP.size()-1; i++){
                        if(almostEqual(referenceX, NFP.get(i).x) && almostEqual(referenceY, NFP.get(i).y)){
                            looped = true;
                        }
                    }
                }

                if(looped){
                    // we've made a full loop
                    break;
                }

                NFP.add(new Segment(referenceX,referenceY));

                B.offsetX += translate.x;
                B.offsetY += translate.y;
                counter++;
            }

            if(NFP != null && NFP.size() > 0){
                NFPlist.add(NFP);
            }

            if(!searchEdges){
                // only get outer NFP or first inner NFP
                break;
            }
            startPoint  = searchStartPoint(A,B,inside,NFPlist);
        }
        return NFPlist;
    }

    public static Segment searchStartPoint(NestPath CA ,NestPath CB , boolean inside ,List<NestPath> NFP){

        NestPath A = new NestPath(CA);
        NestPath B = new NestPath(CB);

        if(A.get(0) != A.get(A.size()-1)){
            A.add(A.get(0));
        }

        if(B.get(0) != B.get(B.size()-1)){
            B.add(B.get(0));
        }

        for(int i=0; i<A.size()-1; i++){
            if(!A.get(i).marked){
                A.get(i).marked = true;
                for(int j=0; j<B.size(); j++){
                    B.offsetX = A.get(i).x - B.get(j).x;
                    B.offsetY = A.get(i).y - B.get(j).y;
                    Boolean Binside = null;
                    for(int k=0; k<B.size(); k++){
                        Boolean inpoly = pointInPolygon( new Segment(B.get(k).x +B.offsetX , B.get(k).y +B.offsetY), A);
                        if(inpoly != null){
                            Binside = inpoly;
                            break;
                        }
                    }

                    if(Binside == null){ // A and B are the same
//                        return new Segment(B.offsetX , B.offsetY);
                        return null;
                    }

                    Segment startPoint = new Segment(B.offsetX , B.offsetY);

                    if(((Binside != null  && inside) || (Binside == null && !inside)) && !intersect(A,B) && !inNfp(startPoint, NFP)){
                        return startPoint;
                    }

                    // slide B along vector
                    double vx = A.get(i+1).x - A.get(i).x;
                    double vy = A.get(i+1).y - A.get(i).y;

                    Double d1 = polygonProjectionDistance(A,B, new Segment(vx , vy));
                    Double d2 = polygonProjectionDistance(B,A, new Segment(-vx,-vy));

                    Double d = null;

                    // todo: clean this up
                    if(d1 == null && d2 == null){
                        // nothin
                    }
                    else if(d1 == null){
                        d = d2;
                    }
                    else if(d2 == null){
                        d = d1;
                    }
                    else{
                        d = Math.min(d1,d2);
                    }

                    // only slide until no longer negative
                    // todo: clean this up
                    if(d != null && !almostEqual(d,0) && d > 0){

                    }
                    else{
                        continue;
                    }

                    double vd2 = vx*vx + vy*vy;

                    if(d*d < vd2 && !almostEqual(d*d, vd2)){
                        double vd = Math.sqrt(vx*vx + vy*vy);
                        vx *= d/vd;
                        vy *= d/vd;
                    }

                    B.offsetX += vx;
                    B.offsetY += vy;

                    for(int k=0; k<B.size(); k++){
                        Boolean inpoly = pointInPolygon(new Segment(B.get(k).x +B.offsetX , B.get(k).y +B.offsetY), A);
                        if(inpoly != null){
                            Binside = inpoly;
                            break;
                        }
                    }
                    startPoint = new Segment(B.offsetX,B.offsetY);
                    if(((Binside && inside) || (!Binside && !inside)) && !intersect(A,B) && !inNfp(startPoint, NFP)){
                        return startPoint;
                    }
                }
            }
        }
        return null;
    }


    /**
     *
     * @param p
     * @param nfp
     * @return
     */
    public static boolean inNfp(Segment p , List<NestPath> nfp){
        if(nfp == null ){
            return false;
        }
        for(int i = 0 ;i <nfp.size();i++){
            for(int j = 0 ; j <nfp.get(i).size();j++){
                if(almostEqual(p.x , nfp.get(i).get(j).x ) && almostEqual(p.y , nfp.get(i).get(j).y )){
                    return true;
                }
            }
        }
        return false;
    }

    public static Double polygonProjectionDistance(NestPath CA , NestPath CB , Segment direction){
        double Boffsetx = CB.offsetX ;
        double Boffsety = CB.offsetY ;

        double Aoffsetx = CA.offsetX;
        double Aoffsety = CA.offsetY;

        NestPath A = new NestPath(CA);
        NestPath B = new NestPath(CB);

        if(A.get(0) != A.get(A.size()-1)){
            A.add(A.get(0));
        }

        if(B.get(0)!= B.get(B.size()-1)){
            B.add(B.get(0));
        }

        NestPath edgeA = A;
        NestPath edgeB = B;

        Double distance = null;
        Segment p,s1,s2 = null;
        Double d = null;
        for(int i=0; i<edgeB.size(); i++){
            // the shortest/most negative projection of B onto A
            Double minprojection = null;
            Segment minp = null;
            for(int j=0; j<edgeA.size()-1; j++){
                p = new Segment(edgeB.get(i).x + Boffsetx , edgeB.get(i).y+Boffsety);
                s1 = new Segment(edgeA.get(j).x +Aoffsetx ,edgeA.get(j).y +Aoffsety);
                s2 = new Segment(edgeA.get(j+1).x +Aoffsetx , edgeA.get(j+1).y +Aoffsety);
                if(Math.abs((s2.y-s1.y) * direction.x - (s2.x-s1.x) * direction.y) < TOL){
                    continue;
                }

                // project point, ignore edge boundaries
                d = pointDistance(p, s1, s2, direction , null);

                if(d != null && (minprojection == null || d < minprojection)){
                    minprojection = d;
                    minp = p;
                }
            }
            if(minprojection != null && (distance == null || minprojection > distance)){
                distance = minprojection;
            }
        }
        return distance;
    }

    public static boolean intersect(final NestPath CA,final NestPath CB){
        double Aoffsetx = CA.offsetX ;
        double Aoffsety = CA.offsetY ;

        double Boffsetx = CB.offsetX ;
        double Boffsety = CB.offsetY ;

        NestPath A = new NestPath(CA);
        NestPath B = new NestPath(CB);

        for(int i=0; i<A.size()-1; i++){
            for(int j=0; j<B.size()-1; j++){
                Segment a1 = new Segment( A.get(i).x+Aoffsetx ,A.get(i).y+Aoffsety);
                Segment a2 = new Segment(A.get(i+1).x +Aoffsetx , A.get(i+1).y +Aoffsety);
                Segment b1 = new Segment(B.get(j).x + Boffsetx , B.get(j).y +Boffsety);
                Segment b2 = new Segment(B.get(j+1).x+Boffsetx , B.get(j+1).y+Boffsety);


                int prevbindex = (j == 0) ? B.size()-1 : j-1;
                int prevaindex = (i == 0) ? A.size()-1 : i-1;
                int nextbindex = (j+1 == B.size()-1) ? 0 : j+2;
                int nextaindex = (i+1 == A.size()-1) ? 0 : i+2;

                // go even further back if we happen to hit on a loop end point
                if(B.get(prevbindex) == B.get(j) || (almostEqual(B.get(prevbindex).x, B.get(j).x) && almostEqual(B.get(prevbindex).y, B.get(j).y))){
                    prevbindex = (prevbindex == 0) ? B.size()-1 : prevbindex-1;
                }

                if(A.get(prevaindex) == A.get(i) || (almostEqual(A.get(prevaindex).x, A.get(i).x) && almostEqual(A.get(prevaindex).y, A.get(i).y))){
                    prevaindex = (prevaindex == 0) ? A.size()-1 : prevaindex-1;
                }

                // go even further forward if we happen to hit on a loop end point
                if(B.get(nextbindex) == B.get(j+1) || (almostEqual(B.get(nextbindex).x, B.get(j+1).x) && almostEqual(B.get(nextbindex).y, B.get(j+1).y))){
                    nextbindex = (nextbindex == B.size()-1) ? 0 : nextbindex+1;
                }

                if(A.get(nextaindex) == A.get(i+1) || (almostEqual(A.get(nextaindex).x, A.get(i+1).x) && almostEqual(A.get(nextaindex).y, A.get(i+1).y))){
                    nextaindex = (nextaindex == A.size()-1) ? 0 : nextaindex+1;
                }

                Segment a0 = new Segment(A.get(prevaindex).x +Aoffsetx , A.get(prevaindex).y +Aoffsety);
                Segment b0 = new Segment(B.get(prevbindex).x +Boffsetx ,B.get(prevbindex).y +Boffsety);

                Segment a3 = new Segment(A.get(nextaindex).x + Aoffsetx , A.get(nextaindex).y +Aoffsety);
                Segment b3 = new Segment(B.get(nextbindex).x +Boffsetx , B.get(nextbindex).y +Boffsety);

                if(onSegment(a1,a2,b1) || (almostEqual(a1.x, b1.x , 0.01) && almostEqual(a1.y, b1.y,0.01))){
                    // if a point is on a segment, it could intersect or it could not. Check via the neighboring points
                    Boolean b0in = pointInPolygon(b0, A);
                    Boolean b2in = pointInPolygon(b2, A);
                    if(b0in == null || b2in == null  ){
                        continue;
                    }
                    if((b0in == true && b2in == false) || (b0in == false && b2in == true)){

                        return true;
                    }
                    else{
                        continue;
                    }
                }

                if(onSegment(a1,a2,b2) || (almostEqual(a2.x, b2.x) && almostEqual(a2.y, b2.y))){
                    // if a point is on a segment, it could intersect or it could not. Check via the neighboring points
                    Boolean b1in = pointInPolygon(b1, A);
                    Boolean b3in = pointInPolygon(b3, A);
                    if(b1in == null || b3in == null){
                        continue;
                    }
                    if((b1in == true && b3in == false) || (b1in == false && b3in == true)){

                        return true;
                    }
                    else{
                        continue;
                    }
                }

                if(onSegment(b1,b2,a1) || (almostEqual(a1.x, b2.x) && almostEqual(a1.y, b2.y))){
                    // if a point is on a segment, it could intersect or it could not. Check via the neighboring points
                    Boolean a0in = pointInPolygon(a0, B);
                    Boolean a2in = pointInPolygon(a2, B);
                    if(a0in == null || a2in == null ){
                        continue;
                    }
                    if((a0in == true && a2in == false) || (a0in == false && a2in == true)){

                        return true;
                    }
                    else{
                        continue;
                    }
                }

                if(onSegment(b1,b2,a2) || (almostEqual(a2.x, b1.x) && almostEqual(a2.y, b1.y))){
                    // if a point is on a segment, it could intersect or it could not. Check via the neighboring points
                    Boolean a1in = pointInPolygon(a1, B);
                    Boolean a3in = pointInPolygon(a3, B);
                    if(a1in == null || a3in == null ){
                        continue;
                    }

                    if((a1in == true && a3in == false) || (a1in == false && a3in == true)){

                        return true;
                    }
                    else{
                        continue;
                    }
                }

                Segment p = lineIntersect(b1, b2, a1, a2 ,null);

                if(p != null){

                    return true;
                }
            }
        }

        return false;
    }

    public static Segment lineIntersect(Segment A ,Segment B ,Segment E ,Segment F , Boolean infinite){
        double a1, a2, b1, b2, c1, c2, x, y;

        a1= B.y-A.y;
        b1= A.x-B.x;
        c1= B.x*A.y - A.x*B.y;
        a2= F.y-E.y;
        b2= E.x-F.x;
        c2= F.x*E.y - E.x*F.y;

        double denom=a1*b2 - a2*b1;

        x = (b1*c2 - b2*c1)/denom;
        y = (a2*c1 - a1*c2)/denom;

        if( !Double.isFinite(x) || !Double.isFinite(y)){
//            System.out.println(" not infi ");
            return null;
        }

        if(infinite== null || !infinite){
            // coincident points do not count as intersecting
            if (Math.abs(A.x-B.x) > TOL && (( A.x < B.x ) ? x < A.x || x > B.x : x > A.x || x < B.x )) return null;
            if (Math.abs(A.y-B.y) > TOL && (( A.y < B.y ) ? y < A.y || y > B.y : y > A.y || y < B.y )) return null;

            if (Math.abs(E.x-F.x) > TOL && (( E.x < F.x ) ? x < E.x || x > F.x : x > E.x || x < F.x )) return null;
            if (Math.abs(E.y-F.y) > TOL && (( E.y < F.y ) ? y < E.y || y > F.y : y > E.y || y < F.y )) return null;
        }
        return new Segment(x,y);
    }

    public static Double polygonSlideDistance(final NestPath TA ,final NestPath TB , Segment direction , boolean ignoreNegative ){
        double Aoffsetx = TA.offsetX;
        double Aoffsety = TA.offsetY;

        double Boffsetx = TB.offsetX;
        double BoffsetY = TB.offsetY;

        NestPath A = new NestPath(TA);
        NestPath B = new NestPath(TB);

        if(A.get(0 ) != A.get(A.size()-1)){
            A.add(A.get(0));
        }
        if(B.get(0) != B.get(B.size() -1 )){
            B.add(B.get(0));
        }

        NestPath edgeA = A;
        NestPath edgeB = B;

        Double distance = null;


        Segment dir = normalizeVector(direction);

        Segment normal = new Segment(dir.y , -dir.x);

        Segment reverse = new Segment(-dir.x , -dir.y );

        Segment A1,A2 ,B1,B2 = null;
        for(int i = 0; i <edgeB.size() - 1 ; i++){
            for(int j = 0 ; j< edgeA.size() -1 ; j ++){
                A1 = new Segment(edgeA.get(j).x + Aoffsetx , edgeA.get(j).y +Aoffsety);
                A2 = new Segment(edgeA.get(j+1) .x +Aoffsetx , edgeA.get(j+1).y +Aoffsety );
                B1 = new Segment(edgeB.get(i).x + Boffsetx , edgeB.get(i).y +BoffsetY );
                B2 = new Segment(edgeB.get(i+1).x +Boffsetx , edgeB.get(i+1).y +BoffsetY);

                if( (almostEqual(A1.x ,A2.x ) && almostEqual(A1.y , A2.y )) || (almostEqual(B1.x,B2.x ) &&almostEqual(B1.y ,B2.y))){
                    continue;
                }
                Double d = segmentDistance(A1,A2,B1,B2 ,dir);

                if(d != null && (distance == null || d < distance)){
                    if(!ignoreNegative || d > 0 || almostEqual(d, 0)){
                        distance = d;
                    }
                }
            }
        }
        return distance;
    }

    public static Segment normalizeVector(Segment v ){
        if( almostEqual(v.x * v.x + v.y * v.y , 1)){
            return v;
        }
        double len = Math.sqrt(v.x * v.x + v.y *v.y);
        double inverse = 1/len;

        return new Segment(v.x * inverse , v.y * inverse);
    }

    public static Double segmentDistance (Segment A ,Segment B ,Segment E ,Segment F ,Segment direction ){
        double SEGTOL = 10E-4;
        Segment normal = new Segment( direction.y , - direction.x );

        Segment reverse = new Segment( -direction.x , -direction.y );

        double dotA = A.x*normal.x + A.y*normal.y;
        double dotB = B.x*normal.x + B.y*normal.y;
        double dotE = E.x*normal.x + E.y*normal.y;
        double dotF = F.x*normal.x + F.y*normal.y;

        double crossA = A.x*direction.x + A.y*direction.y;
        double crossB = B.x*direction.x + B.y*direction.y;
        double crossE = E.x*direction.x + E.y*direction.y;
        double crossF = F.x*direction.x + F.y*direction.y;
        double crossABmin = Math.min(crossA,crossB);
        double crossABmax = Math.max(crossA,crossB);

        double crossEFmax = Math.max(crossE,crossF);
        double crossEFmin = Math.min(crossE,crossF);

        double ABmin = Math.min(dotA,dotB);
        double ABmax = Math.max(dotA,dotB);

        double EFmax = Math.max(dotE,dotF);
        double EFmin = Math.min(dotE,dotF);

        if(almostEqual(ABmax, EFmin,SEGTOL) || almostEqual(ABmin, EFmax,SEGTOL)){
            return null;
        }
        // segments miss eachother completely
        if(ABmax < EFmin || ABmin > EFmax){
            return null;
        }
        double overlap ;
        if((ABmax > EFmax && ABmin < EFmin) || (EFmax > ABmax && EFmin < ABmin)){
            overlap = 1;
        }
        else{
            double minMax = Math.min(ABmax, EFmax);
            double maxMin = Math.max(ABmin, EFmin);

            double maxMax = Math.max(ABmax, EFmax);
            double minMin = Math.min(ABmin, EFmin);

            overlap = (minMax-maxMin)/(maxMax-minMin);
        }
        double crossABE = (E.y - A.y) * (B.x - A.x) - (E.x - A.x) * (B.y - A.y);
        double crossABF = (F.y - A.y) * (B.x - A.x) - (F.x - A.x) * (B.y - A.y);

        if(almostEqual(crossABE,0) && almostEqual(crossABF,0)){

            Segment ABnorm = new Segment(B.y - A.y , A.x -B.x);
            Segment EFnorm = new Segment(F.y-E.y, E.x-F.x);

            double ABnormlength = Math.sqrt(ABnorm.x*ABnorm.x + ABnorm.y*ABnorm.y);
            ABnorm.x /= ABnormlength;
            ABnorm.y /= ABnormlength;

            double EFnormlength = Math.sqrt(EFnorm.x*EFnorm.x + EFnorm.y*EFnorm.y);
            EFnorm.x /= EFnormlength;
            EFnorm.y /= EFnormlength;

            // segment normals must point in opposite directions
            if(Math.abs(ABnorm.y * EFnorm.x - ABnorm.x * EFnorm.y) < SEGTOL && ABnorm.y * EFnorm.y + ABnorm.x * EFnorm.x < 0){
                // normal of AB segment must point in same direction as given direction vector
                double normdot = ABnorm.y * direction.y + ABnorm.x * direction.x;
                // the segments merely slide along eachother
                if(almostEqual(normdot,0, SEGTOL)){
                    return null;
                }
                if(normdot < 0){
                    return (double)0;
                }
            }
            return null;
        }
        List<Double> distances = new ArrayList<Double>();

        // coincident points
        if(almostEqual(dotA, dotE)){
            distances.add(crossA-crossE);
        }
        else if(almostEqual(dotA, dotF)){
            distances.add(crossA-crossF);
        }
        else if(dotA > EFmin && dotA < EFmax){
            Double d = pointDistance(A,E,F,reverse ,false);
            if(d != null && almostEqual(d, 0)){ //  A currently touches EF, but AB is moving away from EF
                Double dB = pointDistance(B,E,F,reverse,true);
                if(dB < 0 || almostEqual(dB*overlap,0)){
                    d = null;
                }
            }
            if(d != null){
                distances.add(d);
            }
        }

        if(almostEqual(dotB, dotE)){
            distances.add(crossB-crossE);
        }
        else if(almostEqual(dotB, dotF)){
            distances.add(crossB-crossF);
        }
        else if(dotB > EFmin && dotB < EFmax){
            Double d = pointDistance(B,E,F,reverse , false);

            if(d != null && almostEqual(d, 0)){ // crossA>crossB A currently touches EF, but AB is moving away from EF
                Double dA = pointDistance(A,E,F,reverse,true);
                if(dA < 0 || almostEqual(dA*overlap,0)){
                    d = null;
                }
            }
            if(d != null){
                distances.add(d);
            }
        }

        if(dotE > ABmin && dotE < ABmax){
            Double d = pointDistance(E,A,B,direction ,false);
            if(d != null && almostEqual(d, 0)){ // crossF<crossE A currently touches EF, but AB is moving away from EF
                Double dF = pointDistance(F,A,B,direction, true);
                if(dF < 0 || almostEqual(dF*overlap,0)){
                    d = null;
                }
            }
            if(d != null){
                distances.add(d);
            }
        }

        if(dotF > ABmin && dotF < ABmax){
            Double d = pointDistance(F,A,B,direction ,false);
            if(d != null && almostEqual(d, 0)){ // && crossE<crossF A currently touches EF, but AB is moving away from EF
                Double dE = pointDistance(E,A,B,direction, true);
                if(dE < 0 || almostEqual(dE*overlap,0)){
                    d = null;
                }
            }
            if(d != null){
                distances.add(d);
            }
        }

        if(distances.size() == 0){
            return null;
        }

        Double minElement = Double.MAX_VALUE;
        for(Double d : distances){
            if( d < minElement ){
                minElement = d;
            }
        }
        return minElement;
    }

    public static Double pointDistance( Segment p ,Segment s1 , Segment s2 ,Segment normal , Boolean infinite){
        normal = normalizeVector(normal);
        Segment dir = new Segment(normal.y , - normal.x );

        double pdot = p.x*dir.x + p.y*dir.y;
        double s1dot = s1.x*dir.x + s1.y*dir.y;
        double s2dot = s2.x*dir.x + s2.y*dir.y;

        double pdotnorm = p.x*normal.x + p.y*normal.y;
        double s1dotnorm = s1.x*normal.x + s1.y*normal.y;
        double s2dotnorm = s2.x*normal.x + s2.y*normal.y;


        if(infinite == null || !infinite){
            if (((pdot<s1dot || almostEqual(pdot, s1dot)) && (pdot<s2dot || almostEqual(pdot, s2dot))) || ((pdot>s1dot || almostEqual(pdot, s1dot)) && (pdot>s2dot || almostEqual(pdot, s2dot)))){
                return null; // dot doesn't collide with segment, or lies directly on the vertex
            }
            if ((almostEqual(pdot, s1dot) && almostEqual(pdot, s2dot)) && (pdotnorm>s1dotnorm && pdotnorm>s2dotnorm)){
                return Math.min(pdotnorm - s1dotnorm, pdotnorm - s2dotnorm);
            }
            if ((almostEqual(pdot, s1dot) && almostEqual(pdot, s2dot)) && (pdotnorm<s1dotnorm && pdotnorm<s2dotnorm)){
                return -Math.min(s1dotnorm-pdotnorm, s2dotnorm-pdotnorm);
            }
        }

        return -(pdotnorm - s1dotnorm + (s1dotnorm - s2dotnorm)*(s1dot - pdot)/(s1dot - s2dot));
    }

    /**
     * 专门为环绕矩形生成的nfp
     * @param A
     * @param B
     * @return
     */
    public static List<NestPath> noFitPolygonRectangle(NestPath A , NestPath B){
        double minAx = A.get(0).x;
        double minAy = A.get(0).y;
        double maxAx = A.get(0).x;
        double maxAy = A.get(0).y;

        for(int i=1; i<A.size(); i++){
            if(A.get(i).x < minAx){
                minAx = A.get(i).x;
            }
            if(A.get(i).y < minAy){
                minAy = A.get(i).y;
            }
            if(A.get(i).x > maxAx){
                maxAx = A.get(i).x;
            }
            if(A.get(i).y > maxAy){
                maxAy = A.get(i).y;
            }
        }

        double minBx = B.get(0).x;
        double minBy = B.get(0).y;
        double maxBx = B.get(0).x;
        double maxBy = B.get(0).y;
        for(int i=1; i<B.size(); i++){
            if(B.get(i).x < minBx){
                minBx = B.get(i).x;
            }
            if(B.get(i).y < minBy){
                minBy = B.get(i).y;
            }
            if(B.get(i).x > maxBx){
                maxBx = B.get(i).x;
            }
            if(B.get(i).y > maxBy){
                maxBy = B.get(i).y;
            }
        }



        if(maxBx-minBx > maxAx-minAx){

            return null;
        }
        double diffBy = maxBy - minBy;
        double diffAy = maxAy - minAy;

        if(diffBy > diffAy){
            return null;
        }


        List<NestPath> nfpRect = new ArrayList<NestPath>();
        NestPath res = new NestPath();
        res.add(minAx-minBx+B.get(0).x , minAy-minBy+B.get(0).y);
        res.add(maxAx - maxBx+B.get(0).x , minAy -minBy+B.get(0).y );
        res.add(maxAx - maxBx +B.get(0).x , maxAy - maxBy+B.get(0).y);
        res.add(minAx-minBx+B.get(0).x , maxAy - maxBy +B.get(0).y);
        nfpRect.add(res);
        return nfpRect;
    }

    /**
     *
     * @param A
     * @param B
     * @return
     */
    public static List<NestPath> minkowskiDifference(NestPath A, NestPath B){
        Path Ac = scaleUp2ClipperCoordinates(A);
        Path Bc = scaleUp2ClipperCoordinates(B);
        for(int i = 0 ; i< Bc.size();i++){
            long X = Bc.get(i).getX();
            long Y = Bc.get(i).getY();
            Bc.get(i).setX(-1 * X);
            Bc.get(i).setY(-1 * Y);
        }
        Paths solution =  DefaultClipper.minkowskiSum(Ac , Bc , true);
        double largestArea = Double.MAX_VALUE;
        NestPath clipperNfp = null;
        for(int  i = 0; i< solution.size() ; i ++){

            NestPath  n = toNestCoordinates(solution.get(i));
            double sarea = GeometryUtil.polygonArea(n);
            if(largestArea > sarea){
                clipperNfp = n;
                largestArea =sarea;
            }
        }

        for(int  i = 0 ; i< clipperNfp.size() ; i ++){
            clipperNfp.get(i).x += B.get(0).x ;
            clipperNfp.get(i).y += B.get(0).y ;
        }
        List<NestPath> nfp = new ArrayList<NestPath>();
        nfp.add(clipperNfp);
        return nfp;
    }

    public static NestPath linearize(Segment p1 , Segment p2 , double rx , double ry , double angle ,int laregearc , int sweep , double tol ){
        NestPath finished = new NestPath();
        finished.add(p2);
        DataExchange arc = ConvertToCenter(p1,p2,rx,ry,angle,laregearc,sweep);
        Deque<DataExchange> list = new ArrayDeque<>();
        list.add(arc);
        while(list.size() > 0 ){
            arc = list.getFirst();
            DataExchange fullarc = ConvertToSvg(arc.center,arc.rx , arc.ry ,arc.theta , arc.extent , arc.angle);
            DataExchange subarc = ConvertToSvg(arc.center , arc.rx ,arc.ry ,arc.theta ,0.5*arc.extent , arc.angle);
            Segment arcmid = subarc.p2;
            Segment mid = new Segment(0.5*(fullarc.p1.x + fullarc.p2.x) , 0.5 *(fullarc.p1.y + fullarc.p2.y));
            if(withinDistance( mid , arcmid ,tol )){
                finished.reverse();finished.add(new Segment(fullarc.p2));finished.reverse();
                list.removeFirst();
            }
            else{
                DataExchange arc1 = new DataExchange(new Segment(arc.center), arc.rx, arc.ry , arc.theta , 0.5 * arc.extent , arc.angle , false);
                DataExchange arc2 = new DataExchange(new Segment(arc.center),arc.rx , arc.ry , arc.theta+0.5 * arc.extent , 0.5 * arc.extent , arc.angle , false);
                list.removeFirst();
                list.addFirst(arc2);list.addFirst(arc1);
            }
        }
        return finished;
    }

    public static DataExchange ConvertToSvg(Segment center , double rx , double ry , double theta1 , double extent , double angleDegrees){
        double theta2 = theta1 + extent;

        theta1 = degreesToRadians(theta1);
        theta2 = degreesToRadians(theta2);
        double angle = degreesToRadians(angleDegrees);

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double t1cos = Math.cos(theta1);
        double t1sin = Math.sin(theta1);

        double t2cos = Math.cos(theta2);
        double t2sin = Math.sin(theta2);

        double x0 = center.x + cos * rx * t1cos +	(-sin) * ry * t1sin;
        double y0 = center.y + sin * rx * t1cos +	cos * ry * t1sin;

        double x1 = center.x + cos * rx * t2cos +	(-sin) * ry * t2sin;
        double y1 = center.y + sin * rx * t2cos +	cos * ry * t2sin;

        int largearc = (extent > 180) ? 1 : 0;
        int sweep = (extent > 0) ? 1 : 0;
        List<Segment> list = new ArrayList<>();
        list.add(new Segment(x0,y0));list.add(new Segment(x1,y1));
        return new DataExchange(new Segment(x0,y0), new Segment(x1,y1),rx,ry,angle , largearc , sweep , true);
    }

    public static DataExchange ConvertToCenter(Segment p1 , Segment p2 , double rx , double ry , double angleDgrees , int largearc , int sweep){
        Segment mid = new Segment(0.5 *(p1.x +p2.x) ,0.5 *(p1.y +p2.y));
        Segment diff = new Segment(0.5 *(p2.x - p1.x ) , 0.5 * (p2.y - p1.y ));

        double angle = degreesToRadians(angleDgrees);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double x1 = cos * diff.x + sin * diff.y;
        double y1 = -sin * diff.x + cos * diff.y;

        rx = Math.abs(rx);
        ry = Math.abs(ry);
        double Prx = rx * rx;
        double Pry = ry * ry;
        double Px1 = x1 * x1;
        double Py1 = y1 * y1;

        double radiiCheck = Px1/Prx + Py1/Pry;
        double radiiSqrt = Math.sqrt(radiiCheck);
        if (radiiCheck > 1) {
            rx = radiiSqrt * rx;
            ry = radiiSqrt * ry;
            Prx = rx * rx;
            Pry = ry * ry;
        }

        double sign = (largearc != sweep) ? -1 : 1;
        double sq = ((Prx * Pry) - (Prx * Py1) - (Pry * Px1)) / ((Prx * Py1) + (Pry * Px1));

        sq = (sq < 0) ? 0 : sq;

        double coef = sign * Math.sqrt(sq);
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);

        double cx = mid.x + (cos * cx1 - sin * cy1);
        double cy = mid.y + (sin * cx1 + cos * cy1);

        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;
        double n = Math.sqrt( (ux * ux) + (uy * uy) );
        double p = ux;
        sign = (uy < 0) ? -1 : 1;

        double theta = sign * Math.acos( p / n );
        theta = radiansToDegree(theta);

        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = ((ux * vy - uy * vx) < 0) ? -1 : 1;
        double delta = sign * Math.acos( p / n );
        delta = radiansToDegree(delta);

        if (sweep == 1 && delta > 0)
        {
            delta -= 360;
        }
        else if (sweep == 0 && delta < 0)
        {
            delta += 360;
        }
        delta %= 360;
        theta %= 360;
        List<Segment> list = new ArrayList<>();
        list.add(new Segment(cx , cy ));
        return new DataExchange(new Segment(cx,cy) , rx ,ry , theta , delta , angleDgrees , false);

    }

    public static double degreesToRadians(double angle){
        return angle * (Math.PI / 180);
    }

    public static double radiansToDegree(double angle){
        return angle * ( 180 / Math.PI);
    }



    static class DataExchange{
        Segment p1;
        Segment p2;
        Segment center;
        double rx;
        double ry;
        double theta;
        double extent;
        double angle;
        double largearc;
        int sweep;
        boolean flag;

        public DataExchange(Segment p1, Segment p2, double rx, double ry, double angle, double largearc, int sweep ,boolean flag) {
            this.p1 = p1;
            this.p2 = p2;
            this.rx = rx;
            this.ry = ry;
            this.angle = angle;
            this.largearc = largearc;
            this.sweep = sweep;
            this.flag = flag;
        }


        public DataExchange(Segment center, double rx, double ry, double theta, double extent, double angle , boolean flag) {
            this.center = center;
            this.rx = rx;
            this.ry = ry;
            this.theta = theta;
            this.extent = extent;
            this.angle = angle;
            this.flag = flag;
        }

        @Override
        public String toString() {
            String s = "";
            if(flag){
                s += " p1 = " +p1.toString() +" p2 = "+ p2.toString() +"\n rx = "+ rx +" ry = "+ry +" angle = "+angle +" largearc = "+largearc +" sweep = "+ sweep ;
            }
            else{
                s += " center = "+center +"\n rx = "+ rx +" ry = "+ ry +" theta = "+ theta +" extent = "+ extent +" angle = "+ angle ;
            }
            return s;
        }
    }

    public static boolean withinDistance( Segment p1 , Segment p2 , double distance){
        double dx = p1.x - p2.x ;
        double dy = p1.y - p2.y ;
        return ((dx * dx + dy * dy) < distance * distance);
    }

}

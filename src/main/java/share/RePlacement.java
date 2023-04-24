package share;

import de.lighti.clipper.*;
import nest.data.Bound;
import nest.data.NestPath;
import nest.data.Segment;
import nest.util.CommonUtil;
import nest.util.Config;
import nest.util.GeometryUtil;
import nest.util.coor.ClipperCoor;
import org.omg.CORBA.PUBLIC_MEMBER;
import share.Sheet;
import sun.java2d.loops.GeneralRenderer;

import java.text.CollationElementIterator;
import java.util.*;

public class RePlacement {

    // 底板的长宽
    public int xObject;
    public int yObject;

    public Queue tabuQueue;

    public double threshold; //阈值
    public double smallThreshold; //最小阈值

    // 记录总的合并结果
    public List<String> resultUion = new ArrayList<>();

    // 记录所有的合并过程，元素为合并的<k:合并的零件，v:适应度值>
    public List<Map<String, Double>> processUion = new ArrayList<>();

    // 二维数组存放利用率未满的底板中已放置的零件
    public List<List<NestPath>> pieceList = new ArrayList<>();

    // 二维数组存放底板上的洞
    public List<Paths> holeList = new ArrayList<>();
    
    // 原始底板列表
    public List<Sheet> objectList = new ArrayList<>();

    // 原始的零件列表
    public List<Piece> originPieceList = new ArrayList<>();

    // 凹凸多边形列表
    List<NestPath> convexList = new ArrayList<>();
    List<NestPath> nonConvexList = new ArrayList<>();

    // 维护每个零件在哪个底板的哈希表
    public Map<Integer, Integer> pieceInObjectMap = new HashMap<>();

    public void setObjectList(List<Sheet> listaObjetos) {
        this.objectList = listaObjetos;
    }

    public void setOriginPieceList(List<Piece> originPieceList) {
        this.originPieceList = originPieceList;
    }

    /**
     * 用一个零件填满单个洞
     */
    public void fillSingleHole(List<Sheet> listaObjetos){
        objectList = listaObjetos;
        // TODO: 1. 按利用率从小到大对所有底板排序
        Collections.sort(objectList);

        // TODO: 2. 保存所有底板的零件
        // 2.1 先将所有利用率非1的底板的零件用nestpath保存下来，同时保存零件id
        updatePieceList();

        // TODO: 3. 再求交，得到每个底板的洞并保存下来，用二维数组存放
        updateHole();


        // TODO:4. 找到利用率最小的底板（即第一个），对其上的零件，已经改成往所有的底板上找洞了，找到合适的洞直接塞进去，塞一个求一次交
        for(int i = 0; i < pieceList.size(); i++){
            if(objectList.get(i).getFreeArea() == 0){
                continue; //如果是满的，跳过
            }
            // 对底板上的零件，一个个拿
            for(int k = 0; k < pieceList.get(i).size(); k++){
                boolean is_end = false; //如果当前零件已放置，跳过
                NestPath path = pieceList.get(i).get(k);
                NestPath origin = path;
                List<NestPath> f = new ArrayList<>(); //最终可放置位置
                // 遍历每一个剩余多边形
                for(int p = 0; p < holeList.size(); p++){
                    if(p==i){
                        continue; //如果还是当前底板，跳过
                    }
                    if(is_end){
                        break; //如果当前零件已放置，跳过
                    }
                    Paths remain = holeList.get(p);

                    // 4.1 找到面积大于当前零件的最小hole，求内部NFP和几个顶点作为holeNFP
                    // 将remain的顶点加入holeNFP，排序
                    List<NestPath> holeNFP = new ArrayList<>();
                    for(int j = 0 ; j<remain.size() ; j++){
                        // back to normal scale
                        holeNFP.add( toNestCoordinates(remain.get(j)));
                    }
                    Collections.sort(holeNFP); //按面积排序

                    // 4.2 从第一个合适的往后尝试holeNFP
                    NestPath enableHoleNfp = new NestPath();    //合适的空心多边形
                    for(int u = 0; u < holeNFP.size(); u++){
                        if(is_end){
                            break; //如果当前零件已放置，跳过
                        }
                        // TODO: 这里要相等还是小于，先用相等，后面再考虑组合
//                        if( Math.abs(GeometryUtil.polygonArea(holeNFP.get(u))) >= Math.abs(GeometryUtil.polygonArea(path)) ){
                        if( GeometryUtil.almostEqual(Math.abs(GeometryUtil.polygonArea(holeNFP.get(u))), Math.abs(GeometryUtil.polygonArea(path)))  ){
                            enableHoleNfp = holeNFP.get(u);
                            // TODO: 旋转找点位
                            for(int rotateNum = 0; rotateNum < 4; rotateNum++){
                                if(is_end){
                                    break; //如果当前零件已放置，跳过
                                }
                                int rotation = (path.getRotation()+90) % 360;
                                path = GeometryUtil.rotatePolygon2Polygon(origin , rotation);
                                path.setRotation(rotation);
                                path.setSource(path.getSource());
                                path.setId( path.getId());
                                path.setArea(path.getArea()); //设置面积

                                // 4.3 将path绕着holeNFP内部形成的NFP


                                // 4.4 判断holeNFP里的点能否放置，若能放置，加入f中
                                // 4.5 将path移动到空心处
                                for(int t = 0; t < enableHoleNfp.size(); t++){
                                    if(is_end){
                                        break; //如果当前零件已放置，跳过
                                    }
                                    // 相对于path的哪个点移动也会有不同的结果，所以要遍历path的点
                                    for(int positionPoint = 0; positionPoint < path.size(); positionPoint++) {
                                        if(is_end){
                                            break; //如果当前零件已放置，跳过
                                        }
                                        double shifx = enableHoleNfp.get(t).x - path.get(positionPoint).x;
                                        double shify = enableHoleNfp.get(t).y - path.get(positionPoint).y;

                                        // 构建移动后的多边形
                                        NestPath shifPath = new NestPath();

                                        for (int m = 0; m < path.size(); m++) {
                                            shifPath.add(new Segment(Math.rint(path.get(m).x + shifx), Math.rint(path.get(m).y + shify)));
                                        }

                                        // 判断这个新构建的多边形的位置是否合法
                                        if (GeometryUtil.insidePP(shifPath, enableHoleNfp)) {
                                            NestPath point = new NestPath();
                                            point.add(enableHoleNfp.get(t).x, enableHoleNfp.get(t).y);
                                            f.add(point);
                                            // 将第i块底板上的第k个零件移动到第p块底板上，并修改holeList和list
                                            // TODO: 这里可以加个比较，如果邻接周长更大，就重新放，否则不重新放
                                            movePiecetoObject(i,k,p,rotation,shifPath,holeList,pieceList,objectList);
                                            is_end = true;
                                            k = -1; //重新遍历
                                            break;
                                        }
                                    }
                                }

                            }

                        }
                    }
                }
            }
        }


        // TODO: 5 找到邻接周长最大的点位放置，并加到对应的object中




        // 5. 重新求交，求得每个底板的洞，更新，只需要拿holeList里的remain再次求交即可

        // 6. 继续塞

        // 7. 尝试交换位置

        // 重排序
        Collections.sort(objectList);
    }

    /**
     * 移出一个，换进1个或2个或3个的组合
     * TODO: 有bug，在TS018C8中，没有换出一个，换进两个
     */
    public void changeOne(){
        // 2.从利用率大的下手，按面积从小到大换出，换出后，寻找能否塞入的零件1，2，3个零件组合
        Collections.sort(objectList); //按利用率从小到大对所有底板排序
        updatePieceList();
        updateHole();
        // TODO:底板利用率足够高的才使用此策略？
        // 1.维护零件在哪个底板的哈希表
        updatePieceInObjectMap();


        // 剔除利用率为1的底板并保存下来
        List<Sheet> fullObjects = new ArrayList<>(); //利用率为1的底板
        List<Paths> fullHoleList = new ArrayList<>(); //利用率为1的洞列表
        List<List<NestPath>> fullPieceList = new ArrayList<>(); //
        for(int i = objectList.size() - 1; i >= 0; i--){
            if(objectList.get(i).getFreeArea() == 0){
                Sheet removeSheet = objectList.remove(i);
                fullObjects.add(removeSheet); //保存被剔除的底板
                Paths removeHole = holeList.remove(i);
                fullHoleList.add(removeHole);
                List<NestPath> removePieceList = pieceList.remove(i);
                fullPieceList.add(removePieceList);
            }else{ //如果利用率不满，对其中的零件排个序
                // 对objectlist排序
                List<Piece> pzasInside = objectList.get(i).getPzasInside();
                Collections.sort(pzasInside);

                // 对piecelist排序
                Collections.sort(pieceList.get(i));
            }
        }

        // 3.记录下换出去的零件id，如果换出后发现还是原来的零件放入，则取消该零件的换出，放入禁忌列表
        // 第i块底板 TODO: 改为利用率最高的底板
        for(int i = objectList.size() - 1; i >= objectList.size() - 2; i--){
            List<Piece> pzasInside = objectList.get(i).getPzasInside();
            // 按面积从小到大取出第j个零件换出
            boolean canPlace = false; //如果有零件换出换入成功了，就退出
            for(int j = 0; j < pzasInside.size(); j++){
                if(canPlace){
                    break;
                }
                // 取出后求并，得到新的holes，如果holes的数量增加，则不取出
                Piece piece = pzasInside.get(j);
                int holeNum = holeList.get(i).size(); //该底板上的洞数量
                // 求并
                NestPath nestPath = PiecetoNestPath(piece); //先转成nestpath再转成path
                Path piecePath = scaleUp2ClipperCoordinates(nestPath);

                Paths holes = holeList.get(i); //第i块底板上原始的洞们
                Paths newHoles = unionPaths(holes, piecePath); //得到新的洞们

                if(newHoles.size() > holes.size()){ //如果洞变多了，则跳过，尝试下一个零件
                    // TODO: 形状太奇怪的也跳过（比如点数过多？）
                    continue;
                }else{
                    // 找到面积变化的那块洞，方法是遍历新的洞们，如果找不到在原来洞里面积一样的，就是它了
                    NestPath newHole = findChangeHole(holes, newHoles);

                    // 取出后求并，首先判断1个，2个，3个的组合能否填满窟窿，如果能，证明是可行的
                    // 否则1.直接原零件放回去，2.寻找利用率更高的放(这步计划到后面实施)

                    // 遍历所有零件，除了第i个底板，找一个数之和or两数之和or三数之和=newholes的面积
                    double putInArea = GeometryUtil.polygonArea(newHole); //初始化为hole的面积
                    List<NestPath> fillInPiece = findFillInHolePieces(i, putInArea);

                    if(fillInPiece.size() == 0 || fillInPiece == null){
                        //break; //这里好像应该是continue
                        continue;
                    }
                    // TODO: 有时候出现洞的情况，此时得到的newhole其实是有零件的，跳过，条件是只有一个零件
                    // 条件：本来就是当前底板的、只有一个（造成洞的零件）、零件在新生成的洞里
                    if(fillInPiece.get(0).bid == i && fillInPiece.size() == 1 && GeometryUtil.insidePP(fillInPiece.get(0), newHole)){
                        continue;
                    }

                    // 开始放置
                    // 先给能放置的零件排个序
                    Collections.sort(fillInPiece);

                    for(int k = fillInPiece.size()-1; k >= 0; k--){
                        // 一个个放，并将零件的位置调整好
                        NestPath movePath = placePieceInHole(newHole, fillInPiece.get(k)); //得到移动后的零件
                        if(movePath.size() == 0 || movePath == null){ //如果放置失败
                            canPlace = false;
                            break;
                        }else{ //放置成功，更新洞
                            fillInPiece.set(k,movePath); //更改fillInPiece
                            newHole = updateHoleWhenPiecePlaceIn(newHole, movePath);
                            canPlace = true;
                        }
                    }

                    // 如果一个或两个或三个都放置成功了
//                    canPlace = true; //能走到这里说明放置成功了
                    if(canPlace){
                        // (1).调整洞口、零件列表，移除第i块底板上的第j个零件
                        NestPath removePiece = pieceList.get(i).remove(j);//换出的零件
                        // (1.1)将放的进零件放入第i个底板，这里也移动了新零件
                        for (NestPath canFillInPath : fillInPiece) {
                            pieceList.get(i).add(canFillInPath);
                        }
                        // (1.2)删除其他底板的被移动零件
                        for (NestPath canFillInPath : fillInPiece) {
                            // 先找到在哪块底板上
                            int objectId = pieceInObjectMap.get(canFillInPath.getId());
                            // 再找到底板上对应的零件删除
                            for (NestPath prepareRemovePath : pieceList.get(objectId)) {
                                if(prepareRemovePath.getId() == canFillInPath.getId()){
                                    pieceList.get(objectId).remove(prepareRemovePath);
                                    break;
                                }
                            }
                        }

                        // (1.3)旧零件何去何从？开一个新的底板，放在新底板里
                        List<NestPath> newobjectPiece = new ArrayList<>(); //新的底板
                        newobjectPiece.add(removePiece);
                        pieceList.add(0,newobjectPiece); //新的零件列表
                        changePiecelistToObjcelist(); //转化为底板列表

                    }

                }

            }

        }
        // 将原来满的底板加回来
        for (Sheet fullObject : fullObjects) {
            objectList.add(fullObject);
        }
        // 将原来的洞列表加回来
        for (Paths paths : fullHoleList) {
            holeList.add(paths);
        }
        // 将原来的零件列表加回来
        for (List<NestPath> nestPaths : fullPieceList) {
            pieceList.add(nestPaths);
        }




        // (1.2) 更新所有底板的洞口
        updateHole();

        // (1.3) 更新哈希表
        updatePieceInObjectMap();
        // 4.尝试放2,3,4,5个，使得利用率更高？
        Collections.sort(objectList);
    }


    /**
     * 不产生洞的方式重新放置
     */
    public void noHolePlace(){
        // 0. 预处理，将利用率未满的底板的零件加入等待放置列表
        pieceList.clear();
        holeList.clear();
        pieceInObjectMap.clear();
        List<NestPath> alreadyPieceList = new ArrayList<>(); //等待放置列表
        List<NestPath> fullPieceList = new ArrayList<>(); //底板放满的列表
        for(int i = 0; i < objectList.size(); i++){
            if(objectList.get(i).getFreeArea() == 0){
                for (Piece piece : objectList.get(i).getPzasInside()) {
                    fullPieceList.add(PiecetoNestPath(piece));
                }
            }
            for (Piece piece : objectList.get(i).getPzasInside()) {
                alreadyPieceList.add(PiecetoNestPath(piece));
            }
        }
        Collections.sort(alreadyPieceList);


        while(!alreadyPieceList.isEmpty()){ //一个个放
            NestPath pointsList = new NestPath(); //可放置点位
            // 第一个放的不能产生洞
            NestPath path = alreadyPieceList.get(alreadyPieceList.size() - 1); //当前准备放的零件
            // 第一个零件，找直角点放

        }

        // 1. 按面积从大到小放找有直角的放，且不产生洞

        // 2.先找到有直角的放

        // 3.围着当前已有的点放，找邻接周长比例最大的零件放，如果产生洞就不放

        // 4.
    }


    /**
     * 将第i块底板上的第k个零件移动到第p块底板上，并修改第p块holeList和objectList, pieceList, shifPath为移动后的零件
     */
    public void movePiecetoObject(int i, int k, int p, int rotate, NestPath shifPath, List<Paths> holeList, List<List<NestPath>> pieceList, List<Sheet> objectList) {
        // 移除第i块底板上的第k个零件
        NestPath remove = pieceList.get(i).remove(k);
        objectList.get(i).getPzasInside().remove(k);
        // 更新piecelist，加到第p块底板上
        shifPath.setRotation(rotate);
        shifPath.setId(remove.getId());
        shifPath.setArea(remove.getArea());
        pieceList.get(p).add(shifPath);

        // 转换回piece
        int vertices = shifPath.size();
        int[] coordenadas = new int[vertices*2];
        List<Segment> segments = shifPath.getSegments();
        int cnt = 0;
        for(int u = 0; u < vertices; u++){
            coordenadas[cnt++] = (int) segments.get(u).x;
            coordenadas[cnt++] = (int) segments.get(u).y;
        }
        // 构建piece，没有id信息
        Piece piece = new Piece(coordenadas);
        piece.setnumber(remove.getId());
        piece.setRotada(rotate);
        piece.setArea((int) remove.getArea());
        // 加到第p块底板上
        objectList.get(p).addPieza(piece);

        // 修改holelist的第p块，与shifPath做除
        Paths currentPath = holeList.remove(p); //当前洞
        Paths shifPaths = new Paths(); //准备移除的
        shifPaths.add(scaleUp2ClipperCoordinates(shifPath));

        Paths remainOne = new Paths();
        DefaultClipper clipper1 = new DefaultClipper(2);
        clipper1.addPaths(shifPaths, Clipper.PolyType.CLIP, true);
        clipper1.addPaths(currentPath, Clipper.PolyType.SUBJECT, true); //被裁剪
        clipper1.execute(Clipper.ClipType.DIFFERENCE, remainOne, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);

        holeList.add(p, remainOne);

    }


    /**
     * 找到底板上的洞
     */
    public void findHole(List<List<NestPath>> pieceList){
        holeList.clear();
        // 拿出与已经放置的板件placed中的板件
        for(int i = 0; i < pieceList.size(); i++){
            // 3.1 对每个已放置的零件，求得他的剩余多边形
            // 定义bin的Path，这里改成动态的
            NestPath bin = new NestPath();
            bin.add(0, 0);
            bin.add(1000, 0);
            bin.add(1000, 1000);
            bin.add(0, 1000);
            Paths polygonBin = new Paths();
            polygonBin.add(scaleUp2ClipperCoordinates(bin));

            Paths remain = new Paths(); //每一个底板的最终剩余多边形集合(可能有多个)
            DefaultClipper clipperLast = new DefaultClipper(2);

            // 遍历每一个零件
            for(int j = 0; j < pieceList.get(i).size();j++){
                // 获取第i个底板上的第j个零件
                NestPath currentPath = pieceList.get(i).get(j);

                Paths remainOne = new Paths();
                Path clone = scaleUp2ClipperCoordinates(currentPath); //当前零件转换为Path格式
                DefaultClipper clipper1 = new DefaultClipper(2);
                clipper1.addPath(clone, Clipper.PolyType.CLIP, true);
                clipper1.addPath(polygonBin.get(0), Clipper.PolyType.SUBJECT, true); //被裁剪
                clipper1.execute(Clipper.ClipType.DIFFERENCE, remainOne, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);

                if(j == 0){ //与第一个放置零件的剩余多边形
                    clipperLast.addPaths(remainOne , Clipper.PolyType.CLIP , true);
                    remain = remainOne;
                }else{
                    clipperLast.addPaths(remainOne , Clipper.PolyType.SUBJECT , true);
                    clipperLast.execute(Clipper.ClipType.INTERSECTION, remain, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);
                    // 执行完后得更新
                    clipperLast = new DefaultClipper(2);
                    clipperLast.addPaths(remain , Clipper.PolyType.CLIP , true);
                }
            }

            // 3.2 最后再和bin交一下，因为有可能只剩一个多边形，也得和bin交一下，得hole。
            if(remain.size() == 0){
                clipperLast.addPaths(remain, Clipper.PolyType.SUBJECT, true);
                clipperLast.addPaths(polygonBin, Clipper.PolyType.CLIP, true);
                clipperLast.execute(Clipper.ClipType.INTERSECTION, remain, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);
            }

            // 3.3 将结果保存到全局变量二维数组中
            holeList.add(remain);
        }
    }


    /**
     *  NestPath转化为Path
     */
    public static Path scaleUp2ClipperCoordinates(NestPath polygon){
        Path p = new Path();
        for(Segment s : polygon.getSegments()){
            ClipperCoor cc = CommonUtil.toClipperCoor(s.x , s.y);
            p.add(new Point.LongPoint(cc.getX() , cc.getY()));
        }
        return p;
    }

    /**
     * Path转化为NestPath
     */
    public static NestPath toNestCoordinates(Path polygon){
        NestPath clone = new NestPath();
        for(int i = 0 ; i< polygon.size() ; i ++){
            Segment s = new Segment((double)polygon.get(i).getX()/ Config.CLIIPER_SCALE , (double)polygon.get(i).getY()/Config.CLIIPER_SCALE);
            clone.add(s);
        }
        return clone ;
    }

    /**
     * 将Piece转化为NestPath
     */
    public static NestPath PiecetoNestPath(Piece piece){
        NestPath path = new NestPath(); //保存当前零件
        double x,y;
        int vertices = piece.getvertices();
        for(int j = 0; j < vertices; j++){
            x = (double)piece.coordX[j];
            y = (double)piece.coordY[j];
            path.add(x,y); //加入顶点
        }
        path.setId(piece.getnumber()); //设置id
        path.setRotation((int)piece.getRotada()); //设置旋转角度
        path.setArea(piece.getArea()); //设置面积
        if(piece.getStrPid().equals("&")){ //初始化，还没有字符串id，此时用number来赋值
            path.setStrId(String.valueOf(piece.getnumber()));
        }else{ //有字符串id了，直接赋值
            path.setStrId(piece.getStrPid()); //设置字符串id
        }

        return path;
    }

    /**
     * 将NestPath转化为Piece
     */
    public static Piece NestPathtoPiece(NestPath path){
        int vertices = path.size();
        int[] coordinates = new int[vertices*2];
        int cnt = 0;
        for(int i = 0; i < vertices; i++){
            coordinates[cnt++] = (int) path.get(i).x;
            coordinates[cnt++] = (int) path.get(i).y;
        }
        Piece piece = new Piece(coordinates);
        piece.setnumber(path.getId()); //设置id
        piece.setRotada(path.getRotation()); //设置旋转
        piece.setArea((int) path.getArea()); //设置面积
        piece.setStrPid(path.getStrId());
        return piece;
    }

    /**
     * 将NestPath转化为Piece，包括子零件
     */
    public static Piece NestPathtoPieceIncludeChild(NestPath path){
        int vertices = path.size();
        int[] coordinates = new int[vertices*2];
        int cnt = 0;
        for(int i = 0; i < vertices; i++){
            coordinates[cnt++] = (int) path.get(i).x;
            coordinates[cnt++] = (int) path.get(i).y;
        }
        Piece piece = new Piece(coordinates);
        piece.setnumber(path.getId()); //设置id
        piece.setRotada(path.getRotation()); //设置旋转
        piece.setArea((int) path.getArea()); //设置面积
        piece.setStrPid(path.getStrId()); //设置字符串id

        // 如果path有子元素
//        List<Piece> children = new ArrayList<>();
//        preOrder(path, children);
//        for(int i = 0; i < children.size(); i++){
//            if(children.size() > 1)
//                piece.getChild().add(children.get(i));
//        }
        piece = dfs(piece, path); //todo:大问题？这里为啥不用=就无法正确赋值？

        return piece;
    }

    /**
     * 递归赋值
     */
    public static Piece dfs(Piece piece, NestPath path){
        piece = NestPathtoPiece(path);
        if(path.child.size() > 0){
            piece.child.add(dfs(new Piece(), path.child.get(0)));
            piece.child.add(dfs(new Piece(), path.child.get(1)));
        }

        return piece;
    }

    /**
     * 遍历孩子节点
     */
    public static void preOrder(NestPath path, List<Piece> children){
        // 到达根节点
        if(path.leftChild == null && path.rightChild == null){
            children.add(NestPathtoPiece(path));
        }
        if(path.leftChild != null && path.rightChild != null){
            preOrder(path.leftChild, children);
            preOrder(path.rightChild, children);
        }
    }


    /**
     * 求paths和path的并
     */
    public static Paths unionPaths(Paths paths, Path path){
        Paths remainOne = new Paths();
        DefaultClipper clipper1 = new DefaultClipper(2);
        clipper1.addPaths(paths, Clipper.PolyType.CLIP, true);
        clipper1.addPath(path, Clipper.PolyType.SUBJECT, true); //被裁剪
        clipper1.execute(Clipper.ClipType.UNION, remainOne, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);
        return remainOne;
    }

    /**
     * 求paths和path的并，弱多边形
     */
    public static Paths unionPath(Path path1, Path path2){
        Paths remainOne = new Paths();
        DefaultClipper clipper1 = new DefaultClipper();
        clipper1.addPath(path1, Clipper.PolyType.CLIP, true);
        clipper1.addPath(path2, Clipper.PolyType.SUBJECT, true); //被裁剪
        clipper1.execute(Clipper.ClipType.UNION, remainOne, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);
        return remainOne; //只返回一个弱多边形
    }

    /**
     * 在piecelist中除了第i个底板，找到1个或2个或3个零件满足面积刚好等于putInArea
     */
    private List<NestPath> findFillInHolePieces(int i, double putInArea) {
        List<NestPath> fillInPiece = new ArrayList<>();
        // 先尝试一个
        for(int k = 0; k < pieceList.size(); k++){
            // 不考虑跳过当前底板了
//            if(k==i){
//                continue;
//            }
            for (NestPath otherObjctNestPath : pieceList.get(k)) {
                otherObjctNestPath.bid = k;
                // 如果当前零件的面积已经大于要放入的洞的面积了，后面的也不用尝试了
                if(GeometryUtil.polygonArea(otherObjctNestPath) > putInArea){
                    break;
                }else if(GeometryUtil.polygonArea(otherObjctNestPath) == putInArea){ //找到面积相等的一块
                    fillInPiece.add(otherObjctNestPath);
                    return fillInPiece;
                }
            }
        }
        // 再尝试两个
        List<NestPath> result = new ArrayList<>();
        List<NestPath> newPieceList = new ArrayList<>();
        for(int k = 0; k < pieceList.size(); k++){
            // 不考虑跳过当前底板了
//            if(k==i){ // TODO: 跳过当前底板，这里等会要改
//                continue;
//            }
            for (NestPath nestPath : pieceList.get(k)) {
                nestPath.bid = k;
                newPieceList.add(nestPath);
            }
        }
        Collections.sort(newPieceList);
        if(findTwoPieceFillInHole(newPieceList,putInArea,0,0,result)){
            return result;
        }

        // 再尝试三个
        result.clear();
        newPieceList.clear();
        for(int k = 0; k < pieceList.size(); k++){
            // 不考虑跳过当前底板了
//            if(k==i){ // TODO: 跳过当前底板，这里等会要改
//                continue;
//            }
            for (NestPath nestPath : pieceList.get(k)) {
                nestPath.bid = k;
                newPieceList.add(nestPath);
            }
        }
        Collections.sort(newPieceList);
        if(findThreePieceFillInHole(newPieceList,putInArea,0,0,result)){
            return result;
        }

        // 再尝试四个
        result.clear();
        newPieceList.clear();
        for(int k = 0; k < pieceList.size(); k++){
            // 不考虑跳过当前底板了
//            if(k==i){ // TODO: 跳过当前底板，这里等会要改
//                continue;
//            }
            for (NestPath nestPath : pieceList.get(k)) {
                nestPath.bid = k;
                newPieceList.add(nestPath);
            }
        }
        Collections.sort(newPieceList);
        if(findFourPieceFillInHole(newPieceList,putInArea,0,0,result)){
            return result;
        }
        return fillInPiece;
    }


    /**
     * 找到面积变化的洞
     * @param holes
     * @param newHoles
     * @return
     */
    public NestPath findChangeHole(Paths holes, Paths newHoles) {
        // 原始洞列表
        List<NestPath> originHoleList = new ArrayList<>();
        for (Path everyOriginHole : holes) {
            originHoleList.add(toNestCoordinates(everyOriginHole));
        }
        // 新洞列表
        List<NestPath> newHoleList = new ArrayList<>();
        for (Path everyNewHole : newHoles) {
            newHoleList.add(toNestCoordinates(everyNewHole));
        }
        NestPath newHole = new NestPath();
        for (NestPath everyNewHole : newHoleList) {
            boolean flag = true; //true表示对当前新洞，找不到原洞里和他面积一样的，即面积有变化的，为false表示找不到面积变化的
//            for (NestPath path : originHoleList) {
            for(int i = 0; i < originHoleList.size(); i++){
                NestPath path = originHoleList.get(i);
                if(GeometryUtil.polygonArea(everyNewHole) == GeometryUtil.polygonArea(path)){
                    // TODO: 找到相等的，同时，原hole列表有一个在新hole里面，即洞，那么可返回
                    for(int j = 0; j < originHoleList.size(); j++){
                        if(j==i){
                            continue;
                        }
                        // 拿走的是内部零件，需保证新零件列表减少数目了，即newHoles.size() < holes.size()
                        if(GeometryUtil.insidePP(originHoleList.get(j), everyNewHole) && newHoles.size() < holes.size()){
                            return everyNewHole;
                        }
                    }
                    flag = false;
                    break;
                }
            }
            if(flag){ //没有面积相等的，赋值后退出
                newHole = everyNewHole;
                return newHole;
            }
        }
        return newHole;
    }

    /**
     * 将零件放置到洞里，返回移动后的零件
     */
    public NestPath placePieceInHole(NestPath enableHoleNfp, NestPath path){
        // 克隆一个原始对象
        NestPath origin = null;
        try {
            origin = path.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        NestPath movePath = new NestPath(); //最终移动的零件
        boolean is_end = false; //零件是否放置成功
        // 1.旋转找点位
        for(int rotateNum = 0; rotateNum <4; rotateNum++){
            if(is_end){
                break; //如果当前零件已放置成功，退出
            }
            int rotation = (path.getRotation()+90) % 360;
            path = GeometryUtil.rotatePolygon2Polygon(origin , rotation); //这里会出现浮点数问题，最好四舍五入一下，但是也不用，因为后面我有四舍五入
            path.setRotation(rotation);
            path.setId( path.getId());
            path.setArea(path.getArea());

            // 4.3 将path绕着holeNFP内部形成的NFP


            // 4.4 判断holeNFP里的点能否放置，若能放置，加入f中
            // 4.5 将path移动到空心处
            for(int t = 0; t < enableHoleNfp.size(); t++){
                if(is_end){
                    break; //如果当前零件已放置成功，退出
                }
                // 相对于path的哪个点移动也会有不同的结果，所以要遍历path的点
                for(int positionPoint = 0; positionPoint < path.size(); positionPoint++) {
                    if(is_end){
                        break; //如果当前零件已放置成功，退出
                    }
                    double shifx = enableHoleNfp.get(t).x - path.get(positionPoint).x;
                    double shify = enableHoleNfp.get(t).y - path.get(positionPoint).y;

                    NestPath shifPath = new NestPath();

                    for (int m = 0; m < path.size(); m++) {
                        shifPath.add(new Segment(Math.rint(path.get(m).x + shifx), Math.rint(path.get(m).y + shify)));
                    }

                    // 判断这个新构建的多边形的位置是否合法
                    if (GeometryUtil.insidePP(shifPath, enableHoleNfp)) {
//                        NestPath point = new NestPath();
//                        point.add(enableHoleNfp.get(t).x, enableHoleNfp.get(t).y);
//                        f.add(point);
                        // 将第i块底板上的第k个零件移动到第p块底板上，并修改holeList和list
                        // TODO: 这里可以加个比较，如果邻接周长更大，就重新放，否则不重新放
                        is_end = true;
                        movePath = shifPath; //将零件移动
                        movePath.setRotation(rotation); //
                        movePath.setId( path.getId());
                        movePath.setArea(path.getArea());

//                        k = -1; //重新遍历
                        break;
                    }
                }
            }

        }


        return movePath;
    }

    /**
     * 根据Objectlist更新piecelist
     * @param
     * @return
     */
    public void updatePieceList() {
        pieceList.clear();
        for(int i = 0; i < objectList.size(); i++){
            Sheet currentSheet = objectList.get(i);

            //if(currentSheet.getFreeArea() > 0){ // 有剩余面积的才保存上面的零件（不需要，全保存吧）
            List<NestPath> everyObjectPath = new ArrayList<>(); //一维，保存每一块底板上的零件
            List<Piece> pieceInside = currentSheet.getPzasInside();
            // 将所有利用率不为1的底板的零件保存下来
            for (Piece piece : pieceInside) {
                NestPath path = new NestPath(); //保存当前零件
                double x,y;
                int vertices = piece.getvertices();
                for(int j = 0; j < vertices; j++){
                    x = (double)piece.coordX[j];
                    y = (double)piece.coordY[j];
                    path.add(x,y); //加入顶点
                }
                path.setId(piece.getnumber()); //设置id
                path.setRotation((int)piece.getRotada()); //设置旋转角度
                path.setArea(piece.getTotalSize()); //设置面积
                everyObjectPath.add(path);
            }
            pieceList.add(everyObjectPath); //将一维的结果保存下来
        }
    }

    /**
     * 更新所有底板的洞
     * @param
     * @return
     */
    public void updateHole() {
        holeList.clear(); //清空
        findHole(pieceList); //重新计算
    }

    /**
     * 更新哈希表
     */
    public void updatePieceInObjectMap() {
        pieceInObjectMap.clear();
        for(int i = 0; i < pieceList.size(); i++){
            for (NestPath nestPath : pieceList.get(i)) {
                pieceInObjectMap.put(nestPath.getId(), i);
            }
        }
    }

    /**
     * 找到两个零件符合面积area的
     */
    public boolean findTwoPieceFillInHole(List<NestPath> piecesList, double area, double pieceAreaSum, int index, List<NestPath> result){
        if(result.size() == 2 && pieceAreaSum == area){
            return true;
        }
        if(result.size() == 2 && pieceAreaSum != area){
            return false;
        }
        for(int i = index; i < piecesList.size(); i++){
            double pieceArea = GeometryUtil.polygonArea(piecesList.get(i));
            if(pieceArea >= area){
                break;
            }
            result.add(piecesList.get(i));
            pieceAreaSum += pieceArea;
            if(findTwoPieceFillInHole(piecesList, area, pieceAreaSum,index+1, result)){
                return true;
            }
            result.remove(result.size()-1);
            pieceAreaSum -= pieceArea;
        }
        return false;
    }


    /**
     * 找到三个零件符合面积area的
     */
    public boolean findThreePieceFillInHole(List<NestPath> piecesList, double area, double pieceAreaSum, int index, List<NestPath> result){
        if(result.size() == 3 && pieceAreaSum == area){
            return true;
        }
        if(result.size() == 3 && pieceAreaSum != area){
            return false;
        }
        for(int i = index; i < piecesList.size(); i++){
            double pieceArea = GeometryUtil.polygonArea(piecesList.get(i));
            if(pieceArea >= area){
                break;
            }
            result.add(piecesList.get(i));
            pieceAreaSum += pieceArea;
            if(findThreePieceFillInHole(piecesList, area, pieceAreaSum,index+1, result)){
                return true;
            }
            result.remove(result.size()-1);
            pieceAreaSum -= pieceArea;
        }
        return false;
    }

    /**
     * 找到四个零件符合面积area的
     */
    public boolean findFourPieceFillInHole(List<NestPath> piecesList, double area, double pieceAreaSum, int index, List<NestPath> result){
        if(result.size() == 4 && pieceAreaSum == area){
            return true;
        }
        if(result.size() == 4 && pieceAreaSum != area){
            return false;
        }
        for(int i = index; i < piecesList.size(); i++){
            double pieceArea = GeometryUtil.polygonArea(piecesList.get(i));
            if(pieceArea >= area){
                break;
            }
            result.add(piecesList.get(i));
            pieceAreaSum += pieceArea;
            if(findFourPieceFillInHole(piecesList, area, pieceAreaSum,index+1, result)){
                return true;
            }
            result.remove(result.size()-1);
            pieceAreaSum -= pieceArea;
        }
        return false;
    }


    /**
     * 更新放了零件的洞
     * @param enableHoleNfp
     * @param path
     */
    public NestPath updateHoleWhenPiecePlaceIn(NestPath enableHoleNfp, NestPath path) {
        Paths remainOne = new Paths();
        Path clone = scaleUp2ClipperCoordinates(path); //零件
        Path hole = scaleUp2ClipperCoordinates(enableHoleNfp); //洞
        DefaultClipper clipper1 = new DefaultClipper(2);
        clipper1.addPath(clone, Clipper.PolyType.CLIP, true);
        clipper1.addPath(hole, Clipper.PolyType.SUBJECT, true); //被裁剪
        clipper1.execute(Clipper.ClipType.DIFFERENCE, remainOne, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO);

        if(remainOne.size() == 0 || remainOne == null){
            return new NestPath();
        }

        return toNestCoordinates(remainOne.get(0));
    }

    /**
     * 将零件列表转化为底板列表
     */
    public void changePiecelistToObjcelist() {
        objectList.clear();

        for(int i = 0; i < pieceList.size(); i++){
            Sheet sheet = new Sheet(1000, 1000, i);
            for (NestPath nestPath : pieceList.get(i)) {
                sheet.addPieza(NestPathtoPiece(nestPath));
            }
            objectList.add(sheet);
        }

    }

    /**
     * 找到凹凸多边形
     */
    public void findConvexAndNonconvex(List<Piece> originPieceList, List<NestPath> convexList, List<NestPath> nonConvexList) {
        for(int j = 0; j < originPieceList.size(); j++){
            Piece piece = originPieceList.get(j);
            NestPath path = PiecetoNestPath(piece);
            if(GeometryUtil.isConvex(path)){ //凸多边形
                convexList.add(path);
            }else{
                nonConvexList.add(path);
            }
        }
//
//        for(int i = 0; i < pieceList.size(); i++){
//            for(int j = 0; j < pieceList.get(i).size(); j++){
//                NestPath path = pieceList.get(i).get(j);
//                if(GeometryUtil.isConvex(path)){ //凸多边形
//                    convexList.add(path);
//                }else{
//                    nonConvexList.add(path);
//                }
//            }
//        }
    }

    /**
     * 结合凹凸多边形
     */
    public List<Piece> combineConvexAndConcave(List<NestPath> convexList, List<NestPath> nonConvexList) {
        List<Piece> result = new ArrayList<>(); //保存最终结果
        while(!nonConvexList.isEmpty()) {
            NestPath nonConvex = nonConvexList.get(0);
            // 要有退出的条件
            int j;
            // DEBUG: 对点数小的凹多边形，不处理
//            if(nonConvex.size() <= 5){
//                nonConvexList.remove(nonConvex);
//                result.add(NestPathtoPiece(nonConvex));
//                continue;
//            }
            for (j = 0; j < convexList.size(); j++) {
                NestPath convex = convexList.get(j);

                /**
                 * 改start
                 */
                // todo: 这里就可以用禁忌表来ban掉一些情况了
                // 先排个序，拼接后再比较？
                String smallStrId = convex.getStrId().compareTo(nonConvex.getStrId()) <= 0 ? convex.getStrId() : nonConvex.getStrId();
                String bigStrId = convex.getStrId().compareTo(nonConvex.getStrId()) > 0 ? convex.getStrId() : nonConvex.getStrId();
                String uionStr = smallStrId + "|" + bigStrId;
                if(tabuQueue.contains(uionStr)){ //如果禁忌表里存在，不合并，尝试下一个
                    continue;
                }
                /**
                 * 改end
                 */

                NestPath newConvex = canCombine(nonConvex, convex);
                if (newConvex != null) {
//                    NestPath newConvex = combine(nonConvex, convexList.get(j)); //合并后得到新的凸多边形
                    // 暂时先添加进凸多边形列表吧
                    convexList.add(newConvex);
                    nonConvexList.remove(nonConvex); //删掉组合的凹多边形
                    convexList.remove(j); //删掉用去组合的凸多边形
                    break;
                }
            }
            // 遍历完了，也找不到能组装的凸多边形，将该凹多边形直接加入结果
            if(j == convexList.size()){
                nonConvexList.remove(nonConvex);
                result.add(NestPathtoPiece(nonConvex));
            }

        }
        for (NestPath path : convexList) { //把凸多边形加入
            path.setArea(GeometryUtil.polygonArea(path)); //求面积
//            result.add(NestPathtoPiece(path));
            // TODO: 这里改了，变成有加子零件的情况
            result.add(NestPathtoPieceIncludeChild(path));
        }
        return result;
    }

    /**
     * 结合凹凸多边形2，不转化为piece
     */
    public List<NestPath> combineConvexAndConcave2(List<NestPath> convexList, List<NestPath> nonConvexList) {
        List<NestPath> result = new ArrayList<>(); //保存最终结果
        while(!nonConvexList.isEmpty()) {
            NestPath nonConvex = nonConvexList.get(0);
            // 要有退出的条件
            int j;
            // DEBUG: 对点数小的凹多边形，不处理
//            if(nonConvex.size() <= 5){
//                nonConvexList.remove(nonConvex);
//                result.add(NestPathtoPiece(nonConvex));
//                continue;
//            }

            for (j = 0; j < convexList.size(); j++) {
                NestPath convex = convexList.get(j);

                /**
                 * 改start
                 */
                // todo: 这里就可以用禁忌表来ban掉一些情况了
                // 先排个序，拼接后再比较？
                String smallStrId = convex.getStrId().compareTo(nonConvex.getStrId()) <= 0 ? convex.getStrId() : nonConvex.getStrId();
                String bigStrId = convex.getStrId().compareTo(nonConvex.getStrId()) > 0 ? convex.getStrId() : nonConvex.getStrId();
                String uionStr = smallStrId + "|" + bigStrId;
                if(tabuQueue.contains(uionStr)){ //如果禁忌表里存在，不合并，尝试下一个
                    continue;
                }
                /**
                 * 改end
                 */
                
                // 尝试合并，如果合并失败，newConvex为null
                NestPath newConvex = canCombine(nonConvex, convex);
                if (newConvex != null) {
//                    NestPath newConvex = combine(nonConvex, convexList.get(j)); //合并后得到新的凸多边形
                    // 暂时先添加进凸多边形列表吧
                    convexList.add(newConvex);
                    nonConvexList.remove(nonConvex); //删掉组合的凹多边形
                    convexList.remove(j);            //删掉用去组合的凸多边形
                    break;
                }
            }

            // 遍历完了，也找不到能组装的凸多边形，将该凹多边形直接加入结果
            if(j == convexList.size()){
                nonConvexList.remove(nonConvex);
                result.add(nonConvex);
            }
        }

        // 把凸多边形加入合并结果
        for (NestPath path : convexList) {
            path.setArea(GeometryUtil.polygonArea(path)); //求面积
            result.add(path);
        }
        return result;
    }



    /**
     * 判断能否组合凹凸
     * 属于第一轮，考虑顶点个数
     * @param nonConvex
     * @return
     */
    public NestPath canCombine(NestPath nonConvex, NestPath convex) {
        // 太大了，超过边界了，不再合并
        if( (nonConvex.getMaxX()-nonConvex.getMinX()) > xObject || (nonConvex.getMaxY()-nonConvex.getMinY()) > yObject
                || (convex.getMaxX()-convex.getMinX()) > xObject || (convex.getMaxY()-convex.getMinY()) > yObject ){
            return null;
        }

        // 遍历所有凹多边形的顶点
        for(int i = 0; i < nonConvex.size(); i++){
            // 移动凸多边形
            // 遍历所有凸多边形的顶点，并移动到凹多边形的顶点处
            for(int j = 0; j < convex.size(); j++){
                double shifx = nonConvex.get(i).x - convex.get(j).x;
                double shify = nonConvex.get(i).y - convex.get(j).y;

                // 构建移动后的多边形
                NestPath shifPath = new NestPath();

                for (int m = 0; m < convex.size(); m++) {
                    shifPath.add(new Segment(Math.rint(convex.get(m).x + shifx), Math.rint(convex.get(m).y + shify)));
                }

                // 判断是否交或者在里面
                if(GeometryUtil.interseccionPP(NestPathtoPiece(shifPath), NestPathtoPiece(nonConvex)) || GeometryUtil.intersect(shifPath, nonConvex) || GeometryUtil.intersect(nonConvex, shifPath) || GeometryUtil.insidePP2(shifPath, nonConvex) || GeometryUtil.insidePP2(nonConvex, shifPath) ){
                    continue;
                }else{
                    // 不相交，也不在里面

                    // 取并后看看顶点数有没有变少，弱多边形
                    Paths combinePath = unionPath(scaleUp2ClipperCoordinates(shifPath), scaleUp2ClipperCoordinates(nonConvex));
                    // 如果是两个，不行
                    if(combinePath.size() > 1){
                        continue;
                    }
                    NestPath newConvex = toNestCoordinates(combinePath.get(0)); //新的凸多边形

                    // 如果太大了，超过边界了，跳过
                    if((newConvex.getMaxX()-newConvex.getMinX()) > xObject || (newConvex.getMaxY()-newConvex.getMinY()) > yObject){
                        continue;
                    }

                    // 防止重叠：GeometryUtil.polygonArea(newConvex) != GeometryUtil.polygonArea(nonConvex)
//                    if( newConvex.size() <= nonConvex.size() && GeometryUtil.polygonArea(newConvex) != GeometryUtil.polygonArea(nonConvex) && GeometryUtil.polygonArea(newConvex) != GeometryUtil.polygonArea(convex)){ //顶点数变少了
//                        // 如果能正常返回，就起个id
//                        long nonConvexId = nonConvex.getLongId();
//                        long convexId = convex.getLongId();
//                        String id1 = String.valueOf(nonConvexId);
//                        String id2 = String.valueOf(convexId);
//                        String newId = id1 + id2; //用"2003"表示2和3结合了
//                        //String uionStr = id1 + "|" + id2;
//                        long newConvexId = Long.valueOf(newId);
//                        newConvex.setId(newConvexId);
//                        // 小的在前，大的在后，拼接id
//                        String smallStrId = convex.getStrId().compareTo(nonConvex.getStrId()) <= 0 ? convex.getStrId() : nonConvex.getStrId();
//                        String bigStrId = convex.getStrId().compareTo(nonConvex.getStrId()) > 0 ? convex.getStrId() : nonConvex.getStrId();
//                        String uionStr = smallStrId + "|" + bigStrId;
//                        newConvex.setStrId(smallStrId + "0" + bigStrId); //用0拼接两个id
//
//                        /**
//                         * fitnss改start
//                         */
//                        // 先用减少凹多边形的顶点数来判断，防止重复
//                        // TODO: 加一些启发式规则
//                        // 1.要不要改成nfp绕一圈合并？可以。找顶点合并，看顶点数
//                        // 2.大零件的边占掉的比例，我想设计一个如果有一整条边重合是加大分的，如果是少量重合
//                        // 3.占包络矩形的比例也可以设置为平方，加大作用
//                        double fitness = calculateFitness(newConvex, nonConvex, convex, shifPath); //计算启发函数
//                        if(fitness >= threshold){ //如果启发函数够高
//                            // return newconvex;
//                        }
//
//                        // 记录合并的过程
//                        Map<String, Double> temp = new HashMap<>();
//                        temp.put(uionStr, fitness);
//                        processUion.add(temp);
//
//                        /**
//                         * 改end
//                         */
//
//                        shifPath.setId(convex.getId());
//                        shifPath.setArea(convex.getArea());
//                        shifPath.leftChild = convex.leftChild;
//                        shifPath.rightChild = convex.rightChild;
//                        // TODO:这里子元素也要跟着移动才行
//                        newConvex.leftChild = shifPath;
//                        newConvex.rightChild = nonConvex;
//
//                        return newConvex;
//                    }else{
//                        //该凸多边形的顶点不行，换下一个凸多边形的顶点试试
//                        continue;
//                    }

                    // 防止重叠: GeometryUtil.polygonArea(newConvex) != GeometryUtil.polygonArea(nonConvex)
                    if( GeometryUtil.polygonArea(newConvex) != GeometryUtil.polygonArea(nonConvex) && GeometryUtil.polygonArea(newConvex) != GeometryUtil.polygonArea(convex)){
                        // 如果能正常返回，就起个id
                        long nonConvexId = nonConvex.getLongId();
                        long convexId = convex.getLongId();
                        String id1 = String.valueOf(nonConvexId);
                        String id2 = String.valueOf(convexId);
                        String newId = id1 + id2; //用"2003"表示2和3结合了
                        //String uionStr = id1 + "|" + id2;
                        //long newConvexId = Long.valueOf(newId);
                        //newConvex.setId(newConvexId);
                        // 小的在前，大的在后，拼接id
                        String smallStrId = convex.getStrId().compareTo(nonConvex.getStrId()) <= 0 ? convex.getStrId() : nonConvex.getStrId();
                        String bigStrId = convex.getStrId().compareTo(nonConvex.getStrId()) > 0 ? convex.getStrId() : nonConvex.getStrId();
                        String uionStr = smallStrId + "|" + bigStrId;
                        newConvex.setStrId(smallStrId + "&" + bigStrId); //用0拼接两个id

                        /**
                         * fitnss改start
                         */
                        // 计算启发函数
                        // TODO: 加一些启发式规则
                        // 1.要不要改成nfp绕一圈合并？可以。找顶点合并，看顶点数
                        // 2.大零件的边占掉的比例，我想设计一个如果有一整条边重合是加大分的，如果是少量重合
                        // 3.占包络矩形的比例也可以设置为平方，加大作用
                        double fitness = calculateFitness(newConvex, nonConvex, convex, shifPath);
                        if(fitness >= threshold){ //如果启发函数够高
                            // 记录合并的过程
                            Map<String, Double> temp = new HashMap<>();
                            temp.put(uionStr, fitness);
                            processUion.add(temp);

                            /**
                             * 改end
                             */

                            // shifpath是由convex移动来的
                            shifPath.setId(convex.getId());
                            shifPath.setArea(convex.getArea());
                            shifPath.setChild(convex.getChild());
                            shifPath.setStrId(convex.getStrId());
                            shifPath.leftChild = convex.leftChild;
                            shifPath.rightChild = convex.rightChild;
                            // TODO:这里子元素也要跟着移动才行
                            moveChildofPath(convex, shifx, shify);
                            newConvex.leftChild = convex;
                            newConvex.rightChild = nonConvex;
                            newConvex.child.add(convex);
                            newConvex.child.add(nonConvex);

                            return newConvex;
                        }

                    }else{
                        //该凸多边形的顶点不行，换下一个凸多边形的顶点试试
                        continue;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 凹凸结合的启发函数，有顶点因素影响
     * 新多边形，凹多边形，凸多边形，凸多边形移动后的多边形
     *
     */
    private double calculateFitness(NestPath newConvex, NestPath nonConvex, NestPath convex, NestPath shifPath) {
        // todo: 我感觉不用加权重，因为你也不知道哪个重要，只设计一个参数，就是fitness的接受阈值，会慢慢降低
        double fitness = 0;
        int numOfNewConvex = newConvex.size();
        // 计算面积较大的 或者 凹的多边形的顶点 或者 顶点较大的多边形的顶点，此处为后者
        int numOfNonConvex = Math.max(nonConvex.size(), convex.size());
        // 1.用顶点的个数比值来计算fitness吗，应该说newConvex顶点数比nonConvex少了fitness就高，顶点数比nonConvex多了fitness就小
        // TODO: 怎么设计这个函数，要点1，顶点数减少会大幅提高fitness，使得合并成为可能，顶点数增加也不一定是坏事，但怎么设计呢，也许正的不考虑，负的就提高
        // 或者只有第一轮凹凸结合会考虑这个，确实，对凸凸结合来说，包络矩形的提升更为重要
        fitness += numOfNewConvex - numOfNonConvex <= 0 ? numOfNonConvex - numOfNewConvex + 1 : 0; //如果顶点数减少或者不变，则fitness增加Δ（顶点数的变化量）+1，否则，不增加
        // 2.计算大零件被占的边比例的平方
        // 互相遍历,如果有重叠的边,有的话计算(重叠部分的长度/相应大边的长度)的平方，平方是为了加大影响
        double edgeOverlapRatio = GeometryUtil.adjancencyAllEdgeRatioPP(nonConvex, shifPath);
        fitness += edgeOverlapRatio;
        // 3.计算新零件的包络矩形占比，要不要平方，或者是凸包占包络矩形的比例的平方，这里使用包络矩形可以解释为底板为矩形
        // TODO: 还是占比的提升度？
        fitness += Math.pow(GeometryUtil.polygonArea(newConvex)/GeometryUtil.getPolygonBounds(newConvex).getArea(), 2);
        return fitness;
    }

    /**
     * 计算两个零件重叠的边的重叠比例，计算方法为各个边的重叠率的平方的和（没用上）
     */
    private double calculateAllEdgeOverlapRatio(NestPath nonConvex, NestPath shifPath) {
        double ratio = 0;
        // 先遍历nonConvex的每一条边
        List<Segment> segmentsNon = nonConvex.getSegments();
        List<Segment> segmentsShif = shifPath.getSegments();
        for(int i = 0; i < segmentsNon.size(); i++){
            Segment seg1 = segmentsNon.get(i);
            // 再遍历shifpath的每一条边
            for(int j = 0; j < segmentsShif.size(); j++){
                Segment seg2 = segmentsShif.get(j);
                ratio += calculateSingleEdgeOverlapRatio(seg1, seg2);
            }
        }
        return ratio;
    }


    /**
     * 计算两条线段的重叠比例，公式为重叠部分占较长边的比例（没用上）
     */
    private double calculateSingleEdgeOverlapRatio(Segment seg1, Segment seg2) {
        //double overlapRatio = GeometryUtil.adjancencyAllEdgeRatioPP();
        return 0;
    }


    /**
     * 结合
     * @param nonConvex
     * @param path
     * @return
     */
//    public NestPath combine(NestPath nonConvex, NestPath path) {
//        Paths path1 = unionPath(scaleUp2ClipperCoordinates(nonConvex), scaleUp2ClipperCoordinates(path));
//        return toNestCoordinates(path1.get(0));
//    }

    /**
     * 结合凹凸多边形
     */
    public List<Piece> combineNonConvex(){
        // 保存凹凸多边形列表
        List<NestPath> convexList = new ArrayList<>();
        List<NestPath> nonConvexList = new ArrayList<>();
        // 将凹凸多边形区分开
        findConvexAndNonconvex(originPieceList, convexList, nonConvexList);

        //原本
//        List<Piece> result = combineConvexAndConcave(convexList, nonConvexList);
//        result = rectangularity(result);
//        return result;

        // 结合凹凸多边形，这一步可能要去掉
        List<NestPath> result = combineConvexAndConcave2(convexList, nonConvexList);

        // 矩形化
        List<Piece> pieceResult = rectangularity2(result);

        return pieceResult;
    }

    /**
     * 提升矩形度
     * @param result
     * @return
     */
    public List<Piece> rectangularity(List<Piece> result) {
        // 先用NestPath保存下来
        List<NestPath> newPieceList = new ArrayList<>();
        for(int i = 0; i < result.size(); i++){
            newPieceList.add(PiecetoNestPath(result.get(i)));
        }
        // 先排个序？
        Collections.sort(newPieceList);

        // 两两合并，todo:直到启发式函数不再满足or已经矩形了？有一个必要的条件就是合并后不能超过bin的长宽
        int iter = 30;
        while(iter > 0){
            boolean canCombine = false;
            for(int i = 0; i < newPieceList.size(); i++){
                if(canCombine){
                    break;
                }
                NestPath ithNestPath = newPieceList.get(i);
                for(int j = newPieceList.size()-1; j >= i+1; j--){
                    if(canCombine){
                        break;
                    }
                    NestPath jthNestPath = newPieceList.get(j);

                    /**
                     * 改start
                     */
                    // todo: 判断是否在禁忌表里
                    // 小的在前，大的在后，拼接id
                    String smallStrId = ithNestPath.getStrId().compareTo(jthNestPath.getStrId()) <= 0 ? ithNestPath.getStrId() : jthNestPath.getStrId();
                    String bigStrId = ithNestPath.getStrId().compareTo(jthNestPath.getStrId()) > 0 ? ithNestPath.getStrId() : jthNestPath.getStrId();
                    String uionStr = smallStrId + "|" + bigStrId;
                    if(tabuQueue.contains(uionStr)){
                        continue;
                    }
                    /**
                     * 改end
                     */

                    // 组合提高矩形度
                    NestPath newPiece = combineAndImproveRegularity(ithNestPath, jthNestPath);
                    if( newPiece != null && newPiece.size() > 0 ){
                        newPieceList.remove(ithNestPath);
                        newPieceList.remove(jthNestPath);
                        newPieceList.add(newPiece);
                        canCombine = true;
                        break;
                    }
                }
            }
            iter--;
        }

        //转换回piece
        List<Piece> newResult = new ArrayList<>();
        for(int i = 0; i < newPieceList.size(); i++){
            NestPath path = newPieceList.get(i);
            path.setArea(GeometryUtil.polygonArea(path)); //求面积
//            newResult.add(NestPathtoPiece(path));
            // TODO: 这里也改了，改成有子零件的
            newResult.add(NestPathtoPieceIncludeChild(path));
        }
        return newResult;
    }

    /**
     * 提升矩形度
     * @return
     */
    public List<Piece> rectangularity2(List<NestPath> newPieceList) {

        // 先排个序？
        Collections.sort(newPieceList);

        // 两两合并，todo:直到启发式函数不再满足or已经矩形了？有一个必要的条件就是合并后不能超过bin的长宽
        int iter = 30;
        while(iter > 0){
            boolean canCombine = false; //记录该轮是否合并过，若合并过则进入下一轮
            for(int i = 0; i < newPieceList.size(); i++){
                if(canCombine){
                    break;
                }
                NestPath ithNestPath = newPieceList.get(i);
                for(int j = newPieceList.size()-1; j >= i+1; j--){
                    if(canCombine){
                        break;
                    }
                    NestPath jthNestPath = newPieceList.get(j);

                    /**
                     * 改start
                     */
                    // todo: 判断是否在禁忌表里
                    // 小的在前，大的在后，拼接id
                    String smallStrId = ithNestPath.getStrId().compareTo(jthNestPath.getStrId()) <= 0 ? ithNestPath.getStrId() : jthNestPath.getStrId();
                    String bigStrId = ithNestPath.getStrId().compareTo(jthNestPath.getStrId()) > 0 ? ithNestPath.getStrId() : jthNestPath.getStrId();
                    String uionStr = smallStrId + "|" + bigStrId;
                    if(tabuQueue.contains(uionStr)){
                        continue;
                    }
                    /**
                     * 改end
                     */

                    // 组合提高矩形度
                    NestPath newPiece = combineAndImproveRegularity(ithNestPath, jthNestPath);
                    if( newPiece != null && newPiece.size() > 0 ){
                        newPieceList.remove(ithNestPath);
                        newPieceList.remove(jthNestPath);
                        newPieceList.add(newPiece);
                        canCombine = true;
                        break;
                    }
                }
            }
            iter--;
        }

        //转换回piece
        List<Piece> newResult = new ArrayList<>();
        for(int i = 0; i < newPieceList.size(); i++){
            NestPath path = newPieceList.get(i);
            path.setArea(GeometryUtil.polygonArea(path)); //求面积
//            newResult.add(NestPathtoPiece(path));
            // TODO: 这里也改了，改成有子零件的
            newResult.add(NestPathtoPieceIncludeChild(path));
        }
        return newResult;
    }

    /**
     * 合并两个零件，看能否提高矩形度
     */
    public NestPath combineAndImproveRegularity(NestPath ithNestPath, NestPath jthNestPath) {
        // 矩形度为1了，不结合
        if(calRegularity(ithNestPath) == 1 || calRegularity(jthNestPath) == 1){
            return null;
        }
        // 太大了，超过边界了，不再合并
        if( (ithNestPath.getMaxX()-ithNestPath.getMinX()) > xObject || (ithNestPath.getMaxY()-ithNestPath.getMinY()) > yObject
            || (jthNestPath.getMaxX()-jthNestPath.getMinX()) > xObject || (jthNestPath.getMaxY()-jthNestPath.getMinY()) > yObject ){
            return null;
        }

        return canCombineRegularity(ithNestPath, jthNestPath);
    }

    /**
     * 计算零件的矩形度
     */
    public double calRegularity(NestPath path) {
        Bound polygonBounds = GeometryUtil.getPolygonBounds(path);
        return (GeometryUtil.polygonArea(path)/polygonBounds.getArea());
    }

    /**
     * 判断能否组合从而提升矩形度，可以是两个凸多边形也可以是两个凹多边形
     * convex是移动的那个
     * @param nonConvex
     * @return
     */
    public NestPath canCombineRegularity(NestPath nonConvex, NestPath convex) {
        double areaNonConvex = GeometryUtil.polygonArea(nonConvex);
        double areaConvex = GeometryUtil.polygonArea(convex);
        // 旋转convex
        NestPath origin = null;
        try { //先克隆一个原始版本
            origin = convex.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        int rotation = convex.getRotation();
//        for(int t = 0; t <= 3; t++){
//            rotation += (t*90) % 360;
//            convex = GeometryUtil.rotatePolygon2Polygon(origin, rotation);

            // 遍历所有凹多边形的顶点
            for(int i = 0; i < nonConvex.size(); i++){
                // 移动凸多边形
                // 遍历所有凸多边形的顶点，并移动到凹多边形的顶点处
                for(int j = 0; j < convex.size(); j++){
                    double shifx = nonConvex.get(i).x - convex.get(j).x;
                    double shify = nonConvex.get(i).y - convex.get(j).y;

                    // 构建移动后的凸多边形
                    NestPath shifPath = new NestPath();

                    for (int m = 0; m < convex.size(); m++) {
                        shifPath.add(new Segment(Math.rint(convex.get(m).x + shifx), Math.rint(convex.get(m).y + shify)));
                    }

                    // TODO：先做个判断，看shifPath即移动后的凸多边形是否有两个点与凹多边形重合，即占掉一条边，才继续，否则不继续
//                    if(!whetherTwoPointsOverlap(shifPath, nonConvex)){
//                        continue;
//                    }

                    // 判断是否交或者在里面
                    if(GeometryUtil.interseccionPP(NestPathtoPiece(shifPath), NestPathtoPiece(nonConvex)) || GeometryUtil.insidePP(shifPath, nonConvex)  || GeometryUtil.insidePP2(shifPath, nonConvex) || GeometryUtil.insidePP2(nonConvex, shifPath) ){
                        continue;
                    }else{
                        // 不相交，也不在里面
                        // 取并后看看顶点数有没有变少，弱多边形

                        Paths combinePath = unionPath(scaleUp2ClipperCoordinates(shifPath), scaleUp2ClipperCoordinates(nonConvex));
                        // 如果是两个，不行
                        if(combinePath.size() > 1){
                            continue;
                        }
                        NestPath newConvex = toNestCoordinates(combinePath.get(0)); //组合成新的凸多边形

                        // 如果太大了，超过边界了，跳过
                        if((newConvex.getMaxX()-newConvex.getMinX()) > xObject || (newConvex.getMaxY()-newConvex.getMinY()) > yObject){
                            continue;
                        }
                        // TODO：面积较大的多边形的矩形度提升了，并且占掉大多边形的一整条边
//                        if(areaNonConvex >= areaConvex && calRegularity(newConvex) > calRegularity(nonConvex)
//                                || areaConvex >= areaNonConvex && calRegularity(newConvex) > calRegularity(convex) ){
//                            // 如果能正常返回，就起个id
//                            long nonConvexId = nonConvex.getLongId();
//                            long convexId = convex.getLongId();
//                            String id1 = String.valueOf(nonConvexId);
//                            String id2 = String.valueOf(convexId);
//                            String newId = id1 + id2; //用"2003"表示2和3结合了
//                            //String uionStr = id1 + "|" + id2;
//                            long newConvexId = Long.valueOf(newId);
//                            newConvex.setId(newConvexId);
//                            // 小的在前，大的在后，拼接id
//                            String smallStrId = convex.getStrId().compareTo(nonConvex.getStrId()) <= 0 ? convex.getStrId() : nonConvex.getStrId();
//                            String bigStrId = convex.getStrId().compareTo(nonConvex.getStrId()) > 0 ? convex.getStrId() : nonConvex.getStrId();
//                            String uionStr = smallStrId + "|" + bigStrId;
//                            newConvex.setStrId(smallStrId + "0" + bigStrId); //用0拼接两个id
//                            // todo: 还是用list来保存合并过程，不用str
//
//                            /**
//                             * 改start
//                             */
//                            // 记录合并的过程
//                            double fitness = calculateFitness(newConvex, nonConvex, convex, shifPath); //计算启发函数
//
//                            // todo: fitness > threshold 可以合并，初始threshold为2，后续慢慢降低
//                            if(fitness >= threshold){
//
//                            }
//
//                            Map<String, Double> temp = new HashMap<>();
//                            temp.put(uionStr, fitness);
//                            processUion.add(temp);
//                            /**
//                             * 改end
//                             */
//
//
//                            // 用children元素记录由哪些零件组成
//                            shifPath.setId(convex.getId());
//                            shifPath.setArea(convex.getArea());
//                            shifPath.leftChild = convex.leftChild;
//                            shifPath.rightChild = convex.rightChild;
//                            // TODO:这里子元素也要跟着移动才行
//                            newConvex.leftChild = shifPath;
//                            newConvex.rightChild = nonConvex;
//
//
//                            return newConvex;
//                        }
                        // 记录合并的过程
                        double fitness = calculateFitness(newConvex, nonConvex, convex, shifPath); //计算启发函数
                        if(fitness >= threshold ){
                            // 如果能正常返回，就起个id
                            long nonConvexId = nonConvex.getLongId();
                            long convexId = convex.getLongId();
                            String id1 = String.valueOf(nonConvexId);
                            String id2 = String.valueOf(convexId);
                            String newId = id1 + id2; //用"2003"表示2和3结合了
                            //String uionStr = id1 + "|" + id2;
                            //long newConvexId = Long.valueOf(newId);
                            //newConvex.setId(newConvexId);
                            // 小的在前，大的在后，拼接id
                            String smallStrId = convex.getStrId().compareTo(nonConvex.getStrId()) <= 0 ? convex.getStrId() : nonConvex.getStrId();
                            String bigStrId = convex.getStrId().compareTo(nonConvex.getStrId()) > 0 ? convex.getStrId() : nonConvex.getStrId();
                            String uionStr = smallStrId + "|" + bigStrId;
                            newConvex.setStrId(smallStrId + "&" + bigStrId); //用0拼接两个id
                            // todo: 还是用list来保存合并过程，不用str

                            /**
                             * 改start
                             */
                            // 保存合并结果
                            Map<String, Double> temp = new HashMap<>(); //保存[合并，fitness]
                            temp.put(uionStr, fitness);
                            processUion.add(temp);
                            /**
                             * 改end
                             */


                            // 用children元素记录由哪些零件组成
                            shifPath.setId(convex.getId());
                            shifPath.setArea(convex.getArea());
                            shifPath.setChild(convex.getChild());
                            shifPath.setStrId(convex.getStrId());
                            shifPath.leftChild = convex.leftChild;
                            shifPath.rightChild = convex.rightChild;
                            // TODO:这里子元素也要跟着移动才行
                            moveChildofPath(convex, shifx, shify);
                            newConvex.leftChild = convex;
                            newConvex.rightChild = nonConvex;
                            newConvex.child.add(convex);
                            newConvex.child.add(nonConvex);


                            return newConvex;
                        }

                    }
                }
            }

//        }

        return null;
    }

    /**
     * 移动零件的孩子
     */
    public void moveChildofPath(NestPath path, double shifx, double shify){
        // 对孩子移动
        if(path.child.size() > 0){
            moveChildofPath(path.child.get(0), shifx, shify);
            moveChildofPath(path.child.get(1), shifx, shify);
        }
        // 移动自己
        for (int m = 0; m < path.size(); m++) {
            Segment segment = path.get(m);
            segment.x += shifx;
            segment.y += shify;
        }
    }

    /**
     * 判断两个多边形是否有两个点重叠
     */
    public boolean whetherTwoPointsOverlap(NestPath shifPath, NestPath nonConvex) {
        int cnt = 0;
        for(int i = 0; i < shifPath.size(); i++){
            for(int j = 0; j < nonConvex.size(); j++){
                if(shifPath.get(i).x == nonConvex.get(j).x && shifPath.get(i).y == nonConvex.get(j).y){
                    cnt++;
                }
            }
        }
        if(cnt == 2){
            return true;
        }
        return false;
    }


}

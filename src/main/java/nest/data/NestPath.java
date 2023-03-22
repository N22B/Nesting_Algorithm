package nest.data;

import de.lighti.clipper.*;
import nest.util.CommonUtil;
import nest.util.Config;
//import nest.util.CommonUtil;
//import nest.util.Config;

import java.util.ArrayList;
import java.util.List;


public class NestPath  implements Comparable<NestPath>, Cloneable{
    private List<Segment> segments; //构成多边形的点
    private List<NestPath> children;
    private NestPath parent;
    public double offsetX;
    public double offsetY;

//    private int id;
    private long id;
    private int source ;
    private int rotation; // 旋转角度
    public int myRotation; // 旋转角度
    public Config config ;
    public double area ;

    public int bid;

    // 左右孩子
    public NestPath leftChild;
    public NestPath rightChild;

    // id字符串
    public String strId;

    // 还是用数组来解决零件由哪些组成的问题
    public List<Integer> listofNestPath = new ArrayList<>();

    public void setStrId(String strId){
        this.strId = strId;
    }

    public String getStrId(){
        return this.strId;
    }

    public NestPath getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(NestPath leftChild) {
        this.leftChild = leftChild;
    }

    public NestPath getRightChild() {
        return rightChild;
    }

    public void setRightChild(NestPath rightChild) {
        this.rightChild = rightChild;
    }

    // 子元素
    public List<NestPath> child = new ArrayList<>();

    public void setChild(List<NestPath> child){
        this.child = child;
    }

    public List<NestPath> getChild(){
        return this.child;
    }

    @Override
    public String toString() {
        return "NestPath{" +
                "segments=" + segments +
                ", children=" + children +
                ", parent=" + parent +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", id=" + id +
                ", source=" + source +
                ", rotation=" + rotation +
                ", myRotation=" + myRotation +
                ", config=" + config +
                ", area=" + area +
                ", bid=" + bid +
                '}';
    }

    public void add(double x , double y ){
        this.add(new Segment(x,y));
    }

    @Override
    public NestPath clone() throws CloneNotSupportedException {
        return (NestPath) super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        NestPath nestPath = (NestPath) obj;
        if(segments.size() != nestPath.size()){
            return false;
        }
        for(int  i =0 ; i <segments.size(); i ++){
            if(!segments.get(i).equals(nestPath.get(i))){
                return false;
            }
        }
        if(children.size() != nestPath.getChildren().size()){
            return false;
        }
        for(int i = 0 ; i<children.size(); i ++){
            if(!children.get(i).equals(nestPath.getChildren().get(i))){
                return false;
            }
        }
        return true;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * 丢弃最后一个segment
     */
    public void pop(){
        segments.remove(segments.size()-1);
    }

    public void reverse(){
        List<Segment> rever = new ArrayList<Segment>();
        for(int i = segments.size()-1; i >=0; i -- ){
            rever.add(segments.get(i));
        }
        segments.clear();
        for(Segment s : rever ){
            segments.add(s);
        }
    }

    public Segment get(int i){
        return segments.get(i);
    }

    public NestPath getParent() {
        return parent;
    }

    public void setParent(NestPath parent) {
        this.parent = parent;
    }

    public void addChildren(NestPath nestPath){
        children.add(nestPath);
        nestPath.setParent(this);
    }

    public List<NestPath> getChildren() {
        return children;
    }

    public void setChildren(List<NestPath> children) {
        this.children = children;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public NestPath(){
        offsetX = 0;
        offsetY = 0;
        children = new ArrayList<NestPath>();
        segments = new ArrayList<Segment>();
        config = new Config();
        area = 0;
    }


    public NestPath(Config config) {
        offsetX = 0;
        offsetY = 0;
        children = new ArrayList<NestPath>();
        segments = new ArrayList<Segment>();
        area = 0;
        this.config = config;
    }

    public NestPath(NestPath srcNestPath){
        segments = new ArrayList<Segment>();
        for(Segment segment : srcNestPath.getSegments() ){
            segments.add(new Segment(segment));
        }

        this.id  = srcNestPath.id;
        this.rotation = srcNestPath.rotation;
        this.source = srcNestPath.source;
        this.offsetX = srcNestPath.offsetX;
        this.offsetY = srcNestPath.offsetY;
        this.bid = srcNestPath.bid;
        this.area = srcNestPath.area;
        children = new ArrayList<NestPath>();

        for(NestPath nestPath: srcNestPath.getChildren()){
            NestPath child = new NestPath(nestPath);
            child.setParent(this);
            children.add(child);
        }
    }

    public static NestPath cleanNestPath(NestPath srcPath){
        /**
         * Convert NestPath 2 Clipper
         */

        Path path = CommonUtil.NestPath2Path(srcPath);

        Paths simple = DefaultClipper.simplifyPolygon(path, Clipper.PolyFillType.NON_ZERO);
        if(simple.size() == 0 ){
            return null;
        }
        Path biggest = simple.get(0);
        double biggestArea = Math.abs(biggest.area());
        for(int i = 0; i <simple.size();i++){
            double area = Math.abs(simple.get(i).area());
            if(area > biggestArea ){
                biggest = simple.get(i);
                biggestArea = area;
            }
        }
        Path clean = biggest.cleanPolygon(srcPath.config.CURVE_TOLERANCE * Config.CLIIPER_SCALE);
        if(clean.size() == 0 ){
            return null ;
        }

        /**
         *  Convert Clipper 2 NestPath
         */
        NestPath cleanPath = CommonUtil.Path2NestPath(clean);
        cleanPath.bid = srcPath.bid;
        cleanPath.setRotation(srcPath.rotation);
        return cleanPath;
    }

    /**
     * 通过平移将NestPath的最低x坐标，y坐标的值必定都是0，
     */
    public void Zerolize(){
        ZeroX();ZeroY();
    }

    private void ZeroX(){
        double xMin = Double.MAX_VALUE;
        for(Segment s : segments){
            if(xMin > s.getX() ){
                xMin = s.getX();
            }
        }
        for(Segment s :segments ){
            s.setX(s.getX() - xMin );
        }
    }

    private void ZeroY(){
        double yMin = Double.MAX_VALUE;
        for(Segment s : segments){
            if(yMin > s.getY() ){
                yMin = s.getY();
            }
        }
        for(Segment s : segments ){
            s.setY(s.getY() - yMin);
        }
    }

    public void clear(){
        segments.clear();
    }

    public int size(){
        return segments.size();
    }

    public void add(Segment s){
        segments.add(s);
    }

    public List<Segment> getSegments() {
        return segments;
    }


    public int getId() {
        return (int)id;
    }

    public long getLongId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public int compareTo(NestPath o) {
        double area0  = this.area;
        double area1 = o.area;
        if(area0 > area1 ){
            return 1;
        }
        else if(area0 == area1){
            return 0;
        }
        return -1;
    }

    public double getMaxY(){
        double MaxY = Double.MIN_VALUE;
        for(Segment s : segments){
            if(MaxY < s.getY()){
                MaxY = s.getY();
            }
        }
        return MaxY;
    }

    public double getMinY(){
        double MinY = Double.MAX_VALUE;
        for(Segment s : segments){
            if(MinY > s.getY()){
                MinY = s.getY();
            }
        }
        return MinY;
    }

    public double getMaxX(){
        double MaxX = Double.MIN_VALUE;
        for(Segment s : segments){
            if(MaxX < s.getX()){
                MaxX = s.getX();
            }
        }
        return MaxX;
    }

    public double getMinX(){
        double MinX = Double.MAX_VALUE;
        for(Segment s : segments){
            if(MinX > s.getX()){
                MinX = s.getX();
            }
        }
        return MinX;
    }


    public void translate(double x,  double y ){
        for(Segment s : segments){
            s.setX(s.getX() + x );
            s.setY(s.getY() + y);
        }
    }
}

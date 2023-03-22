import de.lighti.clipper.Clipper;
import de.lighti.clipper.DefaultClipper;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import nest.data.NestPath;
import nest.util.GeometryUtil;
import nest.util.coor.NestCoor;
import org.junit.Test;
import share.Piece;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyTest {

    public class Person {
        public String name;
        // 省略构造函数、Getter&Setter方法
    }

    @Test
    public void test01() {
        List<List<Integer>> pieceList = new ArrayList<>();
//        pieceList.get(3).add(1);

        System.out.println(pieceList);
    }

    @Test
    public void test02() {
        String s = "abc";
        s = "edf";
        System.out.println(s);
    }

    @Test
    public void test03() {
        Person xiaoZhang = new Person();
        Person xiaoLi = new Person();
        xiaoLi = xiaoZhang;
        xiaoZhang.name = "abc";
//        change(xiaoZhang);
        System.out.println(xiaoLi.name);
        System.out.println(xiaoZhang);

//        Person xiaoLi = new Person("小李");
//        swap(xiaoZhang, xiaoLi);
//        System.out.println("xiaoZhang:" + xiaoZhang.getName());
//        System.out.println("xiaoLi:" + xiaoLi.getName());
    }

    @Test
    public void test04() {
        float a = 0.1f;
        System.out.println(a);
    }

    public void change(Person person) {
        System.out.println(person);
        person.name = "edf";
    }

    @Test
    public void test05() {
        double a = 2.9999;

        System.out.println(a);
        System.out.println(Math.rint(a));
    }

    @Test
    public void test06() {
        NestPath path = new NestPath();
        path.add(0, 0);
        path.add(10, 0);
        path.add(10, 10);
        path.add(0, 10);

        NestPath path2 = new NestPath();
        path2.add(10, 0);
        path2.add(20, 0);
        path2.add(20, 20);
        path2.add(10, 10);

        System.out.println(GeometryUtil.intersect(path, path2));
//        System.out.println(GeometryUtil.isConvex(path));
    }

    @Test
    public void testStringCompare() {
        String a = "2";
        String b = "2";
        System.out.println(a.compareTo(b));
    }

    @Test
    public void TestReadExcel() {
        String path = "C:\\Users\\zzb\\Desktop\\NestingData\\";
        String fileName = "albano.xls";
        try {
            Workbook workbook = Workbook.getWorkbook(new File(path + fileName));
            Sheet sheet = workbook.getSheet(2);
            for (int i = 0; i < sheet.getRows(); i = i + 2) { //第i行
                Cell[] cells = sheet.getRow(i);//获取第i行的cell
                int len = cells.length;
                for (int j = 0; j < len; j++) {
                    int x = Integer.valueOf(sheet.getCell(j, i).getContents());
                    int y = Integer.valueOf(sheet.getCell(j, i + 1).getContents());
                    System.out.println("x:" + x);
                    System.out.println("y:" + y);
                }
//                for (Cell cell : cells) {
//                    String contents = cell.getContents();
//
//                    System.out.println(contents);
//                }


//                for(int j = 0; j < 50; j++){ //第j列
//                    Cell[] row1 = sheet.getRow(i);
//                    Cell[] column = sheet.getColumn(j);
//                    // 第j列第i行
//                    int row = sheet.getCell(j, i).getRow();
//                    String contents = sheet.getCell(j, i).getContents();
//                    System.out.println("row"+row);
//                    System.out.println("contens"+contents);
//                }
//                String contents = sheet.getCell(0, i).getContents();
//                System.out.println(contents);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRotate() {
        int[] coord1 = {0, 0, 100, 0, 100, 30, 80, 100, 0, 100};
        Piece piece1 = new Piece(coord1);

        int[] coord2 = {100, 30, 100, 100, 80, 100};
        Piece piece2 = new Piece(coord2);

        piece1.rotate(90);
        piece1.rotateCori(90);
        piece2.rotate(90);
        piece2.rotateCori(90);

        System.out.println(piece1);
        System.out.println(piece2);

    }


    @Test
    public void countDownTest() {
        CountDownLatch countDownLatch = new CountDownLatch(10);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        String[] all = new String[10];
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int id = i;
            executorService.submit(() -> {
                singleTask(all, random, id);
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("游戏开始");
        executorService.shutdown();
    }

    private void singleTask(String[] all, Random random, int id) {
        for (int j = 0; j <= 100; j++) {
            try {
                Thread.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            all[id] = j + "%";
            System.out.print("\r" + Arrays.toString(all));
        }
    }
}

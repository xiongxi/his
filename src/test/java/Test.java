/**
 * Created by xiongxi on 2018/1/7.
 */
public class Test {

    public static void main(String[] args){
        /*int a = 567;
        System.out.println(a/10/10%10);
        System.out.println(a/10%10);
        System.out.println(a%10);*/
//        System.out.println(a % 100);
        String a = "说明.TxT";
        System.out.println(a.endsWith("\\w+"));
        System.out.println(a.matches("说明\\.[T|t][X|x][T|t]"));
//        FileUtils

        String[] strArray = {"1","2","3"};
        for(String str : strArray){
            System.out.println(str);
            if(str.endsWith("2")){
                break;
            }
        }
    }
}

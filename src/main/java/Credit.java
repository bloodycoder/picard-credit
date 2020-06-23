import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Scanner;


public class Credit {
    //读取json文件
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            FileInputStream fileReader = new FileInputStream(fileName);
            Reader reader = new InputStreamReader(fileReader);
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String getPath()
    {
        String jarWholePath = Credit.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            jarWholePath = java.net.URLDecoder.decode(jarWholePath, "UTF-8");
        } catch (UnsupportedEncodingException e) { System.out.println(e.toString()); }
        String jarPath = new File(jarWholePath).getParentFile().getAbsolutePath();
        return jarPath;
    }
    public static void main(String[] args) throws IOException, ParseException {
        //InputStream pathin = Credit.class.getClassLoader().getResourceAsStream("credit.json");
        //String path = Credit.class.getClassLoader().getResource("credit.json").getPath();
        MsgConst.filePath = getPath()+"/credit.json";
        String s = readJsonFile(MsgConst.filePath);
        JSONObject jobj = JSON.parseObject(s);
        //JSONArray movies = jobj.getJSONArray("RECORDS");//构建JSONArray数组
        /*
        for (int i = 0 ; i < movies.size();i++){
            JSONObject key = (JSONObject)movies.get(i);
        }*/

        System.out.println("Welcome.Type 'help' for help.");

        Scanner commandIn = new Scanner(System.in);
        CommandHandler handle = new CommandHandler(jobj,commandIn);
        String cmd;
        int err;
        while(true){
            System.out.print("picard>");
            cmd = commandIn.nextLine().toLowerCase();
            err = handle.parseStr(cmd);
            if(err == -1){
                System.out.println("Server error please retry.");
                break;
            }
            else if(err == MsgConst.EXITClIENT){
                System.out.println("Exit system.");
                break;
            }
        }
        commandIn.close();
    }
}
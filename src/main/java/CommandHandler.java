import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
class Job{
    String message;
    int day;
    Job(String message,int day){
        this.message = message;
        this.day = day;
    }
    public static void prtSortedJob(ArrayList<Job> sortedJob){
        Collections.sort(sortedJob, new Comparator<Job>() {
            @Override
            public int compare(Job o1, Job o2) {
                return o1.day-o2.day;
            }
        });
        for(Job j:sortedJob){
            BeautiConsole.colorPrint(j.message,BeautiConsole.RED,-1);
        }
    }
}
class FolderInfo{
    JSONArray folderJSONArr;
    String foldernameStack;
    int cdIndex;
}
class CommandHandler{
    public JSONObject jobj;
    public int credit;
    int currentJobIndex;
    Scanner sin;
    public static SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
    JSONArray rootJobList;
    JSONArray currentJobList;
    JSONArray activities;
    LinkedList<FolderInfo>folderInfoStack;
    ArrayList<Job> sortedJob;
    String currentJobName;
    public CommandHandler(JSONObject jobj, Scanner sin) throws ParseException {
        this.jobj = jobj;
        this.credit = (Integer) jobj.get("score");
        this.sin = sin;
        this.checkWeekCard();
        this.rootJobList = jobj.getJSONArray("jobList");
        this.activities = jobj.getJSONArray("activities");
        this.currentJobList = this.rootJobList;
        this.currentJobName = "全部任务";
        this.sortedJob = new ArrayList<Job>();
        this.folderInfoStack = new LinkedList<FolderInfo>();
        this.SortShopActivities();
    }
    public void prtJob(JSONArray jobRoot,int cengji,int daylimit) throws ParseException {
        Iterator iter = jobRoot.iterator();
        int index = 0;
        while(iter.hasNext()){
            JSONObject job = (JSONObject)iter.next();
            String Jobname = (String)job.get("jobName");
            int credit = Integer.parseInt(job.getString("jobCredit"));
            if(daylimit == MsgConst.NOLIMIT){
                for(int i=0;i<cengji;i++)
                    System.out.print("  ");
                if(cengji == 0){
                    BeautiConsole.colorPrint(index+","+Jobname+"#"+credit+"\n",BeautiConsole.RED,-1);
                }
                else{
                    System.out.println(Jobname+"#"+credit);
                }
            }
            else{
                //parse date
                String dateStr = (String)job.get("dueDate");
                Date jobduedate = ft.parse(dateStr);
                Date today = new Date();
                int daybetween = util.daysBetween(today,jobduedate);
                if(daybetween <= daylimit){
                    Job myjob = new Job(Jobname+"#"+credit+" in "+daybetween+" days "+"duedate "+dateStr+" \n",daybetween);
                    this.sortedJob.add(myjob);
                    //BeautiConsole.colorPrint(Jobname+"#"+credit+" in "+daybetween+" days "+"duedate "+dateStr+" \n",BeautiConsole.RED,-1);
                }
            }
            JSONArray subJob = job.getJSONArray("subJob");
            if(subJob!= null && subJob.size()>0){
                prtJob(subJob,cengji+1,daylimit);
            }
            index++;
        }
    }
    public int checkWeekCard() throws ParseException {
        Date dNow = new Date();
        JSONArray jarr = jobj.getJSONArray("weekCard");
        if(jarr == null){
            return 0;
        }
        Iterator iter = jarr.iterator();
        while(iter.hasNext()){
            String dateStr = (String)iter.next();
            Date date = ft.parse(dateStr);
            if(dNow.after(date)){
                this.credit+=MsgConst.WEEKCARD;
                iter.remove();
            }
        }
        if(jarr.size() == 0){
            jobj.remove("weekCard");
        }
        return 0;
    }
    public int SaveJson() throws IOException {
        //String path = Credit.class.getClassLoader().getResource("credit.json").getPath();
        String json = jobj.toJSONString();
        File jsonFile = new File(MsgConst.filePath);
        FileWriter fileWrite = new FileWriter(jsonFile);
        fileWrite.write(json);
        fileWrite.close();
        return 0;
    }
    public void SortShopActivities(){
        Collections.sort(this.activities, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                String s1 = String.valueOf(o1);
                String s2 = String.valueOf(o2);
                int credit1 = Integer.parseInt(s1.split("#")[1]);
                int credit2 = Integer.parseInt(s2.split("#")[1]);
                return credit1-credit2;
            }
        });
    }
    public int parseStr(String cmd) throws IOException, ParseException {
        String[]s;
        int errcode = 0;
        SaveJson();
        try{
            s = cmd.split(" ");
        }
        catch (Exception e){
            System.out.println("command error.please type 'help' for help");
            return -1;
        }
        if(s.length>0){
            /**/
            if(s[0].compareTo("quit") == 0 || s[0].compareTo("exit") == 0 || s[0].compareTo("q") == 0 || s[0].compareTo("e") == 0){
                jobj.put("score",credit);
                SaveJson();
                return MsgConst.EXITClIENT;
            }
            if(s[0].compareTo("help") == 0){
                System.out.println("shop:显示全部");
                System.out.println("buy:购买");
                System.out.println("finish:完成任务");
                System.out.println("touch touch -l touch -r");
                System.out.println("exit:离开");
            }
            if(s[0].compareTo("ls") == 0){
                //show all jobs
                if(s.length>=2 && s[1].compareTo("-w") == 0 ){
                    //显示一周的
                    BeautiConsole.colorPrint("一周内任务\n",BeautiConsole.YELLOW,-1);
                    this.sortedJob.clear();
                    prtJob(currentJobList,0,MsgConst.WEEK);
                    Job.prtSortedJob(this.sortedJob);
                    return 0;
                }
                else if(s.length>=2 && s[1].compareTo("-m") == 0 ){
                    //显示一周的
                    BeautiConsole.colorPrint("一月内任务\n",BeautiConsole.YELLOW,-1);
                    prtJob(currentJobList,0,MsgConst.MONTH);
                    Job.prtSortedJob(this.sortedJob);
                    return 0;
                }
                else if(s.length>=2 && s[1].compareTo("-hy") == 0 ){
                    //显示一周的
                    BeautiConsole.colorPrint("半年内任务\n",BeautiConsole.YELLOW,-1);
                    prtJob(currentJobList,0,MsgConst.HALF_YEAR);
                    Job.prtSortedJob(this.sortedJob);
                    return 0;
                }
                else if(s.length>=2 && s[1].compareTo("-y") == 0 ){
                    //显示一周的
                    BeautiConsole.colorPrint("一年内任务\n",BeautiConsole.YELLOW,-1);
                    prtJob(currentJobList,0,MsgConst.YEAR);
                    Job.prtSortedJob(this.sortedJob);
                    return 0;
                }
                BeautiConsole.colorPrint(currentJobName+"\n",BeautiConsole.YELLOW,-1);
                if(currentJobList.size()>0){
                    prtJob(currentJobList,0,MsgConst.NOLIMIT);
                }
                else{
                    System.out.println("暂时无任务");
                }
            }
            else if(s[0].compareTo("cd") == 0){
                if(s[1].compareTo("~")==0){
                    currentJobList = jobj.getJSONArray("jobList");
                    currentJobName = "全部任务";
                    this.folderInfoStack.clear();
                    return 0;
                }
                if(s[1].compareTo("..") == 0){
                    if(folderInfoStack.size()<1)
                        return 0;
                    FolderInfo tmpInfo = folderInfoStack.pollLast();
                    currentJobList = tmpInfo.folderJSONArr;
                    currentJobName = tmpInfo.foldernameStack;
                    return 0;
                }
                int index = Integer.parseInt(s[1]);
                if(index<currentJobList.size()){
                    JSONObject job = currentJobList.getJSONObject(index);
                    FolderInfo tmpInfo = new FolderInfo();
                    tmpInfo.folderJSONArr = currentJobList;
                    tmpInfo.foldernameStack = currentJobName;
                    tmpInfo.cdIndex = index;
                    folderInfoStack.offerLast(tmpInfo);
                    currentJobIndex = index;
                    currentJobName = job.getString("jobName");
                    currentJobList = job.getJSONArray("subJob");
                }
                else{
                    System.out.println("切换目录错误");
                }
            }
            // 创建工作
            else if(s[0].compareTo("touch") == 0 || s[0].compareTo("t") == 0 ) {
                if(s.length>=2 && (s[1].compareTo("-list")==0 || s[1].compareTo("-l")==0)){
                    System.out.println("公共部分?");
                    String commonPart = sin.nextLine();
                    String jobdate = "2099-12-29";
                    System.out.println("从哪到哪? num~num");
                    String[]credit = sin.nextLine().split("~");
                    int from = Integer.parseInt(credit[0]);
                    int to = Integer.parseInt(credit[1]);
                    if(from>to)
                        return 0;
                    System.out.println("credit");
                    String fenshu = sin.nextLine();
                    if(fenshu.length() == 0)
                        fenshu = "0";
                    for(int i=from;i<=to;i++){
                        JSONObject newJob = new JSONObject();
                        newJob.put("jobName",commonPart+Integer.toString(i));
                        newJob.put("jobCredit",fenshu);
                        newJob.put("dueDate",jobdate);
                        JSONArray subjob = new JSONArray();
                        newJob.put("subJob",subjob);
                        currentJobList.add(newJob);
                    }
                    return 0;
                }
                boolean rootFlag = false;
                if(s.length>=2 && s[1].compareTo("-r") == 0){
                    rootFlag = true;
                }
                System.out.println("jobname?");
                String jobname = sin.nextLine();
                if(jobname.length()==0){
                    System.out.println("任务名不能为空");
                    return 0;
                }
                System.out.println("jobdate?yyyy-mm-dd or mm-dd");
                String jobdate = sin.nextLine();
                if(jobdate.length()<=2){
                    jobdate = "2099-12-29";
                }
                else if(jobdate.length() <= 5){
                    jobdate = util.getYear()+"-"+jobdate;
                }
                System.out.println("credit");
                String credit = sin.nextLine();
                if(credit.length() == 0)
                    credit = "0";
                JSONObject newJob = new JSONObject();
                newJob.put("jobName",jobname);
                newJob.put("jobCredit",credit);
                newJob.put("dueDate",jobdate);
                JSONArray subjob = new JSONArray();
                newJob.put("subJob",subjob);
                if(rootFlag)
                    rootJobList.add(newJob);
                else
                    currentJobList.add(newJob);
            }
            else if(s[0].compareTo("finish")==0 || s[0].compareTo("f")==0){
                if(s.length>=2 && s[1].compareTo("-c") == 0 || s.length == 1){
                    //finish current
                    if(folderInfoStack.size()<1){
                        BeautiConsole.colorPrintln("错误，不能在根目录完成全部任务",BeautiConsole.RED,-1);
                        return 0;
                    }
                    System.out.print("你确定要完成任务");
                    BeautiConsole.colorPrintln(currentJobName,BeautiConsole.YELLOW,-1);
                    String y = sin.nextLine();
                    if(y.compareTo("y") == 0){
                        FolderInfo tmpInfo = folderInfoStack.pollLast();
                        currentJobList = tmpInfo.folderJSONArr;
                        currentJobName = tmpInfo.foldernameStack;
                        int index = tmpInfo.cdIndex;
                        JSONObject job = currentJobList.getJSONObject(index);
                        credit += Integer.parseInt(job.getString("jobCredit"));
                        currentJobList.remove(index);
                        System.out.println("成功");
                    }
                    return 0;
                }
                int index = Integer.parseInt(s[1]);
                JSONObject job = currentJobList.getJSONObject(index);
                System.out.print("你确定要完成任务");
                BeautiConsole.colorPrintln(job.getString("jobName"),BeautiConsole.YELLOW,-1);
                String y = sin.nextLine();
                if(y.compareTo("y") == 0){
                    credit += Integer.parseInt(job.getString("jobCredit"));
                    currentJobList.remove(index);
                    System.out.println("成功");
                }
                else
                    return 0;
                //credit+= value;
                SaveJson();
            }
            else if(s[0].compareTo("remove")==0 || s[0].compareTo("rm")==0){
                int index = Integer.parseInt(s[1]);
                JSONObject job = currentJobList.getJSONObject(index);
                System.out.print("你确定要删除任务");
                BeautiConsole.colorPrintln(job.getString("jobName"),BeautiConsole.YELLOW,-1);
                String y = sin.nextLine();
                if(y.compareTo("y") == 0){
                    currentJobList.remove(index);
                    System.out.println("成功");
                }
                else
                    return 0;
                //credit+= value;
            }
            else if(s[0].compareTo("shop") == 0 || s[0].compareTo("s") == 0 ){
                //todo 排序功能
                if(s.length >=2 && (s[1].compareTo("touch")==0 || s[1].compareTo("t")==0)){
                    //创建
                    System.out.println("新的活动名称?");
                    String acname = sin.nextLine();
                    if(acname.length()==0){
                        System.out.println("活动名不能为空");
                        return 0;
                    }
                    System.out.println("credit");
                    String credit = sin.nextLine();
                    if(credit.length() == 0)
                        credit = "0";
                    String newActivity = acname+"#"+credit;
                    activities.add(newActivity);
                    System.out.println("成功");
                    this.SortShopActivities();
                    return 0;
                }
                else if(s.length >=3 && s[1].compareTo("rm")==0) {
                    //删除
                    int index = Integer.parseInt(s[2]);
                    if(activities.size()>index){
                        System.out.print("你确定要删除活动");
                        BeautiConsole.colorPrintln(activities.getString(index),BeautiConsole.YELLOW,-1);
                        String y = sin.nextLine();
                        if(y.compareTo("y") == 0){
                            activities.remove(index);
                            System.out.println("成功");
                        }
                    }
                    return 0;
                }
                else{
                    JSONArray zhouka = jobj.getJSONArray("weekCard");
                    int zhoukacnt = 0;
                    if(zhouka != null){
                        zhoukacnt = zhouka.size();
                    }
                    System.out.print("欢迎来到超市，显示所有娱乐活动，现有分值");
                    BeautiConsole.colorPrint(String.valueOf(credit),BeautiConsole.YELLOW,-1);
                    System.out.print(",周卡张数");
                    BeautiConsole.colorPrint(String.valueOf(zhoukacnt)+"\n",BeautiConsole.YELLOW,-1);
                    for(int i=0;i<activities.size();i++){
                        String name = (String)activities.get(i);
                        if(i%2==0)
                            BeautiConsole.colorPrintln(i+","+name,BeautiConsole.PURPLE,-1);
                        else
                            BeautiConsole.colorPrintln(i+","+name,BeautiConsole.YELLOW,-1);
                    }
                }
            }
            else if(s[0].compareTo("buy") == 0 || s[0].compareTo("b") == 0 ){
                JSONArray activities = jobj.getJSONArray("activities");
                String name = (String)activities.get(Integer.parseInt(s[1]));
                String[]activity = name.split("#");
                int creditNeed = Integer.parseInt(activity[1]);
                /*
                if(creditNeed>credit){
                    System.out.println("没有足够的金币");
                    return 0;
                }*/
                System.out.println("确定要花"+creditNeed+"来兑换"+activity[0]+"吗？(Y/N)");
                String line = sin.nextLine();
                if(line.compareTo("y")==0 || line.compareTo("Y") == 0){
                    //to do
                    credit -= creditNeed;
                    if(activity[0].compareTo("周卡购买") == 0){
                        //购买周卡
                        Date dNow = new Date();
                        Calendar cal = new GregorianCalendar();
                        cal.setTime(dNow);
                        cal.add(Calendar.DATE,7);
                        dNow = cal.getTime();
                        JSONArray jarr = jobj.getJSONArray("weekCard");
                        if(jarr == null){
                            jarr = new JSONArray();
                        }
                        jarr.add(ft.format(dNow));
                        jobj.put("weekCard",jarr);
                    }
                    System.out.println("成功!");
                }
                SaveJson();
            }
        }
            return errcode;
        }
    }
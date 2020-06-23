import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class util {
    public static int daysBetween(Date smdate, Date bdate) throws ParseException
    {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        smdate = ft.parse(ft.format(smdate));
        bdate = ft.parse(ft.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days=Math.abs(time2-time1)/(1000*3600*24);
        return Integer.parseInt(String.valueOf(between_days));
    }
    public static String getYear(){
        Calendar cal = Calendar.getInstance();
        return String.valueOf(cal.get(Calendar.YEAR));
    }
}

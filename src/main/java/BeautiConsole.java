public class BeautiConsole {
    public static final int WHITE = 0;
    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int YELLOW = 3;
    public static final int BLUE = 4;
    public static final int PURPLE = 5;
    public static final int LOWBLUE = 6;
    public static final int GREY = 7;
    public static void colorPrint(String words,int fontColor,int bgColor){
        fontColor+=30;
        if(bgColor == -1){
            System.out.print("\033["+fontColor+";4m"+words+"\033[0m");
        }
        else{
            bgColor+=40;
            System.out.print("\033["+bgColor+";"+fontColor+";4m"+words+"\033[0m");
        }
    }
    public static void colorPrintln(String words,int fontColor,int bgColor){
        fontColor+=30;
        if(bgColor == -1){
            System.out.println("\033["+fontColor+";4m"+words+"\033[0m");
        }
        else{
            bgColor+=40;
            System.out.println("\033["+bgColor+";"+fontColor+";4m"+words+"\033[0m");
        }
    }
}

public class SubClass extends SuperClass{
    protected int b;

    @Override
    public void func1() {
        b = 3;
    }
    
    @Override
    public void func2() {
        if (b == 3)
            System.out.println("b == 3");
    }
    
    public SubClass(int bb) {
        super();
        b = bb;
    }

    public static void main (String[] args) {
        SuperClass sClass = new SubClass(1);
        sClass.start();
    }
}

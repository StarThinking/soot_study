public abstract class SuperClass {
    public abstract void func1();
    public abstract void func2();

    public void start() {
        func1();
        System.out.println("start");
        func2();
    }

    public SuperClass() {
    }
}


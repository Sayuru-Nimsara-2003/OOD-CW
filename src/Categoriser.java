public abstract class Categoriser {
    private String articleTitle;
    private String articleContent;
    private String category;

    public abstract void categorise();
}

class NLPModel1 extends Categoriser{    // Replace class name with the model name
    @Override
    public void categorise() {
        // categoriser implementation
    }
}

class NLPModel2 extends Categoriser{    // Replace class name with the model name
    @Override
    public void categorise() {
        // categoriser implementation
    }
}

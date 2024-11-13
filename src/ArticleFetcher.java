public abstract class ArticleFetcher {
    private Article article;

    public abstract void convert();
}

class FileFetcher extends ArticleFetcher{

    @Override
    public void convert() {
        // Get articles from files
    }
}


class APIFetcher extends ArticleFetcher{

    @Override
    public void convert() {
        //get articles from API
    }
}
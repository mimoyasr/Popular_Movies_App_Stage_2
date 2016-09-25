package app.com.example.mimo.moviez;

public class Trailer {
    private String key;
    private String name;

    public Trailer( String key, String name) {
        setKey(key);
        setName(name);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}


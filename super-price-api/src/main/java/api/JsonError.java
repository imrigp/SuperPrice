package api;

public class JsonError {
    String error;

    public JsonError(String msg) {
        this.error = msg;
    }

    public String getError() {
        return error;
    }

    public void setError(String msg) {
        this.error = msg;
    }

    public static JsonError build(String error) {
        return new JsonError(error);
    }
}

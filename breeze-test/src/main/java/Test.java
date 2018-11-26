import org.lendingclub.http.breeze.BreezeHttp;
import org.lendingclub.http.breeze.client.okhttp3.BreezeHttpOk3Client;

public class Test {
    public static void main(String[] args) {
        BreezeHttp breeze = new BreezeHttpOk3Client();
        System.out.println(breeze.get("https://jsonplaceholder.typicode.com/users"));
    }
}

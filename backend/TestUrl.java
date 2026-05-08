import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class TestUrl {
    public static void main(String[] args) {
        String urlStr = "https://releases.ubuntu.com/22.04.4/ubuntu-22.04.4-desktop-amd64.iso";
        try {
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            // Standard User-Agent
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            System.out.println("Connecting to: " + urlStr);
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            long fileSize = connection.getContentLengthLong();
            System.out.println("Content Length: " + fileSize);
            
            if (responseCode >= 300 && responseCode < 400) {
                String location = connection.getHeaderField("Location");
                System.out.println("Redirected to: " + location);
            }
            
            String acceptRanges = connection.getHeaderField("Accept-Ranges");
            System.out.println("Accept-Ranges: " + acceptRanges);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

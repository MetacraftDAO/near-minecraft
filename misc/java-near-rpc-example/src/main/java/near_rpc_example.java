// Method 1
// import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;

// import java.io.ByteArrayOutputStream;
// import java.nio.charset.StandardCharsets;
// import java.util.HashMap;
// import java.util.Map;
// import java.net.URL;

// Method 2
// import com.thetransactioncompany.jsonrpc2.client.*;
// import com.thetransactioncompany.jsonrpc2.*;

// Method 3
import org.kurento.jsonrpc.*;
import org.kurento.jsonrpc.message.Request;
import org.kurento.jsonrpc.message.Response;
import org.kurento.jsonrpc.client.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class near_rpc_example {
    public static void main(String[] args) throws Exception {
        System.out.println("This is an example of querying NEAR blockchain for account information.");

        // // Method 1, doesn't work ==========

        // JSONRPC2Session mySession = new JSONRPC2Session(new URL("https://rpc.testnet.near.org"));
        // // Construct new request
        // String method = "query";
        // int requestID = 0;
        // JSONRPC2Request request = new JSONRPC2Request(method, requestID);

        // // Send request
        // JSONRPC2Response response = null;

        // try {
        //         response = mySession.send(request);

        // } catch (JSONRPC2SessionException e) {
        //         System.out.println("RPC call failed");
        //         System.err.println(e.getMessage());
        // }

        // System.out.println("Still alive");

        // // Print response result / error
        // if (response.indicatesSuccess()) {
        //     System.out.println(response.getResult());
        // } else {
        //     System.out.println(response.getError().getMessage());
        // }


        // // Method 2, doesn't work ==========
        // ByteArrayOutputStream byteArrayOutputStream;
        // JsonRpcHttpClient client;

        // client = new JsonRpcHttpClient(
        //     new URL("https://rpc.testnet.near.org"));

        // Map<String, Object> params = new HashMap<>();
        // params.put("request_type", "view_account");
        // params.put("finality", "final");
        // params.put("account_id", "ycli.testnet");

        // // byteArrayOutputStream = new ByteArrayOutputStream();
        // // client.invoke("query", new Object[] { "view_account", "final", "ycli.testnet" }, byteArrayOutputStream);
        // // JsonNode node = client.getObjectMapper().readTree(byteArrayOutputStream.toString(StandardCharsets.UTF_8.name()));
        // try {
        //     Map<String, Object> node = client.invoke("query", params, Map.class);
        //     System.out.println("Still alive");
        //     ObjectMapper objectMapper = new ObjectMapper();
        //     String json = objectMapper.writeValueAsString(node);
        //     System.out.println(json);
        //     System.out.println("Success!");
        // } catch (Throwable e) {
        //     System.out.println("Something went wrong in RPC call: " + e.toString());
        // }


        // // Method 3 ============
        try {
            JsonRpcClient client = new JsonRpcClientHttp("https://rpc.testnet.near.org");
            Request<JsonObject> request = new Request<>();
            request.setMethod("query");
            JsonObject params = new JsonObject();
            params.addProperty("request_type", "view_account");
            params.addProperty("finality", "final");
            params.addProperty("account_id", "ycli.testnet");
            request.setParams(params);

            Response<JsonElement> response = client.sendRequest(request);
            System.out.println("Request sent");
            if (response.isError()) {
                System.out.println("Error: " + response.getError().toString());
            } else {
                System.out.println(response.getResult().toString());
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}

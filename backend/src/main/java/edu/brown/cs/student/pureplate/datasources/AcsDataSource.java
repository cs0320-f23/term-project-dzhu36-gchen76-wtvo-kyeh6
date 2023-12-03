package edu.brown.cs.student.pureplate.datasources;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import okio.Buffer;

/**
 * This class queries the ACS API and retrieves the broadband access percentage for a URI containing
 * specific state and county identifiers.
 */
public class AcsDataSource implements Query<String, String> {

  /**
   * Retrieves the broadband percentage from the ACS API.
   *
   * @param target - the URI to query to; it should contain state and county identifiers
   * @return the String broadband percentage for the state and county specified in the target
   * @throws IOException if there are any issues with establishing the URL connection
   */
  @Override
  public String query(String target) throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    Type listStrings = Types.newParameterizedType(List.class, List.class, String.class);
    JsonAdapter<List<List<String>>> jsonAdapter = moshi.adapter(listStrings);
    URL requestUrl = new URL(target);
    HttpURLConnection clientConnection = (HttpURLConnection) requestUrl.openConnection();
    clientConnection.connect();
    List<List<String>> dataList =
        jsonAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    List<String> targetRow = dataList.get(1);
    String broadband = targetRow.get(1);
    return broadband;
  }
}

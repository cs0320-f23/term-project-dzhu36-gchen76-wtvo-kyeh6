package edu.brown.cs.student.pureplate.datasources;

import java.io.IOException;
import java.net.URISyntaxException;
import org.json.simple.parser.ParseException;

/**
 * An interface used by classes that call (and perhaps cache results from) the ACS API (as well as
 * classes that query other data-sources such as the GeoJson redlining data).
 *
 * @param <RESULT> the type of the elements returned by the result
 * @param <TARGET> the type of the value being queried for
 */
public interface Query<RESULT, TARGET> {

  /**
   * A method that retrieves the associated result of the user-provided target. In the context of
   * broadband percentage retrieval, the target should be a call to the ACS API with state and
   * county parameters, while the result should be the associated broadband percentage.
   *
   * @param target - the URI of the ACS API with state and county identifiers (or in the context of
   *               the redlining data, a type that contains all the parameters to filter the
   *               redlining data on)
   * @return the result associated with the target
   * @throws URISyntaxException   if an error occurs with processing the target as a URI
   * @throws IOException          if any broad user input error occurs
   * @throws InterruptedException if a query is interrupted
   */
  RESULT query(TARGET target)
      throws URISyntaxException, IOException, InterruptedException, ParseException;
}

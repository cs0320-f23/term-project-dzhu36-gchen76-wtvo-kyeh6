package edu.brown.cs.student.broadbandtests;

import edu.brown.cs.student.mapserver.datasources.Query;

/**
 * A class that implements the Query interface and substitutes for a Query that calls an API (in
 * this case, the ACS API). Used for testing the BroadBandHandler class.
 */
public class MockBroadbandQuery implements Query<String, String> {

  /**
   * Always returns a constant value, called when queries are made to an API.
   *
   * @param uri - the URI of an API (in this case, the ACS API with state and county identifiers)
   * @return a constant value of "0.0" (mimicking the broadband percentages that ApiCall and
   * CachedApiCall should return)
   */
  @Override
  public String query(String uri) {
    return "0.0";
  }
}

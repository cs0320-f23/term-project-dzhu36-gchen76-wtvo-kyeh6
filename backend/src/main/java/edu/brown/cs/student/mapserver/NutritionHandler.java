package edu.brown.cs.student.mapserver;

import spark.Request;
import spark.Response;
import spark.Route;

public class NutritionHandler implements Route {

  @Override
  public Object handle(Request request, Response response){
    return "hi";
  }
}

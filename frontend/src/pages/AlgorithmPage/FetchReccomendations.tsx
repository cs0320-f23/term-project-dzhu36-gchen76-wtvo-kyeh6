const recommendationsURL = "http://localhost:3233/pureplate?"

export async function getPurePlateData(weight: string, age: string, height: string, gender: string, activityLevel: string, growable: string, foods: string): Promise<string | string[]> {
    try {
        // return "hi"
        const pureplate_response = await fetch(
          `${recommendationsURL}weight=${weight}&height=${height}&age=${age}&gender=${gender}&activity=${activityLevel}&growable=${growable}&foods=${foods}`
        );
        const pureplate_json = await pureplate_response.json();
        if (!isSuccessfulRecommendationCall(pureplate_json)) {
          if ("message" in pureplate_json) {
            return pureplate_json.message;
          }
          return "Input data was not in the correct format";
        }
        const recommendations = pureplate_json.recommendations;
        console.log(typeof recommendations);
        return recommendations;
        // let state = area_api_json.results[0].state_name;
        // let county = area_api_json.results[0].county_name;
        // const split_state = state.split(" ");
        // return recommendations; // maybe represented as a table?
      } catch (err) {
        return "Unable to retrive nutrition data";
      }
}

interface SuccessfulRecommendationCall {
  result: string;
  recommendations: string;
}

function isSuccessfulRecommendationCall(
  rjson: any
): rjson is SuccessfulRecommendationCall {
  if (!("result" in rjson)) return false;
  if (!("recommendations" in rjson)) return false;
  return true;
}

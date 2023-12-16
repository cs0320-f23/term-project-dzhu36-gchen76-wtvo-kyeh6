const recommendationsURL = "http://localhost:3233/pureplate?"

export async function getPurePlateData(weight: string, age: string, height: string, gender: string, activityLevel: string, foods: string): Promise<string> {
    try {
        return "hi"
        const pureplate_response = await fetch(
          `${recommendationsURL}weight=${weight}&height=${height}&age=${age}&gender=${gender}&activity=${activityLevel}&foods=${foods}`
        );
        const pureplate_json = await pureplate_response.json();
        const recommendations = pureplate_json.recommendations;
        console.log(recommendations.class)
        // let state = area_api_json.results[0].state_name;
        // let county = area_api_json.results[0].county_name;
        // const split_state = state.split(" ");
        
        return recommendations; // maybe represented as a table?
      } catch (err) {
        return "Unable to retrive nutrition data";
      }
}
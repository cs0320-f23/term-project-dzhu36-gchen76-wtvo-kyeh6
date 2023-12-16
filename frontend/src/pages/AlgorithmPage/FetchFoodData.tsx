const foodsURL = "http://localhost:3233/data?"

export async function getInitialFood() : Promise<Map<string, Map<string,Number>> | string>{
    try {
      const data_response = await fetch(`${foodsURL}`);
      const data_json = await data_response.json();
      console.log(data_json.foods);
      return data_json.foods;
    } catch (err) {
      return "Unable to retrive food data";
    }
}

// Type Narrowing of response here
interface SuccessfulFoodDataCall {
  results: string[];
}


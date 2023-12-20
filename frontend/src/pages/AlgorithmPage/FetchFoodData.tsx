import React, { useEffect, useState } from 'react';
import { FormControl, FormGroup, FormControlLabel, Checkbox } from '@mui/material';


const foodsURL = "http://localhost:3233/data?"

export async function getInitialFood() : Promise<string[]| string>{
    try {
      const data_response = await fetch(`${foodsURL}`);
      const data_json = await data_response.json();
      console.log("food data");
      console.log(data_json.foods);
      return data_json.foods;
    } catch (err) {
      return "Unable to retrive food data";
    }
}

// export default function YourComponent() {
//   const [foodOptions, setFoodOptions] = useState([]);

//   useEffect(() => {
//     const fetchFoodOptions = async () => {
//       try {
//         const initialFood:string | string[] = await getInitialFood();
//         setFoodOptions(initialFood);
//       } catch (error) {
//         console.error('Error fetching food options:', error);
//       }
//     };

//     fetchFoodOptions();
//   }, []);
// }
// Type Narrowing of response here
interface SuccessfulFoodDataCall {
  results: string[];
}

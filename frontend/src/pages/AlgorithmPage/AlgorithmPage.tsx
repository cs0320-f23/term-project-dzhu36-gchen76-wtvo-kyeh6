//will have name of patient box, age box, and weight box
import React, { useState, useEffect } from "react";
import Header from "../../Header";
import SearchHistory from "./SearchHistory";
import { getInitialFood } from "./FetchFoodData";
import { Dispatch, SetStateAction } from "react";
import { ChangeEvent } from 'react';
import { FormControl, FormGroup, FormControlLabel, Checkbox } from '@mui/material';
import { getPurePlateData } from "./FetchReccomendations";

function AlgorithmPage() {

/**
 * Defines how a REPL function should look
 */
interface REPLFunction {
  (args: string[]): Promise<string | string[][]>;
}

  const [age, setAge] = useState("");
  const [weight, setWeight] = useState("");
  const [height, setHeight] = useState("");
  const [gender, setGender] = useState("");
  const [activityLevel, setActivityLevel] = useState("");
  const [growable, setGrowable] = useState("");
  const [history, setHistory] = useState<(string | string[])[]>([[]]);

  // const Age = document.getElementById('txtbx1')
  // const Height = document.getElementById('txtbx2')
  // const Weight = document.getElementById('txtbx3')
  // const submitBtn = document.getElementById('Submit_Button')


  function handleActivityLevelChange(event: ChangeEvent<HTMLInputElement>) {
    setActivityLevel(event.target.value);
  }
  
  function handleGenderChange(event: ChangeEvent<HTMLInputElement>) {
    const value = event.target.value;
    if (value === "Male" || value === "Female") {
      setGender(value);
    } else {
      setGender("");
      window.alert("Please select a gender");
      // setError('Please enter a valid number for age');
    }
  }
  
  function handleWeightChange(event: ChangeEvent<HTMLInputElement>) {
    const value = event.target.value;
    if (!isNaN(parseFloat(value)) && parseFloat(value).toString() === value) {
      setWeight(value); // Update the state if the input is a valid number
    } 
    else {
      setWeight('');
      window.alert("Value entered is not a valid number")
      // setError('Please enter a valid number for age');
    }
  }
  
  function handleHeightChange(event: ChangeEvent<HTMLInputElement>) {
    const value = event.target.value;
    if (!isNaN(parseInt(value)) && parseInt(value).toString() === value) {
      setHeight(value); // Update the state if the input is a valid number
    } else {
      console.log("error");
      setHeight("");
      window.alert("Value entered is not a valid number");
    }
  }
  
  function handleAgeChange(event: ChangeEvent<HTMLInputElement>) {
    const value = event.target.value;
    if (typeof value === 'string'){
    }
    if (!isNaN(parseInt(value)) && parseInt(value).toString() === value) {
      setAge(value);
    } 
    else {
      setAge('');
      window.alert("Value entered is not a valid age")

    }
  }
  
  function handleGrowableChange(event: ChangeEvent<HTMLInputElement>) {
    const value = event.target.value;
    if (value === "Yes" || value === "No") {
      setGrowable(value);
    } else {
      setGrowable("");
      window.alert("Please select if growable");
    }
  }


  async function handleSubmit(): Promise<void> {
    console.log("Please print something out");
    
    // if ()
    // if ( && formData.textbox2 && formData.checkbox) {
    // set history later
    // if (tokens[0] === "broadband") {
    //   props.setHistory([
    //     ...props.history,
    //     [commandString, await getResponse(tokens, { setMode: props.setMode })],
    //   ]);
    // } else {
    //   props.setSearch(tokens[0]);
    //   props.setHistory([
    //     ...props.history,
    //     [commandString, "searched for " + tokens[0]],
    //   ]);
    // }
    // TODO: manually replace spaces with %20 
    console.log(weight)
    console.log(height)
    console.log(age)
    console.log(gender)
    console.log(activityLevel)
    if(weight !== "" && height !== "" && age !== "" && gender !== "" && activityLevel !== "" && growable !== "") {
      console.log(await getPurePlateData(weight, age, height, gender, activityLevel, growable, "Carrots,%20baby,%20raw`Tomato,%20roma"))
      console.log("test");
      setHistory([
        ...history, await getPurePlateData(weight, age, height, gender, activityLevel, growable, "Carrots,%20baby,%20raw`Tomato,%20roma")]);
    } else {
      console.log("One parameter is empty")
    }
    // }
  }

  const [foodOptions, setFoodOptions] = useState<string[]>([""]);
  const [selectedFoods, setSelectedFoods] = useState<string[]>([""]);

  useEffect(() => {
    const fetchInitialFood = async () => {
      const initialFoodData : string | string[] = await getInitialFood();
      if (Array.isArray(initialFoodData) && typeof initialFoodData[0] === 'string') {
        setFoodOptions(initialFoodData);
      } else {

      }
    };

    fetchInitialFood();
  }, []);

  const handleFoodChange = (food: string) => {
    const isSelected = selectedFoods.includes(food);

    if (isSelected) {
      // Remove the food from the selected list
      setSelectedFoods(selectedFoods.filter((item) => item !== food));
    } else {
      // Add the food to the selected list
      setSelectedFoods([...selectedFoods, food]);
    }
  };

  console.log("this is the history before running SearchHistory");
  console.log(history);
  return (
    <div className="AlgorithmPage">
      <Header />
      <div className="Search History">
          <SearchHistory historyData={history} />
      </div>
      <div className="form-container">
        <div className="Weight-container">
          <h1>Weight (kg)</h1>
          <input id="txtbx3" onChange={handleWeightChange}></input>
          <label htmlFor="txtbx3"> </label>
        </div>
        <div className="Height-container">
          <h1>Height (cm)</h1>
          <input id="txtbx2" onChange={handleHeightChange}></input>
          <label htmlFor="txtbx2"> </label>
        </div>
        <div className="age-container">
          <h1>Age</h1>
          <input id="txtbx1" onChange={handleAgeChange}></input>
          <label htmlFor="txtbx1"> </label>
        </div>
        <div className="gender-container">
          <h1>Gender</h1>
          <input
            type="radio"
            id="cb4"
            value="Male"
            checked={gender === "Male"}
            onChange={handleGenderChange}
          ></input>
          <label htmlFor="cb4">Male</label>
          <input
            type="radio"
            id="cb5"
            value="Female"
            checked={gender === "Female"}
            onChange={handleGenderChange}
          ></input>
          <label htmlFor="cb4">Female</label>
        </div>
        {/* <select> */}
        <div className="activity-level-container">
          <h1>Activity Level</h1>
          <input
            type="radio"
            id="rb1"
            value="Sedentary" // might change later
            checked={activityLevel === "Sedentary"}
            onChange={handleActivityLevelChange}
          ></input>
          <label htmlFor="rb1">Sedentary </label>
          <input
            type="radio"
            id="rb2"
            value="Lightly Active"
            checked={activityLevel === "Lightly Active"}
            onChange={handleActivityLevelChange}
          ></input>
          <label htmlFor="rb2"> Lightly Active </label>
          <input
            type="radio"
            id="rb3"
            value="Moderately Active"
            checked={activityLevel === "Moderately Active"}
            onChange={handleActivityLevelChange}
          ></input>
          <label htmlFor="rb3"> Moderately Active </label>
          <input
            type="radio"
            id="rb4"
            value="Very Active"
            checked={activityLevel === "Very Active"}
            onChange={handleActivityLevelChange}
          ></input>
          <label htmlFor="rb4"> Very Active </label>
          <input
            type="radio"
            id="rb5"
            value="Extra Active"
            checked={activityLevel === "Extra Active"}
            onChange={handleActivityLevelChange}
          ></input>
          <label htmlFor="rb5"> Extra Active </label>
        </div>
        <div className="growable-container">
          <h1 className="Growable">Only Search Growable Foods? </h1>
          <input
            type="radio"
            id="rb1"
            value="Yes"
            checked={growable === "Yes"}
            onChange={handleGrowableChange}
          ></input>
          <label htmlFor="rb1"> Yes </label>
          <input
            type="radio"
            id="rb2"
            value="No"
            checked={growable === "No"}
            onChange={handleGrowableChange}
          ></input>
          <label htmlFor="rb2"> No </label>
        </div>
        <div className="food-container">
          <h1>Select Foods</h1>
          <FormControl component="fieldset">
            <FormGroup>
              {foodOptions.map((food) => (
                <FormControlLabel
                  key={food}
                  control={
                    <Checkbox
                      checked={selectedFoods.includes(food)}
                      onChange={() => handleFoodChange(food)}
                    />
                  }
                  label={food}
                />
              ))}
            </FormGroup>
          </FormControl>
        </div>
        <button
          aria-label="Submit Button"
          id="Submit_Button"
          aria-description="This is the submit button. Click this to submit command you inputted."
          onClick={() => handleSubmit()}
          value="Save"
        >
          Submit All!
        </button>
      </div>
    </div>
  );
}

export default AlgorithmPage;

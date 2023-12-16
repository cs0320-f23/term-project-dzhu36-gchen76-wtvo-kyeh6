//will have name of patient box, age box, and weight box
import React from "react";
import Header from "../../Header";
import SearchHistory from "./SearchHistory";
import { Dispatch, SetStateAction, useState } from "react";
import { ChangeEvent } from 'react';
import Checkbox from '@mui/material/Checkbox';
import FormControlLabel from '@mui/material/FormControlLabel';
import { getPurePlateData } from "./FetchReccomendations";
// import React, { ChangeEvent } from "react";

function AlgorithmPage() {
  // handleCheckboxChange = (event: ChangeEvent<HTMLInputElement>) => {
  //     const checkboxes = document.querySelectorAll('.activity-level-container input[type="checkbox"]');

  //     checkboxes.forEach((checkbox: HTMLInputElement) => {
  //       if (checkbox !== event.target) {
  //         checkbox.checked = false;
  //       }
  //     });
  //   };

  //   var checkList = document.getElementById("list1");
  //   checkList.getElementsByClassName("anchor")[0].onclick = function (evt) {
  //     if (checkList.classList.contains("visible"))
  //       checkList.classList.remove("visible");
  //     else checkList.classList.add("visible");
  //   };


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

  // const Age = document.getElementById('txtbx1')
  const Height = document.getElementById('txtbx2')
  const Weight = document.getElementById('txtbx3')
  const submitBtn = document.getElementById('Submit_Button')


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
      console.log(getPurePlateData(weight, age, height, gender, activityLevel, growable, "Carrots,%20baby,%20raw`Tomato,%20roma"))
    } else {
      console.log("One parameter is empty")
    }
    // }
  }

  return (
    <div className="AlgorithmPage">
      <Header />
      <SearchHistory />
      <h1 className="Age">Age</h1>
      <h1 className="Gender">Gender</h1>
      <h1 className="Weight">Weight (kg)</h1>
      <h1 className="Activity-Level">Activity Level</h1>
      <h1 className="Height">Height (cm)</h1>
      <h1 className="Growable">Only Search Growable Foods? </h1>s
      <h1 className="Weight-container">
        <input id="txtbx3" onChange={handleWeightChange}></input>
        <label htmlFor="txtbx3"> </label>
      </h1>
      <h1 className="Height-container">
        <input id="txtbx2" onChange={handleHeightChange}></input>
        <label htmlFor="txtbx2"> </label>
      </h1>
      <h1 className="age-container">
        <input id="txtbx1" onChange={handleAgeChange}></input>
        <label htmlFor="txtbx1"> </label>
      </h1>
      <h1 className="gender-container">
        <input
          type="radio"
          id="cb4"
          value="Male"
          checked={gender === "Male"}
          onChange={handleGenderChange}
        ></input>
        <label htmlFor="cb4">Male</label>
      </h1>
      <h1 className="gender-container">
        <input
          type="radio"
          id="cb5"
          value="Female"
          checked={gender === "Female"}
          onChange={handleGenderChange}
        ></input>
        <label htmlFor="cb5"> Female </label>
      </h1>
      {/* <select> */}
      <h1 className="activity-level-container">
        <input
          type="radio"
          id="rb1"
          value="Sedentary" // might change later
          checked={activityLevel === "Sedentary"}
          onChange={handleActivityLevelChange}
        ></input>
        <label htmlFor="rb1">Sedentary </label>
      </h1>
      <h1 className="activity-level-container">
        <input
          type="radio"
          id="rb2"
          value="Lightly Active"
          checked={activityLevel === "Lightly Active"}
          onChange={handleActivityLevelChange}
        ></input>
        <label htmlFor="rb2"> Lightly Active </label>
      </h1>
      <h1 className="activity-level-container">
        <input
          type="radio"
          id="rb3"
          value="Moderately Active"
          checked={activityLevel === "Moderately Active"}
          onChange={handleActivityLevelChange}
        ></input>
        <label htmlFor="rb3"> Moderately Active </label>
      </h1>
      <h1 className="activity-level-container">
        <input
          type="radio"
          id="rb4"
          value="Very Active"
          checked={activityLevel === "Very Active"}
          onChange={handleActivityLevelChange}
        ></input>
        <label htmlFor="rb4"> Very Active </label>
      </h1>
      <h1 className="activity-level-container">
        <input
          type="radio"
          id="rb5"
          value="Extra Active"
          checked={activityLevel === "Extra Active"}
          onChange={handleActivityLevelChange}
        ></input>
        <label htmlFor="rb5"> Extra Active </label>
      </h1>
      <h1 className="growable-container">
        <input
          type="radio"
          id="rb1"
          value="Yes"
          checked={growable === "Yes"}
          onChange={handleGrowableChange}
        ></input>
        <label htmlFor="rb1"> </label>
      </h1>
      <h1 className="growable-container">
        <input
          type="radio"
          id="rb2"
          value="No"
          checked={growable === "No"}
          onChange={handleGrowableChange}
        ></input>
        <label htmlFor="rb2"> </label>
      </h1>
      <button
        aria-label="Submit Button"
        id="Submit_Button"
        aria-description="This is the submit button. Click this to submit command you inputted."
        onClick={() => handleSubmit()}
        value="Save"
      >
        Submit Keyword
      </button>
    </div>
  );
}

export default AlgorithmPage;

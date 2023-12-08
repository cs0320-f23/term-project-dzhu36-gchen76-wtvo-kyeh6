//will have name of patient box, age box, and weight box
import React from "react";
import Header from "../../Header";

// import grannyImage from "../public/old-woman-gardening.avif";

function AlgorithmPage() {
  {
    return (
      <div className="AlgorithmPage">
        <Header />
        <h1 className="Age">Age</h1>
        <h1 className="Gender">Gender</h1>
        <h1 className="Weight">Weight</h1>
        <h1 className="Activity Level">Activity Level</h1>
        <h1 className="Height">Height</h1>

        <h1 className="activitylevel">
          <input
            className="LowCheckBox"
            type="checkbox"
            id="check"
            size="larger"
          ></input>
          <label for="LowCheckBox">Checkbox</label>
        </h1>
      </div>
    );
  }
}

export default AlgorithmPage;

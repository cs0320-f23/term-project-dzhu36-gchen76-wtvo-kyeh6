//will have name of patient box, age box, and weight box
import React from "react";
import Header from "../../Header";
import SearchHistory from "./SearchHistory";

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

  {
    return (
      <div className="AlgorithmPage">
        <Header />
        <SearchHistory />
        <h1 className="Age">Age</h1>
        <h1 className="Gender">Gender</h1>
        <h1 className="Weight">Weight (kg)</h1>
        <h1 className="Activity-Level">Activity Level</h1>
        <h1 className="Height">Height (m)</h1>

        <h1 class="Weight-container">
          <input id="txtbx2"></input>
          <label for="txtbx2"> </label>
        </h1>

        <h1 class="Height-container">
          <input id="txtbx2"></input>
          <label for="txtbx2"> </label>
        </h1>

        <h1 class="age-container">
          <input id="txtbx1"></input>
          <label for="txtbx1"> </label>
        </h1>

        <h1 class="gender-container">
          <input type="checkbox" id="cb4"></input>
          <label for="cb4">Male</label>
        </h1>
        <h1 class="gender-container">
          <input type="checkbox" id="cb5"></input>
          <label for="cb5"> Female </label>
        </h1>

        {/* <select> */}
        <h1 class="activity-level-container">
          <input type="checkbox" id="cb1"></input>
          <label for="cb1">Low </label>
        </h1>
        <h1 class="activity-level-container">
          <input type="checkbox" id="cb2"></input>
          <label for="cb2"> Medium </label>
        </h1>
        <h1 class="activity-level-container">
          <input type="checkbox" id="cb3"></input>
          <label for="cb3"> High </label>
        </h1>

        {/* </select> */}

        {/* <div id="list1" class="dropdown-check-list" tabindex="100">
          <span className="anchor">Select Fruits</span>
          <ul class="items">
            <li>
              <input type="checkbox" />
              Apple{" "}
            </li>
            <li>
              <input type="checkbox" />
              Orange
            </li>
            <li>
              <input type="checkbox" />
              Grapes{" "}
            </li>
            <li>
              <input type="checkbox" />
              Berry{" "}
            </li>
            <li>
              <input type="checkbox" />
              Mango{" "}
            </li>
            <li>
              <input type="checkbox" />
              Banana{" "}
            </li>
            <li>
              <input type="checkbox" />
              Tomato
            </li>
          </ul>
        </div> */}
      </div>
    );
  }
}

export default AlgorithmPage;

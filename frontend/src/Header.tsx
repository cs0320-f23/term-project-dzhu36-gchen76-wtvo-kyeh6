//will have search bar, home button, login butotn, and contact button

import React from "react";
//import logoImage from "./public/LogoP.png";
import { useNavigate } from "react-router-dom";

interface HeaderProps {}

// const navigate = useNavigate();

function handleClickHome() {
  // Logic to handle the "Home" button click
  // if window.
  console.log("Home button clicked");
  window.scrollTo({
    top: 0,
    left: 0,
    behavior: "smooth",
  });
}

function handleClickTeam() {
  // Logic to handle the "Team" button click
  console.log("Team button clicked");
  window.scrollTo({
    top: 600,
    left: 0,
    behavior: "smooth",
  });
}

function handleClickUseApplication() {
  // Logic to handle the "Use Application" button click
  console.log("Use Application button clicked");
  // routeChange; //calls the change in route
}
const Header = () => {
  {
    const navigate = useNavigate();

    return (
      <div className="header">
        <h1 id="headerbar">
          PurePlate
          {/* <br />
          Nourshing Nutrients For All */}
          <button className="HomeButton" onClick={handleClickHome}>
            Home
          </button>
          <button className="TeamButton" onClick={handleClickTeam}>
            Meet The Team
          </button>
          <button
            className="ApplicationButton"
            onClick={() => navigate("software")}
          >
            Software
          </button>
        </h1>
      </div>
    );
  }
};

export default Header;

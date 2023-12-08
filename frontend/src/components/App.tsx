import React from "react";
import Header from "../Header";
import Pagebreaker from "../Page-Breaker";
import "../styles/App.css";
import TextBox from "../HomePage/text";
import Profile from "../HomePage/profile";

import granImage from "../public/gardening.png";
import pot from "../public/potted-plant.jpg";

class App extends React.Component {
  render() {
    return (
      <div className="app">
        <Header />

        <div className="granny-image-container">
          <img id="grannyImage " src={granImage} />
        </div>

        <>
          <TextBox></TextBox>
          <Pagebreaker></Pagebreaker>
          <Profile></Profile>
        </>
      </div>
    );
  }
}

export default App;

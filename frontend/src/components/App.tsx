import React from "react";
import Header from "../Header";
import Pagebreaker from "../Page-Breaker";
import "../styles/App.css";
import TextBox from "../HomePage/text";
import Profile from "../HomePage/profile";

// import grannyImage from "../public/old-woman-gardening.avif";

class App extends React.Component {
  render() {
    return (
      <div className="app">
        <Header />
        <>
          <TextBox></TextBox>
          <Pagebreaker></Pagebreaker>
          <Profile></Profile>
        </>
        <div className="app-content">{/* Your main content goes here */}</div>
      </div>
    );
  }
}

export default App;

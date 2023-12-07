//will have profile image, and profile text
//will have search bar, home button, login butotn, and contact button

import React from "react";
// const grannyImage = require("../public/old-woman-gardening.avif");
const Profile = () => {
  {
    return (
      <div className="Team Descriptions">
        {/* <img src={grannyImage} /> */}
        <h3 id="MeetTheTeam">Meet The Team</h3>
        <h1 id="Daniel">
          Daniel Zhu <br />
          Backend Developer <br />
          Daniel_zhu1@Brown.edu
        </h1>
        <h1 id="Kyle">
          Kyle Yeh <br />
          Backend Developer <br />
          Kyle_Yeh@Brown.edu
        </h1>
        <h1 id="Grace">
          Grace Chen <br />
          Frontend Developer <br />
          Grace_A_Chen@Brown.edu
        </h1>
        <h1 id="Wilson">
          Wilson Vo <br />
          Frontend and UI/UX Developer <br />
          Wilson_Vo@Brown.edu
        </h1>
      </div>
    );
  }
};

export default Profile;

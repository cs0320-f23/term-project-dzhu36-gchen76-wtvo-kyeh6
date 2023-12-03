//will have search bar, home button, login butotn, and contact button

import React from 'react';
import logoImage from './public/LogoP.png';


interface HeaderProps {
onHomeClick: () => void;
onTeamClick: () => void;
onUseApplicationClick: () => void;
}


class Header extends React.Component<HeaderProps> {
  handleClickHome = () => {
    // Logic to handle the "Home" button click
    console.log('Home button clicked');
  };

  handleClickTeam = () => {
    // Logic to handle the "Team" button click
    console.log('Team button clicked');
  };

  handleClickUseApplication = () => {
    // Logic to handle the "Use Application" button click
    console.log('Use Application button clicked');
  };
  render() {
    return (
      <div className="header">
        <Image >{logoImage}</Image>
        <h1>urePlate</h1>
        <button className="header-button"onClick={this.handleClickHome}>
          Home
        </button >
        <button className="header-button" onClick={this.handleClickTeam}>
          Meet The Team
        </button>
        <button className="header-button" onClick={this.handleClickUseApplication}>
          Use Application!
        </button>
      </div>
    );
  }
}

export default Header;
//will have search bar, home button, login butotn, and contact button

import React from 'react';
import logoImage from './public/LogoP.png';

 
interface HeaderProps {

}

function handleClickHome (){
  // Logic to handle the "Home" button click
  console.log('Home button clicked');
};

function handleClickTeam() {
  // Logic to handle the "Team" button click
  console.log('Team button clicked');
};

function handleClickUseApplication() {
  // Logic to handle the "Use Application" button click
  console.log('Use Application button clicked');
};
const Header = () => {
 
   {
    return (
      <div className="header">
        <img >src={logoImage}</img>
        <h1>urePlate</h1>
        <button className="header-button"onClick={handleClickHome}>
          Home
        </button >
        <button className="header-button" onClick={handleClickTeam}>
          Meet The Team
        </button>
        <button className="header-button" onClick={handleClickUseApplication}>
          Use Application!
        </button>
      </div>
    );
  }
}

export default Header;
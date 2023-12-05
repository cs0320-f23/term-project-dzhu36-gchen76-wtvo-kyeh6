import React from 'react';
import Header from './Header';
import './App.css';
import grannyImage from './public/old-woman-gardening.avif'

class App extends React.Component {
  render() {
    return (
      <div className="app">
       <Header/>
        <div className="app-content">
          
          {/* Your main content goes here */}
        </div>
      </div>
    );
  }
}

export default App;

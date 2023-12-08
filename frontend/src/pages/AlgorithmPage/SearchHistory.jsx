import React from "react";

class SearchHistory extends React.Component {
  render() {
    return <div className="searchHistory">Search History</div>;
    <div style="height:120px;width:120px;border:1px solid #ccc;font:16px/26px Georgia, Garamond, Serif;overflow:auto;">
      As you can see, once there's enough text in this box, the box will grow
      scroll bars... that's why we call it a scroll box! You could also place an
      image into the scroll box.
    </div>;
  }
}

export default SearchHistory;

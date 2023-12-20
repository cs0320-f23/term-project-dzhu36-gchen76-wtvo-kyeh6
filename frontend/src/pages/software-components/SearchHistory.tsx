import React from "react";
import { RecommendationToTable } from "./Table";

interface SearchHistoryProps {
  historyData: (string | string[])[]; // Assuming historyData is an array of strings
}

class SearchHistory extends React.Component<SearchHistoryProps> {
  render() {
    return (
      <div className="searchHistory">
        Search History
        <div className="scroll-box">
          <p>{getResult(this.props.historyData)}</p>
        </div>
      </div>
    );
    // <div style="height:120px;width:120px;border:1px solid #ccc;font:16px/26px Georgia, Garamond, Serif;overflow:auto;">
    //   As you can see, once there's enough text in this box, the box will grow
    //   scroll bars... that's why we call it a scroll box! You could also place an
    //   image into the scroll box.
    // </div>;
  }
}



// export function SearchHistory(props: SearchHistoryProps) {
//   return (
//       <div className="searchHistory">
//         Search History
//         <div className="scroll-box">
//           <p>{getResult(props.historyData)}</p>
//         </div>
//       </div>
//     );
//     // <div style="height:120px;width:120px;border:1px solid #ccc;font:16px/26px Georgia, Garamond, Serif;overflow:auto;">
//     //   As you can see, once there's enough text in this box, the box will grow
//     //   scroll bars... that's why we call it a scroll box! You could also place an
//     //   image into the scroll box.
//     // </div>;
// }

function getResult(historyData: (string | string[])[]) {
  return historyData.map((data, index) => {
    if (typeof data === "string") {
      // For string, return as paragraph
      return (
        <div key={index} className="history-entry">
          <p key={index}>{data}</p>
        </div>
      );
    } else if (Array.isArray(data)) {
      // For string array, use RecommendationToTable
      return (
        <div key={index} className="history-entry">
          <RecommendationToTable key={index} data={data} />
        </div>
      );
    } else {
      return <p></p>;
    }
  });
}
export default SearchHistory;

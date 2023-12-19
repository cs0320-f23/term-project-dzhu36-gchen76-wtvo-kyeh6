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

function getResult(commandTuple: (string | string[])[]) {
  const lastElement = commandTuple[commandTuple.length - 1];
  if (typeof lastElement === "string") {
    return lastElement;
  }
  console.log("this is the type of lastElement");
  console.log(typeof lastElement);
  return (
    <RecommendationToTable data={lastElement!} />
  );
}
export default SearchHistory;

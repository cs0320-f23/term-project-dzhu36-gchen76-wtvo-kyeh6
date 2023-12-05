import { useState } from "react";
import "../styles/main.css";
import { REPLHistory } from "./REPLHistory";
import { REPLInput } from "./REPLInput";
import { ViewingTable } from "./ViewingTable";

export default function REPL() {
  //Adds shared state of all commands submitted.
  const [history, setHistory] = useState<string[]>([]);

  const [data, setData] = useState<string[][]>([]);

  return (
    <div className="repl">
      <REPLHistory history={history} />
      <hr></hr>
      <REPLInput
        history={history}
        setHistory={setHistory}
        data={data}
        setData={setData}
      />
      <hr></hr>
      <ViewingTable data={data} />
    </div>
  );
}

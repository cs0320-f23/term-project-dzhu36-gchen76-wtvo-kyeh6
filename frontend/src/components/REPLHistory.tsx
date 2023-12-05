import "../styles/main.css";
//This is the REPLHistory File
//This interface created props such as hustory
interface REPLHistoryProps {
  history: string[];
}
//This function builds the history
export function REPLHistory(props: REPLHistoryProps) {
  return (
    <div className="repl-history" title="repl-history">
      {props.history.map((command, index) => (
        <p title="history-line">{command}</p>
      ))}
    </div>
  );
}

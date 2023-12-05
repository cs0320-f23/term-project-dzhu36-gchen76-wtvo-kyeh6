import "../styles/main.css";
//this interface creates a mock data set
interface ViewingTableProps {
  data: string[][];
}
//this function creates the data sata in the form of HTML data set
export function ViewingTable(props: ViewingTableProps) {
  return (
    <div title="viewing-table">
      <table title="view-table" className="view-table">
        {/* loops through the 2d array that was passed in to create a table that 
        is visible to the user */}
        {props.data.map((rows: string[]) => {
          return (
            <tr title="row">
              {rows.map((cell: string) => {
                return <td title="cell">{cell}</td>;
              })}
            </tr>
          );
        })}
      </table>
    </div>
  );
}
